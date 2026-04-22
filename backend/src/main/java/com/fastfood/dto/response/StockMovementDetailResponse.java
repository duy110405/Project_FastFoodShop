package com.fastfood.dto.response;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMovementDetailResponse {
    String ingredientId;
    String imageUrlIngredient;
    String ingredientName;
    BigDecimal quantityImport;
    BigDecimal importPrice;
}