package com.fastfood.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockIssueRequest {
    String stockRequestId;
    LocalDate issueDate;
    String issuedBy;
    String note;
    List<StockIssueDetailRequest> details;
}
