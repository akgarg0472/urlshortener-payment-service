package com.akgarg.paymentservice.paypal.v1;

import com.akgarg.paymentservice.db.DatabaseService;
import com.akgarg.paymentservice.eventpublisher.PaymentEventPublisher;
import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentStatus;
import com.akgarg.paymentservice.payment.service.AbstractPaymentService;
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
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class PaypalService extends AbstractPaymentService {

    private static final Logger LOG = LogManager.getLogger(PaypalService.class);
    private static final String PAYMENT_GATEWAY_NAME = "Paypal";

    private final PayPalHttpClient httpClient;

    private final String uiCaptureUrl;
    private final String uiCancelUrl;

    public PaypalService(
            @Nonnull final PayPalHttpClient httpClient,
            @Nonnull final DatabaseService databaseService,
            @Nonnull final PaymentEventPublisher paymentEventPublisher
    ) {
        super(databaseService, paymentEventPublisher);
        this.httpClient = httpClient;
        final Dotenv dotenv = Dotenv.load();
        this.uiCaptureUrl = Objects.requireNonNull(dotenv.get("PAYPAL_FRONTEND_CAPTURE_URL"), "Paypal UI capture URL not found in env");
        this.uiCancelUrl = Objects.requireNonNull(dotenv.get("PAYPAL_FRONTEND_CANCEL_URL"), "Paypal UI cancel URL not found in env");
    }

    public CreatePaymentResponse createPayment(@Nonnull final CreatePaymentRequest createPaymentRequest) {
        final String traceId = createPaymentRequest.userId() + "-" + System.currentTimeMillis();

        LOG.info("{} create payment received for amount={}", traceId, createPaymentRequest.amount());

        final var orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        final var amountBreakdown = new AmountWithBreakdown()
                .currencyCode(createPaymentRequest.currency())
                .value(String.valueOf(createPaymentRequest.amount()));

        final var purchaseUnitRequest = new PurchaseUnitRequest()
                .amountWithBreakdown(amountBreakdown);
        orderRequest.purchaseUnits(List.of(purchaseUnitRequest));

        final var applicationContext = new ApplicationContext()
                .returnUrl(uiCaptureUrl)
                .cancelUrl(uiCancelUrl);
        orderRequest.applicationContext(applicationContext);

        final var ordersCreateRequest = new OrdersCreateRequest().requestBody(orderRequest);

        final HttpResponse<Order> orderHttpResponse;

        try {
            orderHttpResponse = httpClient.execute(ordersCreateRequest);
        } catch (IOException e) {
            LOG.error("{} error creating payment order due to gateway error: {}", traceId, e.getMessage(), e);
            throw new PaymentException(500, null, "Payment Gateway Error. Please try again");
        }

        final Order order = orderHttpResponse.result();

        final var redirectUrl = order.links()
                .stream()
                .filter(link -> "approve".equals(link.rel()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No 'approve' link found in order"))
                .href();

        final String orderId = order.id();

        savePaymentInDatabase(createPaymentRequest, traceId, orderId, PAYMENT_GATEWAY_NAME);

        return new CreatePaymentResponse(
                traceId,
                "Payment created successfully",
                HttpStatus.CREATED.value(),
                orderId,
                redirectUrl,
                null
        );
    }

    public CompletePaymentResponse completePayment(@NotNull final CompletePaymentRequest completePaymentRequest) {
        final String paymentId = completePaymentRequest.paymentId();
        String traceId = "";

        final PaymentDetail paymentDetail = getPaymentDetailByPaymentId(paymentId);

        traceId = paymentDetail.getTraceId();

        LOG.info("{} complete payment request received for paymentId={}", traceId, paymentId);

        validatePaymentStatusForPaymentCompletion(paymentDetail);

        final var ordersCaptureRequest = new OrdersCaptureRequest(paymentId);
        final HttpResponse<Order> httpResponse;

        try {
            httpResponse = httpClient.execute(ordersCaptureRequest);
        } catch (IOException e) {
            LOG.error("{} error completing payment order due to gateway error: {}", traceId, e.getMessage(), e);
            throw new PaymentException(500, null, "Gateway error completing payment order. Please try again");
        }

        final var order = httpResponse.result();

        if ("COMPLETED".equalsIgnoreCase(order.status())) {
            LOG.info("{} setting payment status as COMPLETED with id={}", traceId, paymentId);
            paymentDetail.setPaymentStatus(PaymentStatus.COMPLETED);
            paymentDetail.setPaymentCompletedAt(Instant.now());
            updatePaymentDetails(paymentDetail);
            publishPaymentEvent(paymentDetail);
            return new CompletePaymentResponse(
                    HttpStatus.OK.value(),
                    paymentId,
                    paymentDetail.getPaymentStatus().name(),
                    "Payment completed successfully"
            );
        } else {
            LOG.error("{} complete payment failed id={}", traceId, paymentId);
            paymentDetail.setPaymentStatus(PaymentStatus.FAILED);
            updatePaymentDetails(paymentDetail);
            paymentDetail.setPaymentCompletedAt(Instant.now());
            return new CompletePaymentResponse(
                    HttpStatus.OK.value(),
                    paymentId,
                    PaymentStatus.FAILED.name(),
                    "Failed to complete payment. Please try again!"
            );
        }
    }

    @Override
    public VerifyPaymentResponse verifyPayment(@NotNull final VerifyPaymentRequest verifyPaymentRequest) {
        // TODO implement
        return null;
    }

    @Override
    public String getPaymentGatewayName() {
        return PAYMENT_GATEWAY_NAME;
    }

}
