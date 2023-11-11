package com.akgarg.paymentservice.stripe;

import com.akgarg.paymentservice.database.DatabaseService;
import com.akgarg.paymentservice.exception.PaymentException;
import com.akgarg.paymentservice.payment.PaymentDetail;
import com.akgarg.paymentservice.payment.PaymentStatus;
import com.akgarg.paymentservice.paymentgameway.PaymentGatewayService;
import com.akgarg.paymentservice.request.MakePaymentRequest;
import com.akgarg.paymentservice.request.VerifyPaymentRequest;
import com.akgarg.paymentservice.response.PaymentFailureResponse;
import com.akgarg.paymentservice.response.PaymentResponse;
import com.akgarg.paymentservice.response.PaymentSuccessResponse;
import com.akgarg.paymentservice.response.VerifyPaymentResponse;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Payment gateway service for stripe
 *
 * @author Akhilesh Garg
 * @since 11/09/23
 */
@Service
public class StripePaymentGatewayService implements PaymentGatewayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripePaymentGatewayService.class);
    private static final String PAYMENT_GATEWAY = "stripe";

    private final DatabaseService databaseService;
    private final String frontendRedirectUrl;

    public StripePaymentGatewayService(final DatabaseService databaseService) {
        this.databaseService = databaseService;
        final Dotenv dotenv = Dotenv.load();
        Stripe.apiKey = dotenv.get("STRIPE_API_KEY");
        this.frontendRedirectUrl = dotenv.get("FRONTEND_PAYMENT_REDIRECT_PAGE_URL");
    }

    @Override
    public PaymentResponse makePayment(final MakePaymentRequest paymentRequest) {
        LOGGER.info("Received make payment request: {}", paymentRequest);

        try {
            final var automaticPaymentMethods = PaymentIntentCreateParams.AutomaticPaymentMethods
                    .builder()
                    .setEnabled(false)
                    .build();

            final var paymentParams = new PaymentIntentCreateParams.Builder()
                    .setCurrency(paymentRequest.currency())
                    .setAmount(paymentRequest.amount())
                    .setDescription(paymentRequest.paymentDescription())
                    .setAutomaticPaymentMethods(automaticPaymentMethods)
                    .addPaymentMethodType(paymentRequest.paymentMethod())
                    .setReceiptEmail(paymentRequest.receiptEmail())
                    .build();

            final var paymentIntent = PaymentIntent.create(paymentParams);

            final var paymentDetail = getPaymentDetail(paymentRequest, paymentIntent);

            final boolean paymentDetailSaved = databaseService.savePaymentDetails(paymentDetail);

            if (!paymentDetailSaved) {
                LOGGER.error("Payment details not saved in database");
                return new PaymentFailureResponse(500, "Error processing payment", null);
            }

            return new PaymentSuccessResponse(paymentIntent.getClientSecret(), 201);

        } catch (Exception e) {
            LOGGER.error(
                    "{} occurred while creating payment intent: {}",
                    e.getClass().getName(),
                    e.getMessage()
            );
            return new PaymentFailureResponse(500, e.getMessage(), null);
        }
    }

    @Override
    public VerifyPaymentResponse verifyPayment(final VerifyPaymentRequest verifyPaymentRequest) {
        LOGGER.info("Received verify payment request: {}", verifyPaymentRequest);

        final PaymentDetail paymentDetail = databaseService.getPaymentDetails(verifyPaymentRequest.paymentId())
                .orElseThrow(() -> new PaymentException(
                        404,
                        null,
                        "Payment details not found with id: " + verifyPaymentRequest.paymentId()
                ));

        LOGGER.info("{}: payment status found -> {}", paymentDetail.getPaymentId(), paymentDetail.getPaymentStatus());

        return new VerifyPaymentResponse(
                200,
                "Payment verified successfully",
                paymentDetail.getPaymentStatus()
        );
    }

    @Override
    public String getPaymentGateway() {
        return PAYMENT_GATEWAY;
    }

    public StripeCallbackResponse processCallback(final Map<String, Object> stripeCallbackParams) {
        LOGGER.info("Stripe callback received: {}", stripeCallbackParams);

        final PaymentStatus paymentStatus = getPaymentStatus(stripeCallbackParams.get("redirect_status").toString());

        final String redirectUri = frontendRedirectUrl +
                "?payment_id=" + stripeCallbackParams.get("payment_intent") +
                "&payment_status=" + paymentStatus.value() +
                "&payment_gateway=" + PAYMENT_GATEWAY;

        return new StripeCallbackResponse(
                302,
                redirectUri
        );
    }

    private PaymentDetail getPaymentDetail(
            final MakePaymentRequest paymentRequest,
            final PaymentIntent paymentIntent
    ) {
        final var paymentDetail = new PaymentDetail();
        paymentDetail.setPaymentId(paymentIntent.getId());
        paymentDetail.setUserId(paymentRequest.userId());
        paymentDetail.setAmount(paymentRequest.amount());
        paymentDetail.setPaymentStatus(PaymentStatus.PENDING.value());
        paymentDetail.setCurrency(paymentRequest.currency());
        paymentDetail.setPaymentMethod(paymentRequest.paymentMethod());
        paymentDetail.setPaymentCreatedAt(paymentIntent.getCreated() * 1000);
        paymentDetail.setPaymentGateway(PAYMENT_GATEWAY);
        return paymentDetail;
    }

    private PaymentStatus getPaymentStatus(final String paymentStatus) {
        return switch (paymentStatus) {
            case "succeeded" -> PaymentStatus.SUCCESS;
            case "processing" -> PaymentStatus.PROCESSING;
            case "requires_confirmation" -> PaymentStatus.CONFIRMATION_REQUIRED;
            default -> PaymentStatus.FAILED;
        };
    }

}
