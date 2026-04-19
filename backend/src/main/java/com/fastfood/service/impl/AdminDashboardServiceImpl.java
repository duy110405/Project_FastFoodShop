package com.fastfood.service.impl;

import com.fastfood.dto.response.AdminDashboardResponse;
import com.fastfood.dto.response.TopOrderedFoodResponse;
import com.fastfood.repository.OrderDetailRepository;
import com.fastfood.repository.OrderRepository;
import com.fastfood.repository.SalesInvoiceRepository;
import com.fastfood.service.IAdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private static final int DEFAULT_TOP_N = 6;

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate, int topN) {
        LocalDate resolvedFrom = fromDate != null ? fromDate : LocalDate.now();
        LocalDate resolvedTo = toDate != null ? toDate : resolvedFrom;

        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new IllegalArgumentException("fromDate không được lớn hơn toDate");
        }

        int limitedTopN = topN > 0 ? topN : DEFAULT_TOP_N;

        LocalDateTime fromDateTime = resolvedFrom.atStartOfDay();
        LocalDateTime toDateTime = resolvedTo.plusDays(1).atStartOfDay();

        BigDecimal revenue = safeBigDecimal(salesInvoiceRepository.sumRevenueInRange(fromDateTime, toDateTime));
        long invoiceCount = salesInvoiceRepository.countInvoicesInRange(fromDateTime, toDateTime);

        long orderCount = orderRepository.countOrdersInRange(fromDateTime, toDateTime);

        BigDecimal totalImportCost = safeBigDecimal(orderDetailRepository.sumProductionCostOfPaidOrdersInRange(fromDateTime, toDateTime));

        BigDecimal profit = revenue.subtract(totalImportCost);
        BigDecimal averageRevenue = divideOrZero(revenue, invoiceCount);
        BigDecimal averageImportCost = divideOrZero(totalImportCost, invoiceCount);

        List<TopOrderedFoodResponse> topOrderedFoods = orderDetailRepository
                .findTopOrderedFoodsInRange(fromDateTime, toDateTime, PageRequest.of(0, limitedTopN))
                .stream()
                .map(this::mapTopFood)
                .toList();

        return AdminDashboardResponse.builder()
                .fromDate(resolvedFrom)
                .toDate(resolvedTo)
                .profit(profit)
                .orderCount(orderCount)
                .averageImportCost(averageImportCost)
                .averageRevenue(averageRevenue)
                .topOrderedFoods(topOrderedFoods)
                .build();
    }

    private TopOrderedFoodResponse mapTopFood(Object[] row) {
        return TopOrderedFoodResponse.builder()
                .idFood((String) row[0])
                .foodName((String) row[1])
                .imageUrlFood((String) row[2])
                .quantityOrdered(((Number) row[3]).longValue())
                .totalRevenue(toBigDecimal(row[4]))
                .build();
    }

    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private BigDecimal divideOrZero(BigDecimal value, long divisor) {
        if (divisor <= 0) {
            return BigDecimal.ZERO;
        }
        return value.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }
}


