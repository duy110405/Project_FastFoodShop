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
public class StockRequestRequest {
    LocalDate requestDate;
    String requestedBy;
    String note;
    List<StockRequestDetailRequest> details;
}
