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
import java.time.LocalTime;
import java.util.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
@Service
@RequiredArgsConstructor
public class KitchenServiceImpl implements IKitchenService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final FoodRepository foodRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ========================================================
    // 1. LẤY DANH SÁCH MÓN ĐANG CHỜ (PENDING) - CHỈ TRONG NGÀY
    // ========================================================
    @Override
    @Transactional(readOnly = true)
    public List<KitchenTableOrderResponse> getPendingOrdersByTable() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        // Lấy danh sách đơn hàng trong ngày hôm nay
        List<Order> pendingOrders = orderRepository.findOrdersWithDetailsByDate(startOfDay, endOfDay);
        return groupOrdersByTable(pendingOrders, "PENDING");
    }

    // ========================================================
    // 2. LẤY DANH SÁCH MÓN ĐÃ XONG (SERVED) - CHỈ TRONG NGÀY
    // ========================================================
    @Override
    @Transactional(readOnly = true)
    public List<KitchenTableOrderResponse> getCompletedOrders() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        // Lấy danh sách đơn hàng trong ngày hôm nay thay vì findAll()
        List<Order> allOrders = orderRepository.findOrdersWithDetailsByDate(startOfDay, endOfDay); 
        
        return groupOrdersByTable(allOrders, "SERVED");
    }

    /**
     * Hàm dùng chung để nhóm các OrderDetail theo số bàn dựa trên trạng thái (Status)
     */
    private List<KitchenTableOrderResponse> groupOrdersByTable(List<Order> orders, String statusFilter) {
        Map<String, KitchenTableOrderResponse> tableMap = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (Order order : orders) {
            if ("PENDING".equals(statusFilter) && !"PAID".equalsIgnoreCase(order.getStatus())) {
                continue;
            }
            if (order.getOrderDetails() == null) continue;

            for (OrderDetail detail : order.getOrderDetails()) {
                // Lọc theo trạng thái truyền vào (PENDING hoặc SERVED)
                if (!statusFilter.equalsIgnoreCase(detail.getStatus()) || detail.getFood() == null) {
                    continue;
                }

                KitchenTableOrderResponse tableResponse = tableMap.computeIfAbsent(order.getTableNumber(), tableNumber -> {
                    KitchenTableOrderResponse response = new KitchenTableOrderResponse();
                    response.setTableNumber(tableNumber);
                    response.setFirstOrderTime(order.getOrderTime());
                    response.setItems(new ArrayList<>());
                    return response;
                });

                // Cập nhật thời gian đơn hàng đầu tiên để tính elapsed time
                if (order.getOrderTime() != null &&
                        (tableResponse.getFirstOrderTime() == null || order.getOrderTime().isBefore(tableResponse.getFirstOrderTime()))) {
                    tableResponse.setFirstOrderTime(order.getOrderTime());
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

        return tableMap.values().stream()
                .filter(table -> !table.getItems().isEmpty())
                .peek(table -> {
                    if (table.getFirstOrderTime() != null) {
                        long elapsed = Duration.between(table.getFirstOrderTime(), now).toMinutes();
                        table.setElapsedMinutes(Math.max(elapsed, 0));
                    } else {
                        table.setElapsedMinutes(0);
                    }
                })
                // Sắp xếp: PENDING thì món cũ nhất lên đầu, SERVED thì món mới nhất lên đầu (lịch sử)
                .sorted((a, b) -> {
                    if ("SERVED".equals(statusFilter)) {
                        return b.getFirstOrderTime().compareTo(a.getFirstOrderTime());
                    }
                    return a.getFirstOrderTime().compareTo(b.getFirstOrderTime());
                })
                .toList();
    }

    // ========================================================
    // 3. TỔNG HỢP MÓN CÒN TRONG KHO (REMAINING)
    // ========================================================
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

            if (quantityUsed.compareTo(BigDecimal.ZERO) <= 0) return 0;

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

        // 1. Đánh dấu món này đã nấu xong
        orderDetail.setStatus("SERVED");
        orderDetailRepository.save(orderDetail);

        // =========================================================
        // 2. LOGIC TỰ ĐỘNG CHUYỂN MÀU BÀN TỪ VÀNG SANG ĐỎ
        // =========================================================
        Order order = orderDetail.getOrder();

        // Kiểm tra xem TẤT CẢ các món trong bàn này đã nấu xong hết chưa?
        boolean isAllServed = order.getOrderDetails().stream()
                .allMatch(item -> "SERVED".equalsIgnoreCase(item.getStatus()));

        if (isAllServed) {
            // Nếu tất cả đã xong -> Chốt Đơn hàng thành SERVED (Bàn đỏ)
            order.setStatus("SERVED");
            orderRepository.save(order);

            // Bắn tín hiệu sang màn hình Thu Ngân để nó tự chớp màu Đỏ ngay lập tức
            messagingTemplate.convertAndSend("/topic/cashier", "TABLE_SERVED");
        }
    }
}