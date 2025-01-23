package com.akgarg.paymentservice.v1.paypal;

import com.akgarg.paymentservice.db.DatabaseService;
import com.akgarg.paymentservice.eventpublisher.PaymentEvent;
import com.akgarg.paymentservice.eventpublisher.PaymentEventPublisher;
import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentDetailDto;
import com.akgarg.paymentservice.payment.PaymentStatus;
import com.akgarg.paymentservice.v1.paypal.request.CaptureOrderRequest;
import com.akgarg.paymentservice.v1.paypal.request.CreateOrderRequest;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
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

    public CreateOrderResponse createOrder(final String requestId, final String userId, final CreateOrderRequest request) throws Exception {
        log.info("[{}] Create order request: {}", requestId, request);

        if (userId != null && request.userId() != null && !userId.equals(request.userId())) {
            log.warn("[{}] User id is not the same as the requested user id", requestId);
            throw new PaymentException(requestId,
                    HttpStatus.BAD_REQUEST.value(),
                    List.of("UserId in header and request body mismatch"),
                    REQUEST_VALIDATION_FAILED_MSG);
        }

        final var packId = request.packId();

        // check if subscription pack exists
        if (subscriptionCache.getSubscriptionPack(requestId, packId).isEmpty()) {
            log.error("[{}] No subscription plan configured for pack: {}", requestId, packId);
            return new CreateOrderResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    requestId,
                    REQUEST_VALIDATION_FAILED_MSG,
                    null,
                    null,
                    List.of("Invalid pack id provided: " + packId)
            );
        }

        // check is user has same or some other pack activated
        final var activeSubscription = subscriptionCache.getActiveSubscription(requestId, userId);

        if (activeSubscription.isPresent()) {
            final var subscription = activeSubscription.get();

            // check if same subscription is activated
            if (subscription.packId().equals(packId) && subscription.expiresAt() > System.currentTimeMillis()) {
                log.error("[{}] Pack {} already activated", requestId, packId);
                return new CreateOrderResponse(
                        HttpStatus.CONFLICT.value(),
                        requestId,
                        REQUEST_VALIDATION_FAILED_MSG,
                        null,
                        null,
                        List.of("Pack already activated: " + activeSubscription.get().packId())
                );
            }

            // check if current subscription is not default/free one
            final var activeSubscriptionPack = subscriptionCache.getSubscriptionPack(requestId, subscription.packId());
            if (activeSubscriptionPack.isPresent() &&
                    activeSubscriptionPack.get().defaultPack() != Boolean.TRUE &&
                    activeSubscriptionPack.get().price() > 0) {
                log.error("[{}] Pack with id {} already activated", requestId, subscription.packId());
                return new CreateOrderResponse(
                        HttpStatus.CONFLICT.value(),
                        requestId,
                        REQUEST_VALIDATION_FAILED_MSG,
                        null,
                        null,
                        List.of("Subscription Pack already activated: " + activeSubscription.get().packId())
                );
            }
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

        log.info("[{}] Payment order created: {}", requestId, order);

        if (order.getPurchaseUnits().size() != 1) {
            log.error("[{}] Payment order does not have valid purchase units", requestId);
            throw new PaymentException(requestId,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    List.of(FAILED_TO_PROCESS_PAYMENT_REQ_MSG),
                    "Failed to create order");
        }

        final var approvalLink = order.getLinks().stream().filter(link -> "approve".equals(link.getRel())).findFirst();

        if (approvalLink.isEmpty()) {
            log.error("[{}] Payment order created but no approval link found", requestId);
            throw new PaymentException(requestId,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    List.of(FAILED_TO_PROCESS_PAYMENT_REQ_MSG),
                    "Failed to create order");
        }

        final var paymentDetails = saveOrderInDatabase(requestId, userId, packId, order);

        log.info("[{}] Payment order created with id: {}", requestId, paymentDetails.getId());

        return new CreateOrderResponse(
                HttpStatus.CREATED.value(),
                requestId,
                "Payment order created successfully",
                paymentDetails.getId(),
                approvalLink.get().getHref(),
                Collections.emptyList());
    }

    public GetOrderResponse getOrderByOrderId(final String requestId, final String orderId) {
        log.info("[{}] Get order request: {}", requestId, orderId);

        final var paymentDetailOptional = databaseService.getPaymentDetails(requestId, orderId);

        if (paymentDetailOptional.isEmpty()) {
            log.error("[{}] Payment order not found", requestId);
            return new GetOrderResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    requestId,
                    "No payment order found with id: " + orderId,
                    null
            );
        }

        final var paymentDetailDto = PaymentDetailDto.fromPaymentDetail(paymentDetailOptional.get());

        return new GetOrderResponse(
                HttpStatus.OK.value(),
                requestId,
                "Payment order fetched successfully",
                paymentDetailDto
        );
    }

    public CaptureOrderResponse captureOrder(final String requestId, final CaptureOrderRequest request) throws Exception {
        log.info("[{}] Capture order request: {}", requestId, request);

        final var paymentId = request.paymentId();
        final var paymentDetailOptional = databaseService.getPaymentDetails(requestId, paymentId);

        if (paymentDetailOptional.isEmpty()) {
            log.warn("[{}] No payment details found for payment id: {}}", requestId, paymentId);
            throw new PaymentException("Webhook", HttpStatus.NOT_FOUND.value(), List.of("No payment details found"), "Webhook processing failed");
        }

        final var paymentDetail = paymentDetailOptional.get();

        if (paymentDetail.getPaymentStatus() != PaymentStatus.CREATED) {
            log.info("[{}] Payment status is {}. Ignoring capture order request", requestId, paymentDetail.getPaymentStatus());
            return new CaptureOrderResponse(
                    "Payment status is %s".formatted(paymentDetail.getPaymentStatus()),
                    HttpStatus.OK.value()
            );
        }

        log.info("[{}] updating payment status to {}", requestId, PaymentStatus.PROCESSING);
        paymentDetail.setPaymentStatus(PaymentStatus.PROCESSING);
        databaseService.updatePaymentDetails(requestId, paymentDetail);

        final var ordersCaptureInput = new OrdersCaptureInput.Builder()
                .id(paymentId)
                .prefer("return=minimal")
                .build();

        log.info("[{}] Sending order capture request: {}", requestId, ordersCaptureInput);
        final var orderCaptureResponse = paypalClient.getOrdersController().ordersCapture(ordersCaptureInput);
        log.info("[{}] Order capture response status {} with code {}", requestId, orderCaptureResponse.getResult().getStatus(), orderCaptureResponse.getStatusCode());

        if (orderCaptureResponse.getStatusCode() == HttpStatus.CREATED.value() && orderCaptureResponse.getResult().getStatus() == OrderStatus.COMPLETED) {
            completePayment(requestId, paymentId, paymentDetail);
        }

        return new CaptureOrderResponse(
                "Payment completed successfully. It may take some time for changes to reflect in your account",
                HttpStatus.OK.value()
        );
    }

    public void processWebhook(final Map<String, Object> requestBody) throws Exception {
        log.info("Paypal Webhook request: {}", requestBody);
        final var eventType = PaypalWebhookEventType.fromValue(requestBody.get("event_type").toString());

        if (eventType.isEmpty()) {
            log.info("Not processing webhook request for event: {}", requestBody.get("event_type"));
            return;
        }

        switch (eventType.get()) {
            case ORDER_APPROVED -> handleOrderApprovedWebhook(requestBody);
            case PAYMENT_CAPTURE_COMPLETE -> handlePaymentCaptureCompleteWebhook(requestBody);
            default -> log.warn("Unsupported event type: {}", eventType);
        }
    }

    private void handleOrderApprovedWebhook(final Map<String, Object> requestBody) throws Exception {
        final var webhookId = requestBody.get("id").toString();
        log.info("[{}] Processing Paypal Webhook {} request", webhookId, PaypalWebhookEventType.ORDER_APPROVED);

        final String paymentId;
        final Map<?, ?> resourceMap;

        if (requestBody.get("resource") instanceof Map<?, ?> mp) {
            resourceMap = mp;
            paymentId = resourceMap.get("id").toString();
        } else {
            throw new PaymentException(webhookId, HttpStatus.BAD_REQUEST.value(), List.of("Failed to extract order Id"), FAILED_TO_PROCESS_PAYMENT_REQ_MSG);
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

        captureOrder(webhookId, request);
    }

    private void handlePaymentCaptureCompleteWebhook(final Map<String, Object> requestBody) {
        final var webhookId = requestBody.get("id").toString();
        log.info("[{}] Processing Paypal Webhook {} request", webhookId, PaypalWebhookEventType.PAYMENT_CAPTURE_COMPLETE);

        final String paymentId;

        if (requestBody.get("resource") instanceof Map<?, ?> resourceMap &&
                resourceMap.get("supplementary_data") instanceof Map<?, ?> supplementaryData &&
                supplementaryData.get("related_ids") instanceof Map<?, ?> relatedIds) {
            paymentId = relatedIds.get("order_id").toString();
        } else {
            log.error("[{}] Failed to extract order id from webhook body", webhookId);
            throw new PaymentException(webhookId, HttpStatus.BAD_REQUEST.value(), List.of("Failed to extract order Id"), FAILED_TO_PROCESS_PAYMENT_REQ_MSG);
        }

        final var paymentDetailOptional = databaseService.getPaymentDetails(webhookId, paymentId);

        if (paymentDetailOptional.isEmpty()) {
            log.warn("[{}] No payment details found for payment id: {}}", webhookId, paymentId);
            throw new PaymentException("Webhook", HttpStatus.NOT_FOUND.value(), List.of("No payment details found"), "Webhook processing failed");
        }

        completePayment(webhookId, paymentId, paymentDetailOptional.get());
    }

    private void completePayment(final String requestId, final String paymentId, final PaymentDetail paymentDetail) {
        if (paymentDetail.getPaymentStatus() == PaymentStatus.COMPLETED) {
            log.info("[{}] Payment with id {} already marked as {}", requestId, paymentId, PaymentStatus.COMPLETED);
            return;
        }

        log.info("[{}] updating payment status to {} for id: {}", requestId, PaymentStatus.COMPLETED, paymentId);

        paymentDetail.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentDetail.setCompletedAt(System.currentTimeMillis());
        final var updatedPaymentDetails = databaseService.updatePaymentDetails(requestId, paymentDetail);

        log.info("[{}] Payment status updated successfully to {} for id: {}", requestId, updatedPaymentDetails.getPaymentStatus(), updatedPaymentDetails.getId());

        publishPaymentSuccessEvent(requestId, updatedPaymentDetails);
    }

    private void publishPaymentSuccessEvent(final String webhookId, final PaymentDetail paymentDetail) {
        final var paymentEvent = new PaymentEvent(
                paymentDetail.getId(),
                paymentDetail.getUserId(),
                paymentDetail.getPackId(),
                paymentDetail.getAmount(),
                paymentDetail.getCurrency(),
                paymentDetail.getPaymentGateway()
        );
        log.info("[{}] Publishing payment success event: {}", webhookId, paymentEvent);
        paymentEventPublisher.publish(paymentEvent);
    }

    private PaymentDetail saveOrderInDatabase(final String requestId, final String userId, final String packId, final Order order) {
        final var paymentDetail = new PaymentDetail();
        paymentDetail.setId(order.getId());
        paymentDetail.setUserId(userId);
        paymentDetail.setPackId(packId);
        paymentDetail.setPaymentStatus(PaymentStatus.CREATED);
        paymentDetail.setPaymentGateway(PAYMENT_GATEWAY_NAME);
        paymentDetail.setAmount(Double.valueOf(order.getPurchaseUnits().getFirst().getAmount().getValue()));
        paymentDetail.setCurrency(order.getPurchaseUnits().getFirst().getAmount().getCurrencyCode());
        paymentDetail.setDeleted(false);
        return databaseService.savePaymentDetails(requestId, paymentDetail);
    }

}
