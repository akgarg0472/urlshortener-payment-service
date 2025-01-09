package com.akgarg.paymentservice.paypal.v1;

import com.akgarg.paymentservice.db.DatabaseService;
import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentDetailDto;
import com.akgarg.paymentservice.payment.PaymentStatus;
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
    private static final String PAYMENT_GATEWAY_NAME = "paypal";

    private final PaypalServerSdkClient paypalClient;
    private final DatabaseService databaseService;
    private final Environment environment;

    public CreateOrderResponse createOrder(final String requestId, final String userId, final CreateOrderRequest request) throws Exception {
        log.info("[{}] Create order request: {}", requestId, request);

        if (userId != null && request.userId() != null && !userId.equals(request.userId())) {
            log.error("[{}] User id is not the same as the requested user id", requestId);
            throw new PaymentException(requestId, HttpStatus.BAD_REQUEST.value(), List.of("UserId in header and request body mismatch"), "Request validation failed");
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
            throw new PaymentException(requestId, HttpStatus.INTERNAL_SERVER_ERROR.value(), List.of(FAILED_TO_PROCESS_PAYMENT_REQ_MSG), "Failed to create order");
        }

        final var approvalLink = order.getLinks().stream().filter(link -> "approve".equals(link.getRel())).findFirst();

        if (approvalLink.isEmpty()) {
            log.error("[{}] Payment order created but no approval link found", requestId);
            throw new PaymentException(requestId, HttpStatus.INTERNAL_SERVER_ERROR.value(), List.of(FAILED_TO_PROCESS_PAYMENT_REQ_MSG), "Failed to create order");
        }

        final var paymentDetails = saveOrderInDatabase(requestId, userId, request.packId(), order);

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

        if (requestBody.get("resource") instanceof Map<?, ?> resourceMap) {
            paymentId = resourceMap.get("id").toString();
        } else {
            throw new PaymentException(webhookId, HttpStatus.BAD_REQUEST.value(), List.of("Failed to extract order Id"), FAILED_TO_PROCESS_PAYMENT_REQ_MSG);
        }

        final var paymentDetails = databaseService.getPaymentDetails(webhookId, paymentId);

        if (paymentDetails.isEmpty()) {
            log.warn("[{}] No payment details found for payment id: {}}", webhookId, paymentId);
            throw new PaymentException("Webhook", HttpStatus.NOT_FOUND.value(), List.of("No payment details found"), "Webhook processing failed");
        }

        log.info("[{}] updating payment status to {}", webhookId, PaymentStatus.PROCESSING);
        paymentDetails.get().setPaymentStatus(PaymentStatus.PROCESSING);
        databaseService.updatePaymentDetails(webhookId, paymentDetails.get());

        final var ordersCaptureInput = new OrdersCaptureInput.Builder()
                .id(paymentId)
                .prefer("return=representation")
                .build();
        log.info("[{}] Sending order capture request: {}", webhookId, ordersCaptureInput);
        final var orderCaptureResponse = paypalClient.getOrdersController().ordersCapture(ordersCaptureInput);
        log.info("[{}] Order capture response: {}", webhookId, orderCaptureResponse.getStatusCode());
    }

    private void handlePaymentCaptureCompleteWebhook(final Map<String, Object> requestBody) {
        final var webhookId = requestBody.get("id").toString();
        log.info("[{}] Processing Paypal Webhook {} request", webhookId, PaypalWebhookEventType.PAYMENT_CAPTURE_COMPLETE);

        final String paymentId;

        if (requestBody.get("supplementary_data") instanceof Map<?, ?> supplementaryData && supplementaryData.get("related_ids") instanceof Map<?, ?> relatedIds) {
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

        log.info("[{}] updating payment status to {} for id: {}", webhookId, PaymentStatus.COMPLETED, paymentId);

        final var paymentDetail = paymentDetailOptional.get();
        paymentDetail.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentDetail.setCompletedAt(System.currentTimeMillis());
        final var updatedPaymentDetails = databaseService.updatePaymentDetails(webhookId, paymentDetail);

        log.info("[{}] Payment status updated successfully to {} for id: {}", webhookId, updatedPaymentDetails.getPaymentStatus(), updatedPaymentDetails.getId());
    }

    private PaymentDetail saveOrderInDatabase(final String requestId, final String userId, final String packId, final Order order) {
        final var paymentDetail = new PaymentDetail();
        paymentDetail.setId(order.getId());
        paymentDetail.setUserId(userId);
        paymentDetail.setPackId(packId);
        paymentDetail.setPaymentStatus(PaymentStatus.CREATED);
        paymentDetail.setPaymentGateway(PAYMENT_GATEWAY_NAME);
        paymentDetail.setAmount(order.getPurchaseUnits().getFirst().getAmount().getValue());
        paymentDetail.setCurrency(order.getPurchaseUnits().getFirst().getAmount().getCurrencyCode());
        paymentDetail.setDeleted(false);
        return databaseService.savePaymentDetails(requestId, paymentDetail);
    }

}
