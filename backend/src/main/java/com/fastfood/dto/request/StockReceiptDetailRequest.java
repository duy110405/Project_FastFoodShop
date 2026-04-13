package com.fastfood.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockReceiptDetailRequest {
    String ingredientId;
    Integer quantity;
    BigDecimal unitPrice;
    LocalDate expiryDate;
}
