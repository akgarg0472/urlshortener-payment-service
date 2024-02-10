package com.akgarg.paymentservice.v1.razorpay;

import com.akgarg.paymentservice.db.DatabaseService;
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
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class RazorpayService extends AbstractPaymentService {

    private static final String PAYMENT_GATEWAY_NAME = "Razorpay";
    private static final Logger LOG = LogManager.getLogger(RazorpayService.class);
    private final RazorpayClient razorpayClient;

    public RazorpayService(
            final RazorpayClient razorpayClient,
            final DatabaseService databaseService
    ) {
        super(databaseService);
        this.razorpayClient = razorpayClient;
    }

    @Override
    public CreatePaymentResponse createPayment(@NotNull final CreatePaymentRequest createPaymentRequest) {
        final String traceId = createPaymentRequest.userId() + "-" + System.currentTimeMillis();

        LOG.info("{} create payment received for amount={}", traceId, createPaymentRequest.amount());

        final var orderRequest = new JSONObject();
        orderRequest.put("amount", createPaymentRequest.amount());
        orderRequest.put("currency", createPaymentRequest.currency());
        orderRequest.put("receipt", UUID.randomUUID().toString().replace("-", ""));
        orderRequest.put("notes", getOrderNotes(createPaymentRequest));

        final Order order;

        try {
            order = razorpayClient.orders.create(orderRequest);
        } catch (RazorpayException e) {
            LOG.error("{} error creating payment order due to gateway error: {}", traceId, e.getMessage(), e);
            throw new PaymentException(500, null, "Gateway error creating payment order. Please try again");
        }

        final String orderId = order.toJson().getString("id");

        savePaymentInDatabase(createPaymentRequest, traceId, orderId, PAYMENT_GATEWAY_NAME);

        return new CreatePaymentResponse(
                traceId,
                "Payment order created successfully",
                HttpStatus.CREATED.value(),
                orderId,
                null,
                null
        );
    }

    @Override
    public CompletePaymentResponse completePayment(@NotNull final CompletePaymentRequest completePaymentRequest) {
        final String paymentId = completePaymentRequest.paymentId();
        String traceId = "";

        final PaymentDetail paymentDetail = getPaymentDetailByPaymentId(paymentId);

        traceId = paymentDetail.getTraceId();

        LOG.info("{} complete payment request received for paymentId={}", traceId, paymentId);

        validatePaymentStatusForPaymentCompletion(paymentDetail);

        LOG.info("{} marking payment status as COMPLETED with id={}", traceId, paymentId);

        paymentDetail.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentDetail.setPaymentCompletedAt(Instant.now());
        updatePaymentDetails(paymentDetail);

        return new CompletePaymentResponse(
                HttpStatus.OK.value(),
                paymentId,
                paymentDetail.getPaymentStatus().name(),
                "Payment completed successfully"
        );
    }

    @Override
    public VerifyPaymentResponse verifyPayment(@NotNull final VerifyPaymentRequest verifyPaymentRequest) {
        return null;
    }

    @Override
    public String getPaymentGatewayName() {
        return PAYMENT_GATEWAY_NAME;
    }

    private Map<String, Object> getOrderNotes(final CreatePaymentRequest request) {
        return Map.of("userId", request.userId(),
                      "packId", request.packId(),
                      "timestamp", System.currentTimeMillis()
        );
    }

}
