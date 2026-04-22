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
public class InventoryItemReportResponse {
    String ingredientId;
    String imageUrlIngredient;
    String ingredientName;
    String unit;
    BigDecimal importPrice;
    BigDecimal currentStock;
    BigDecimal minStock;
    boolean lowStock;
}