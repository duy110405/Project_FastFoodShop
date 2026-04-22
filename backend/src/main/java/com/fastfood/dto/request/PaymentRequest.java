package com.fastfood.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private String orderId;
    private String customerPhone;
    private String paymentMethod; // "CASH" hoặc "TRANSFER"
}
