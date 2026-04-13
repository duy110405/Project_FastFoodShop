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
public class StockRequestResponse {
    String idStockRequest;
    LocalDate requestDate;
    String requestedBy;
    String status;
    String note;
    List<StockMovementDetailResponse> details;
}
