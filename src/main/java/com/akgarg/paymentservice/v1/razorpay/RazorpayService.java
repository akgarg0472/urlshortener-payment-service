package com.akgarg.paymentservice.v1.razorpay;

import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.payment.PaymentService;
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
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class RazorpayService implements PaymentService {

    private static final String PAYMENT_GATEWAY_NAME = "Razorpay";
    private static final Logger LOG = LogManager.getLogger(RazorpayService.class);
    private final RazorpayClient razorpayClient;

    RazorpayService(final RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    @Override
    public CreatePaymentResponse createPayment(final CreatePaymentRequest createPaymentRequest) {
        final String traceId = createPaymentRequest.userId() + "-" + System.currentTimeMillis();

        LOG.info("{} create payment received for amount={}", traceId, createPaymentRequest.amount());

        final var orderRequest = new JSONObject();
        orderRequest.put("amount", createPaymentRequest.amount());
        orderRequest.put("currency", createPaymentRequest.currency());
        orderRequest.put("receipt", UUID.randomUUID().toString().replace("-", ""));
        orderRequest.put("notes", getOrderNotes(createPaymentRequest));

        try {
            final Order order = razorpayClient.orders.create(orderRequest);
            final JSONObject orderJson = order.toJson();
            LOG.info("{} order json: {}", traceId, orderJson);

            return new CreatePaymentResponse(
                    traceId,
                    "Payment order created successfully",
                    HttpStatus.CREATED.value(),
                    orderJson.getString("id"),
                    null,
                    null
            );
        } catch (RazorpayException e) {
            LOG.error("{} create payment request failed due to gateway error: {}", traceId, e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("{} create payment request failed due to server error: {}", traceId, e.getMessage(), e);
        }

        throw new PaymentException(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, "Internal Server Error");
    }

    @Override
    public CompletePaymentResponse completePayment(final CompletePaymentRequest completePaymentRequest) {
        return null;
    }

    @Override
    public VerifyPaymentResponse verifyPayment(final VerifyPaymentRequest verifyPaymentRequest) {
        return null;
    }

    @Override
    public String getPaymentGatewayServiceName() {
        return PAYMENT_GATEWAY_NAME;
    }

    private Map<String, Object> getOrderNotes(final CreatePaymentRequest request) {
        return Map.of("userId", request.userId(),
                      "packId", request.packId(),
                      "timestamp", System.currentTimeMillis()
        );
    }

}
