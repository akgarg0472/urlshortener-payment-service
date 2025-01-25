package com.akgarg.paymentservice.v1.paypal.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Getter
@Setter
@Builder
@ToString
public final class CreateOrderResponse {

    @JsonProperty("status_code")
    private int statusCode;

    @JsonProperty("trace_id")
    private String traceId;

    @JsonProperty("message")
    private String message;

    @JsonProperty("payment_id")
    private String orderId;

    @JsonProperty("approval_url")
    private String approvalUrl;

    @JsonProperty("errors")
    private Collection<String> errors;

}
