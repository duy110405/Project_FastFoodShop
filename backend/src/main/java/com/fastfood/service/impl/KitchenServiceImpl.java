package com.fastfood.service.impl;

import com.fastfood.dto.response.KitchenFoodPendingResponse;
import com.fastfood.dto.response.KitchenOrderItemResponse;
import com.fastfood.dto.response.KitchenTableOrderResponse;
import com.fastfood.entity.catalog.Food;
import com.fastfood.entity.catalog.FoodIngredient;
import com.fastfood.entity.transaction.Order;
import com.fastfood.entity.transaction.OrderDetail;
import com.fastfood.repository.FoodRepository;
import com.fastfood.repository.OrderDetailRepository;
import com.fastfood.repository.OrderRepository;
import com.fastfood.service.IKitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KitchenServiceImpl implements IKitchenService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final FoodRepository foodRepository;

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTableOrderResponse> getPendingOrdersByTable() {
        List<Order> pendingOrders = orderRepository.findPendingOrdersWithDetails();
        Map<String, KitchenTableOrderResponse> tableMap = new LinkedHashMap<>();

        for (Order order : pendingOrders) {
            KitchenTableOrderResponse tableResponse = tableMap.computeIfAbsent(order.getTableNumber(), tableNumber -> {
                KitchenTableOrderResponse response = new KitchenTableOrderResponse();
                response.setTableNumber(tableNumber);
                response.setFirstOrderTime(order.getOrderTime());
                return response;
            });

            if (order.getOrderTime() != null &&
                    (tableResponse.getFirstOrderTime() == null || order.getOrderTime().isBefore(tableResponse.getFirstOrderTime()))) {
                tableResponse.setFirstOrderTime(order.getOrderTime());
            }

            if (order.getOrderDetails() == null) {
                continue;
            }

            for (OrderDetail detail : order.getOrderDetails()) {
                if (!"PENDING".equalsIgnoreCase(detail.getStatus()) || detail.getFood() == null) {
                    continue;
                }

                KitchenOrderItemResponse itemResponse = new KitchenOrderItemResponse();
                itemResponse.setOrderDetailId(detail.getId());
                itemResponse.setOrderId(order.getIdOrder());
                itemResponse.setFoodId(detail.getFood().getIdFood());
                itemResponse.setFoodName(detail.getFood().getFoodName());
                itemResponse.setImageUrlFood(detail.getFood().getImageUrlFood());
                itemResponse.setQuantity(detail.getQuantity());
                tableResponse.getItems().add(itemResponse);
            }
        }

        LocalDateTime now = LocalDateTime.now();

        return tableMap.values().stream()
                .filter(table -> !table.getItems().isEmpty())
                .peek(table -> {
                    if (table.getFirstOrderTime() == null) {
                        table.setElapsedMinutes(0);
                        return;
                    }
                    long elapsed = Duration.between(table.getFirstOrderTime(), now).toMinutes();
                    table.setElapsedMinutes(Math.max(elapsed, 0));
                })
                .sorted(Comparator.comparing(KitchenTableOrderResponse::getFirstOrderTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenFoodPendingResponse> getRemainingFoodSummary() {
        return foodRepository.findAllWithIngredients().stream()
                .map(this::toRemainingFoodResponse)
                .filter(item -> item.getRemainingQuantity() > 0)
                .sorted(Comparator.comparing(KitchenFoodPendingResponse::getRemainingQuantity).reversed())
                .toList();
    }

    private KitchenFoodPendingResponse toRemainingFoodResponse(Food food) {
        KitchenFoodPendingResponse response = new KitchenFoodPendingResponse();
        response.setFoodId(food.getIdFood());
        response.setFoodName(food.getFoodName());
        response.setImageUrlFood(food.getImageUrlFood());
        response.setRemainingQuantity(calculateRemainingQuantity(food));
        return response;
    }

    private int calculateRemainingQuantity(Food food) {
        if (food.getFoodIngredients() == null || food.getFoodIngredients().isEmpty()) {
            return 0;
        }

        int minPortions = Integer.MAX_VALUE;

        for (FoodIngredient recipe : food.getFoodIngredients()) {
            BigDecimal quantityUsed = recipe.getQuantityUsed() == null ? BigDecimal.ZERO : recipe.getQuantityUsed();
            BigDecimal stock = (recipe.getIngredient() == null || recipe.getIngredient().getQuantityStock() == null)
                    ? BigDecimal.ZERO
                    : recipe.getIngredient().getQuantityStock();

            if (quantityUsed.compareTo(BigDecimal.ZERO) <= 0) {
                return 0;
            }

            int portionsByIngredient = stock.divideToIntegralValue(quantityUsed).intValue();
            minPortions = Math.min(minPortions, Math.max(portionsByIngredient, 0));
        }

        return minPortions == Integer.MAX_VALUE ? 0 : minPortions;
    }

    @Override
    @Transactional
    public void markOrderItemServed(Long orderDetailId) {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng"));

        if (!"PENDING".equalsIgnoreCase(orderDetail.getStatus())) {
            return;
        }

        orderDetail.setStatus("SERVED");
        orderDetailRepository.save(orderDetail);
    }
}


