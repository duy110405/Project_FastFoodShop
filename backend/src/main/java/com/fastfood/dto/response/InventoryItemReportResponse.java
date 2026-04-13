package com.fastfood.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryItemReportResponse {
    String ingredientId;
    String ingredientName;
    String unit;
    Integer currentStock;
    Integer minStock;
    boolean lowStock;
}
