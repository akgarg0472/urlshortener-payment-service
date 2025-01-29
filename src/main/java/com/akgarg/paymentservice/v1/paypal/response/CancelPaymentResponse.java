package com.akgarg.paymentservice.v1.paypal.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CancelPaymentResponse {

    @JsonIgnore
    private int statusCode;

    private boolean success;
    private String message;

}
