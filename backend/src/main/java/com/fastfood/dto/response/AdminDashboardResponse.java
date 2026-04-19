package com.fastfood.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal profit;
    private long orderCount;
    private BigDecimal averageImportCost;
    private BigDecimal averageRevenue;
    private List<TopOrderedFoodResponse> topOrderedFoods;
}

