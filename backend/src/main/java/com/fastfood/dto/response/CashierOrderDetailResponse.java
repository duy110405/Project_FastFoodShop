package com.fastfood.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashierOrderDetailResponse {
    private String orderId;
    private String tableNumber;
    private String customerName;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private List<CashierOrderItemResponse> items;
}

