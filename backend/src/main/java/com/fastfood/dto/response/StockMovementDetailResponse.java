package com.fastfood.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMovementDetailResponse {
    String ingredientId;
    String ingredientName;
    Integer quantity;
    BigDecimal unitPrice;
    LocalDate expiryDate;
}
