package com.akgarg.paymentservice.v1.paypal;

import com.akgarg.paymentservice.eventpublisher.PaymentEvent;
import com.akgarg.paymentservice.eventpublisher.PaymentEventPublisher;
import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentDetailDto;
import com.akgarg.paymentservice.payment.PaymentStatus;
import com.akgarg.paymentservice.v1.db.DatabaseService;
import com.akgarg.paymentservice.v1.paypal.request.CancelPaymentRequest;
import com.akgarg.paymentservice.v1.paypal.request.CaptureOrderRequest;
import com.akgarg.paymentservice.v1.paypal.request.CreateOrderRequest;
import com.akgarg.paymentservice.v1.paypal.response.CancelPaymentResponse;
import com.akgarg.paymentservice.v1.paypal.response.CaptureOrderResponse;
import com.akgarg.paymentservice.v1.paypal.response.CreateOrderResponse;
import com.akgarg.paymentservice.v1.paypal.response.GetOrderResponse;
import com.akgarg.paymentservice.v1.subscription.SubscriptionCache;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Service
@RequiredArgsConstructor
public class PaypalService {

    private static final String FAILED_TO_PROCESS_PAYMENT_REQ_MSG = "Failed to process the payment request";
    private static final String REQUEST_VALIDATION_FAILED_MSG = "Request validation failed";
    private static final String PAYMENT_GATEWAY_NAME = "paypal";

    private final PaymentEventPublisher paymentEventPublisher;
    private final SubscriptionCache subscriptionCache;
    private final PaypalServerSdkClient paypalClient;
    private final DatabaseService databaseService;
    private final Environment environment;

    public CreateOrderResponse createOrder(final CreateOrderRequest request) throws Exception {
        log.info("Received create order request {}", request);

        final var packId = request.packId();

        final var subscriptionPack = subscriptionCache.getSubscriptionPack(packId);

        if (subscriptionPack.isEmpty()) {
            log.error("No subscription plan configured for pack {}", packId);
            return CreateOrderResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message(FAILED_TO_PROCESS_PAYMENT_REQ_MSG)
                    .errors(List.of("Invalid pack id provided"))
                    .build();
        }

        final var activeSubscription = subscriptionCache.getActiveSubscription(request.userId());

        if (activeSubscription.isPresent()) {
            final var subscription = activeSubscription.get();

            if (subscription.packId().equals(packId) && subscription.expiresAt() > System.currentTimeMillis()) {
                log.info("Pack {} is already activated for user", packId);
                return CreateOrderResponse.builder()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .message(REQUEST_VALIDATION_FAILED_MSG)
                        .errors(List.of("Pack already activated"))
                        .build();
            }

            final var activeSubscriptionPack = subscriptionCache.getSubscriptionPack(subscription.packId());

            if (activeSubscriptionPack.isPresent() &&
                    activeSubscriptionPack.get().defaultPack() != Boolean.TRUE &&
                    activeSubscriptionPack.get().price() > 0) {
                log.info("Pack with id {} already activated", subscription.packId());

                return CreateOrderResponse.builder()
                        .statusCode(HttpStatus.CONFLICT.value())
                        .message(REQUEST_VALIDATION_FAILED_MSG)
                        .errors(List.of("You already have one activated subscription pack with id " + activeSubscription.get().packId()))
                        .build();
            }
        }

        final var incompletePayments = databaseService.getPaymentDetailForUserByPaymentStatus(request.userId(),
                List.of(PaymentStatus.CREATED, PaymentStatus.PROCESSING));

        if (!incompletePayments.isEmpty()) {
            final var ids = incompletePayments.stream().map(PaymentDetail::getId).collect(Collectors.joining(", "));
            log.info("Incomplete payment found with id {}", ids);
            return CreateOrderResponse.builder()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .message(REQUEST_VALIDATION_FAILED_MSG)
                    .errors(List.of("Existing incomplete payment found with id: " + ids))
                    .build();
        }

        if (!subscriptionPack.get().price().equals(request.amount())) {
            log.warn("Malicious create order request received. Pack price {}, received price: {}",
                    subscriptionPack.get().price(),
                    request.amount());
            return CreateOrderResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .errors(List.of("Invalid amount provided"))
                    .message(FAILED_TO_PROCESS_PAYMENT_REQ_MSG)
                    .build();
        }

        final var amount = new AmountWithBreakdown.Builder()
                .currencyCode(request.currencyCode())
                .value(String.valueOf(request.amount()))
                .build();

        final var purchaseUnitRequest = new PurchaseUnitRequest.Builder()
                .amount(amount)
                .build();

        final var paymentMethod = new PaymentMethodPreference.Builder()
                .payeePreferred(PayeePaymentMethodPreference.IMMEDIATE_PAYMENT_REQUIRED)
                .build();

        final var applicationContext = new OrderApplicationContext.Builder()
                .returnUrl(Objects.requireNonNull(environment.getProperty("paypal.order.return-url")))
                .cancelUrl(Objects.requireNonNull(environment.getProperty("paypal.order.cancel-url")))
                .userAction(OrderApplicationContextUserAction.PAY_NOW)
                .paymentMethod(paymentMethod)
                .build();

        final var orderRequest = new OrderRequest.Builder()
                .intent(CheckoutPaymentIntent.CAPTURE)
                .purchaseUnits(List.of(purchaseUnitRequest))
                .applicationContext(applicationContext)
                .build();

        final var ordersCreateInput = new OrdersCreateInput.Builder()
                .body(orderRequest)
                .prefer("return=representation")
                .build();

        final var orderApiResponse = paypalClient.getOrdersController().ordersCreate(ordersCreateInput);
        final var order = orderApiResponse.getResult();

        if (order == null) {
            log.info("Failed to create order because null received for create order API");
            return CreateOrderResponse.builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .errors(List.of("Invalid response received from payment gateway"))
                    .message(FAILED_TO_PROCESS_PAYMENT_REQ_MSG)
                    .build();
        }

        log.info("Payment order created with id {}", order.getId());

        if (log.isDebugEnabled()) {
            log.debug("Created order: {}", order);
        }

        if (order.getPurchaseUnits().size() != 1) {
            log.info("Payment order does not have valid purchase units");

            throw new PaymentException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    List.of(FAILED_TO_PROCESS_PAYMENT_REQ_MSG),
                    "Failed to create order");
        }

        final var approvalLink = order.getLinks().stream().filter(link -> "approve".equals(link.getRel())).findFirst();

        if (approvalLink.isEmpty()) {
            log.warn("Payment order created but no approval link found");
            throw new PaymentException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    List.of(FAILED_TO_PROCESS_PAYMENT_REQ_MSG),
                    "Failed to create order");
        }

        final var paymentDetails = saveOrderInDatabase(request, order);

        log.info("Payment order created with id {}", paymentDetails.getId());

        return CreateOrderResponse.builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Payment order created successfully")
                .orderId(paymentDetails.getId())
                .approvalUrl(approvalLink.get().getHref())
                .build();
    }

    public GetOrderResponse getOrderByOrderId(final String orderId) {
        log.info("Received request to get order for id {}", orderId);

        final var paymentDetailOptional = databaseService.getPaymentDetails(orderId);

        if (paymentDetailOptional.isEmpty()) {
            log.info("Payment order not found for id {}", orderId);

            return GetOrderResponse.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("No payment order found for id " + orderId)
                    .build();
        }

        final var paymentDetailDto = PaymentDetailDto.fromPaymentDetail(paymentDetailOptional.get());

        return GetOrderResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payment order fetched successfully")
                .paymentDetail(paymentDetailDto)
                .build();
    }

    public CaptureOrderResponse captureOrder(final CaptureOrderRequest request) throws Exception {
        log.info("Capture order request: {}", request);

        final var paymentId = request.paymentId();
        final var paymentDetailOptional = databaseService.getPaymentDetails(paymentId);

        if (paymentDetailOptional.isEmpty()) {
            log.warn("No payment details found for payment id {}", paymentId);
            throw new PaymentException(
                    HttpStatus.NOT_FOUND.value(),
                    List.of("No payment found with id: " + paymentId),
                    "Payment capture failed"
            );
        }

        final var paymentDetail = paymentDetailOptional.get();

        if (!Objects.equals(paymentDetail.getPaymentStatus(), PaymentStatus.CREATED.name())) {
            log.info("Payment status is {}. Ignoring capture order request", paymentDetail.getPaymentStatus());
            return new CaptureOrderResponse(
                    "Payment status is %s".formatted(paymentDetail.getPaymentStatus()),
                    HttpStatus.OK.value()
            );
        }

        log.info("Updating payment status to {}", PaymentStatus.PROCESSING);
        paymentDetail.setPaymentStatus(PaymentStatus.PROCESSING.name());
        databaseService.updatePaymentDetails(paymentDetail);

        final var ordersCaptureInput = new OrdersCaptureInput.Builder()
                .id(paymentId)
                .prefer("return=minimal")
                .build();

        log.info("Sending order capture request: {}", ordersCaptureInput);
        final var orderCaptureResponse = paypalClient.getOrdersController().ordersCapture(ordersCaptureInput);
        log.info("Order capture response status {} with code {}", orderCaptureResponse.getResult().getStatus(), orderCaptureResponse.getStatusCode());

        if (orderCaptureResponse.getStatusCode() == HttpStatus.CREATED.value() && orderCaptureResponse.getResult().getStatus() == OrderStatus.COMPLETED) {
            completePayment(paymentId, paymentDetail);
        }

        return new CaptureOrderResponse(
                "Your payment has been successfully processed. Please allow some time for the changes to be reflected in your account.",
                HttpStatus.OK.value()
        );
    }

    public CancelPaymentResponse cancelPayment(final CancelPaymentRequest request) {
        log.info("Received Cancel payment request {}", request);

        final var paymentId = request.paymentId();
        final var paymentDetailOptional = databaseService.getPaymentDetails(paymentId);

        if (paymentDetailOptional.isEmpty()) {
            log.warn("Payment detail not found for payment Id {}", request.paymentId());
            return CancelPaymentResponse.builder()
                    .success(false)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .message("Payment detail not found for requested id")
                    .build();
        }

        if (paymentDetailOptional.get().getPaymentStatus().equals(PaymentStatus.CANCELLED.name())) {
            log.info("Payment is already cancelled");
            return CancelPaymentResponse.builder()
                    .success(false)
                    .statusCode(HttpStatus.OK.value())
                    .message("Payment is already cancelled")
                    .build();
        }

        paymentDetailOptional.get().setPaymentStatus(PaymentStatus.CANCELLED.name());
        databaseService.updatePaymentDetails(paymentDetailOptional.get());

        log.info("Payment cancelled successfully");

        return CancelPaymentResponse.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Payment cancelled successfully")
                .build();
    }

    public void processWebhook(final Map<String, Object> requestBody) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Paypal Webhook request: {}", requestBody);
        }

        final var eventType = PaypalWebhookEventType.fromValue(requestBody.get("event_type").toString());

        if (eventType.isEmpty()) {
            log.info("Not processing webhook request for event {}", requestBody.get("event_type"));
            return;
        }

        switch (eventType.get()) {
            case ORDER_APPROVED -> handleOrderApprovedWebhook(requestBody);
            case PAYMENT_CAPTURE_COMPLETE -> handlePaymentCaptureCompleteWebhook(requestBody);
            default -> log.info("Unsupported paypal webhook event type {}", eventType);
        }
    }

    private void handleOrderApprovedWebhook(final Map<String, Object> requestBody) throws Exception {
        final var webhookId = requestBody.get("id").toString();
        log.info("Processing {} Paypal Webhook request for webhook id {}", PaypalWebhookEventType.ORDER_APPROVED, webhookId);

        final String paymentId;
        final Map<?, ?> resourceMap;

        if (requestBody.get("resource") instanceof Map<?, ?> mp) {
            resourceMap = mp;
            paymentId = resourceMap.get("id").toString();
        } else {
            throw new PaymentException(HttpStatus.BAD_REQUEST.value(),
                    List.of("Failed to extract order Id"),
                    FAILED_TO_PROCESS_PAYMENT_REQ_MSG
            );
        }

        final var payer = resourceMap.get("payer");

        final String payerId;

        if (payer instanceof Map<?, ?> payerMap) {
            payerId = payerMap.get("payer_id").toString();
        } else {
            payerId = "null";
        }

        final var request = new CaptureOrderRequest(
                paymentId,
                payerId
        );

        captureOrder(request);
    }

    private void handlePaymentCaptureCompleteWebhook(final Map<String, Object> requestBody) {
        final var webhookId = requestBody.get("id").toString();
        log.info("Processing {} Paypal Webhook request for webhook id {}", PaypalWebhookEventType.PAYMENT_CAPTURE_COMPLETE, webhookId);

        final String paymentId;

        if (requestBody.get("resource") instanceof Map<?, ?> resourceMap &&
                resourceMap.get("supplementary_data") instanceof Map<?, ?> supplementaryData &&
                supplementaryData.get("related_ids") instanceof Map<?, ?> relatedIds) {
            paymentId = relatedIds.get("order_id").toString();
        } else {
            log.error("Failed to extract order id from webhook body");
            throw new PaymentException(HttpStatus.BAD_REQUEST.value(), List.of("Failed to extract order Id"), FAILED_TO_PROCESS_PAYMENT_REQ_MSG);
        }

        final var paymentDetailOptional = databaseService.getPaymentDetails(paymentId);

        if (paymentDetailOptional.isEmpty()) {
            log.warn("No payment details found for payment id {}", paymentId);
            throw new PaymentException(HttpStatus.NOT_FOUND.value(), List.of("No payment details found"), "Webhook processing failed");
        }

        completePayment(paymentId, paymentDetailOptional.get());
    }

    private void completePayment(final String paymentId, final PaymentDetail paymentDetail) {
        if (Objects.equals(paymentDetail.getPaymentStatus(), PaymentStatus.COMPLETED.name())) {
            log.info("Payment with id {} already marked as {}", paymentId, PaymentStatus.COMPLETED);
            return;
        }

        log.info("updating payment status to {} for id: {}", PaymentStatus.COMPLETED, paymentId);

        paymentDetail.setPaymentStatus(PaymentStatus.COMPLETED.name());
        paymentDetail.setCompletedAt(System.currentTimeMillis());

        final var updatedPaymentDetails = databaseService.updatePaymentDetails(paymentDetail);

        log.info("Payment status updated successfully to {} for id: {}", updatedPaymentDetails.getPaymentStatus(), updatedPaymentDetails.getId());

        publishPaymentSuccessEvent(updatedPaymentDetails);
    }

    private void publishPaymentSuccessEvent(final PaymentDetail paymentDetail) {
        final var paymentEvent = new PaymentEvent(
                paymentDetail.getId(),
                paymentDetail.getUserId(),
                paymentDetail.getPackId(),
                paymentDetail.getAmount(),
                paymentDetail.getCurrency(),
                paymentDetail.getPaymentGateway(),
                paymentDetail.getEmail(),
                paymentDetail.getName()
        );
        paymentEventPublisher.publishPaymentSuccess(paymentEvent);
    }

    private PaymentDetail saveOrderInDatabase(final CreateOrderRequest request, final Order order) {
        final var paymentDetail = new PaymentDetail();
        paymentDetail.setId(order.getId());
        paymentDetail.setUserId(request.userId());
        paymentDetail.setEmail(request.email());
        paymentDetail.setName(request.name());
        paymentDetail.setPackId(request.packId());
        paymentDetail.setPaymentStatus(PaymentStatus.CREATED.name());
        paymentDetail.setPaymentGateway(PAYMENT_GATEWAY_NAME);
        paymentDetail.setAmount(Double.valueOf(order.getPurchaseUnits().getFirst().getAmount().getValue()));
        paymentDetail.setCurrency(order.getPurchaseUnits().getFirst().getAmount().getCurrencyCode());
        paymentDetail.setCreatedAt(System.currentTimeMillis());
        paymentDetail.setUpdatedAt(System.currentTimeMillis());
        paymentDetail.setDeleted(false);
        return databaseService.savePaymentDetails(paymentDetail);
    }

}
