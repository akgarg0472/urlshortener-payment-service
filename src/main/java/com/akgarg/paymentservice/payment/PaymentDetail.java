package com.akgarg.paymentservice.payment;

/**
 * @author Akhilesh Garg
 * @since 11/11/23
 */
public class PaymentDetail {

    private String paymentId;  // primary key
    private String userId;  // indexed field
    private Long amount;
    private String paymentStatus;
    private String currency;
    private String paymentMethod;
    private Long paymentCreatedAt;
    private Long paymentCompletedAt;
    private String paymentGateway;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(final String paymentId) {
        this.paymentId = paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(final Long amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(final String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getPaymentCreatedAt() {
        return paymentCreatedAt;
    }

    public void setPaymentCreatedAt(final Long paymentCreatedAt) {
        this.paymentCreatedAt = paymentCreatedAt;
    }

    public Long getPaymentCompletedAt() {
        return paymentCompletedAt;
    }

    public void setPaymentCompletedAt(final Long paymentCompletedAt) {
        this.paymentCompletedAt = paymentCompletedAt;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(final String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @Override
    public String toString() {
        return "PaymentDetail{" +
                "paymentId='" + paymentId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", currency='" + currency + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentCreatedAt=" + paymentCreatedAt +
                ", paymentCompletedAt=" + paymentCompletedAt +
                ", paymentGateway='" + paymentGateway + '\'' +
                '}';
    }

}
