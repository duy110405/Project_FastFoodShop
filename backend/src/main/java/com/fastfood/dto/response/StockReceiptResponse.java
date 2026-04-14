package com.fastfood.dto.response;

import java.time.LocalDate;
import java.util.List;

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
public class StockReceiptResponse {
    String idReceipt;
    LocalDate receiptDate;
    String supplierName;
    String status;
    String createdBy;
    List<StockMovementDetailResponse> details;
}