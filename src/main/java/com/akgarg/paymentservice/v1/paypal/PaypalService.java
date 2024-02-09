package com.akgarg.paymentservice.v1.paypal;

import com.akgarg.paymentservice.db.DatabaseService;
import com.akgarg.paymentservice.exception.DatabaseException;
import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentService;
import com.akgarg.paymentservice.payment.PaymentStatus;
import com.akgarg.paymentservice.request.CompletePaymentRequest;
import com.akgarg.paymentservice.request.CreatePaymentRequest;
import com.akgarg.paymentservice.request.VerifyPaymentRequest;
import com.akgarg.paymentservice.response.CompletePaymentResponse;
import com.akgarg.paymentservice.response.CreatePaymentResponse;
import com.akgarg.paymentservice.response.VerifyPaymentResponse;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class PaypalService implements PaymentService {

    private static final Logger LOG = LogManager.getLogger(PaypalService.class);
    private static final String PAYMENT_GATEWAY_NAME = "Paypal";

    private final DatabaseService databaseService;
    private final PayPalHttpClient httpClient;

    private final String uiCaptureUrl;
    private final String uiCancelUrl;

    public PaypalService(final DatabaseService databaseService, final PayPalHttpClient httpClient) {
        this.httpClient = httpClient;
        this.databaseService = databaseService;
        final Dotenv dotenv = Dotenv.load();
        this.uiCaptureUrl = Objects.requireNonNull(dotenv.get("PAYPAL_FRONTEND_CAPTURE_URL"), "Paypal UI capture URL not found in env");
        this.uiCancelUrl = Objects.requireNonNull(dotenv.get("PAYPAL_FRONTEND_CANCEL_URL"), "Paypal UI cancel URL not found in env");
    }

    public CreatePaymentResponse createPayment(final CreatePaymentRequest createPaymentRequest) {
        final String traceId = createPaymentRequest.userId() + "-" + System.currentTimeMillis();

        LOG.info("{} create payment received for amount={}", traceId, createPaymentRequest.amount());

        final var orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        final var amountBreakdown = new AmountWithBreakdown().currencyCode(createPaymentRequest.currency()).value(String.valueOf(createPaymentRequest.amount()));

        final var purchaseUnitRequest = new PurchaseUnitRequest().amountWithBreakdown(amountBreakdown);
        orderRequest.purchaseUnits(List.of(purchaseUnitRequest));

        final var applicationContext = new ApplicationContext().returnUrl(uiCaptureUrl).cancelUrl(uiCancelUrl);
        orderRequest.applicationContext(applicationContext);

        final var ordersCreateRequest = new OrdersCreateRequest().requestBody(orderRequest);

        try {
            final HttpResponse<Order> orderHttpResponse = httpClient.execute(ordersCreateRequest);
            final Order order = orderHttpResponse.result();

            final var redirectUrl = order.links()
                    .stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No 'approve' link found in order")).href();

            final String orderId = order.id();

            final PaymentDetail paymentDetail = new PaymentDetail(
                    traceId,
                    orderId,
                    createPaymentRequest.userId(),
                    createPaymentRequest.amount(),
                    PaymentStatus.CREATED,
                    createPaymentRequest.currency(),
                    createPaymentRequest.paymentMethod(),
                    Instant.now(),
                    null,
                    PAYMENT_GATEWAY_NAME
            );

            LOG.info("Payment details: {}", paymentDetail);

            databaseService.savePaymentDetails(paymentDetail);

            LOG.info("{} payment created successfully", traceId);

            return new CreatePaymentResponse(traceId, "Payment created successfully", HttpStatus.CREATED.value(), orderId, redirectUrl, null);

        } catch (Exception e) {
            if (e instanceof DatabaseException de) {
                LOG.error("{} failed to save payment detail in database: {}", traceId, de.getMessage(), de);
            } else {
                LOG.error("{} -> {} error processing create payment request: {}", traceId, e.getClass().getSimpleName(), e.getMessage(), e);
            }

            throw new PaymentException(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "Internal Server Error");
        }
    }

    public CompletePaymentResponse completePayment(final CompletePaymentRequest completePaymentRequest) {
        final String paymentId = completePaymentRequest.paymentId();
        String traceId = "";

        try {
            final PaymentDetail paymentDetail = databaseService.getPaymentDetails(paymentId).orElseThrow(() -> new NoSuchElementException("Payment details not found for payment id=" + paymentId));

            traceId = paymentDetail.getTraceId();

            LOG.info("{} complete payment request received for paymentId={}", traceId, paymentId);

            if (!PaymentStatus.CREATED.equals(paymentDetail.getPaymentStatus())) {
                LOG.error("{} complete payment failed. Invalid payment status:{}, payment id={}", traceId, paymentDetail.getPaymentStatus(), paymentId);
                return new CompletePaymentResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        paymentId,
                        PaymentStatus.FAILED.name(),
                        "Invalid payment status: " + paymentDetail.getPaymentStatus()
                );
            }

            final var ordersCaptureRequest = new OrdersCaptureRequest(paymentId);
            final var httpResponse = httpClient.execute(ordersCaptureRequest);
            final var order = httpResponse.result();

            if ("COMPLETED".equalsIgnoreCase(order.status())) {
                LOG.info("{} marking payment status as COMPLETED with id={}", traceId, paymentId);
                paymentDetail.setPaymentStatus(PaymentStatus.COMPLETED);
                paymentDetail.setPaymentCompletedAt(Instant.now());
                databaseService.updatePaymentDetails(paymentDetail);
                return new CompletePaymentResponse(HttpStatus.OK.value(), paymentId, paymentDetail.getPaymentStatus().name(), "Payment completed successfully");
            } else {
                LOG.error("{} complete payment failed id={}", traceId, paymentId);
                paymentDetail.setPaymentStatus(PaymentStatus.FAILED);
                databaseService.savePaymentDetails(paymentDetail);
                paymentDetail.setPaymentCompletedAt(Instant.now());
                return new CompletePaymentResponse(HttpStatus.OK.value(), paymentId, PaymentStatus.FAILED.name(), "Failed to complete payment. Please try again!");
            }
        } catch (Exception e) {
            if (e instanceof DatabaseException de) {
                LOG.error("{} error performing DB operation: {}", traceId, de.getMessage(), de);
            } else {
                LOG.error("{} -> {} error processing complete payment request: {}", traceId, e.getClass().getSimpleName(), e.getMessage(), e);
            }

            throw new PaymentException(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "Internal Server Error");
        }
    }

    @Override
    public VerifyPaymentResponse verifyPayment(final VerifyPaymentRequest verifyPaymentRequest) {
        // TODO implement
        return null;
    }

    @Override
    public String getPaymentGatewayServiceName() {
        return PAYMENT_GATEWAY_NAME;
    }

}
