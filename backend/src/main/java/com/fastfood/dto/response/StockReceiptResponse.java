package com.fastfood.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockReceiptResponse {
    String idStockReceipt;
    String supplierId;
    String supplierName;
    LocalDate receiptDate;
    String createdBy;
    String note;
    List<StockMovementDetailResponse> details;
}
