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
public class StockIssueResponse {
    String idStockIssue;
    String stockRequestId;
    LocalDate issueDate;
    String issuedBy;
    String note;
    List<StockMovementDetailResponse> details;
}
