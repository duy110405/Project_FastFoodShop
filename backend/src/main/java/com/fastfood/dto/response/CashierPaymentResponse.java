package com.fastfood.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashierPaymentResponse {
    private String invoiceId;
    private String orderId;
    private String tableNumber;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private LocalDateTime paymentDate;
}

