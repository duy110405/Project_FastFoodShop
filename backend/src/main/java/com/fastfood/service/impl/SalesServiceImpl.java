package com.fastfood.service.impl;

import com.fastfood.dto.request.OrderRequest;
import com.fastfood.dto.request.PaymentRequest;
import com.fastfood.entity.catalog.Food;
import com.fastfood.entity.catalog.FoodIngredient;
import com.fastfood.entity.catalog.Ingredient;
import com.fastfood.entity.transaction.Order;
import com.fastfood.entity.transaction.OrderDetail;
import com.fastfood.entity.transaction.SalesInvoice;
import com.fastfood.repository.FoodRepository;
import com.fastfood.repository.IngredientRepository;
import com.fastfood.repository.OrderRepository;
import com.fastfood.repository.SalesInvoiceRepository;
import com.fastfood.service.ISalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements ISalesService {

    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    public String generateNextOrderId() {
        String maxId = orderRepository.findMaxIdOrder();
        String prefix = "ORD";
        if (maxId == null || maxId.isEmpty()) return prefix + "001";
        int nextNum = Integer.parseInt(maxId.substring(prefix.length())) + 1;
        return prefix + String.format("%03d", nextNum);
    }

    @Override
    public String generateNextInvoiceId() {
        String maxId = salesInvoiceRepository.findMaxIdInvoice();
        String prefix = "INV";
        if (maxId == null || maxId.isEmpty()) return prefix + "001";
        int nextNum = Integer.parseInt(maxId.substring(prefix.length())) + 1;
        return prefix + String.format("%03d", nextNum);
    }

    @Override
    public Set<String> getOccupiedTableNumbers() {
        return orderRepository.findByStatus("PENDING")
                .stream()
                .map(Order::getTableNumber)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public Order placeOrder(OrderRequest request) {
        Order order = new Order();
        order.setIdOrder(generateNextOrderId());
        order.setTableNumber(request.getTableNumber());
        order.setCustomerName(request.getCustomerName());
        order.setOrderTime(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setCreatedBy(request.getCreatedBy());

        var details = request.getItems().stream().map(item -> {
            Food food = foodRepository.findById(item.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
            for (FoodIngredient recipe : food.getFoodIngredients()) {
                Ingredient ingredient = recipe.getIngredient();

                //Chống Null cho định lượng
                BigDecimal qtyUsed = recipe.getQuantityUsed() != null ? recipe.getQuantityUsed() : BigDecimal.ZERO;

                // Tổng trừ = Định lượng * Số lượng món
                BigDecimal totalDeducted = qtyUsed.multiply(BigDecimal.valueOf(item.getQuantity()));

                //Chống Null cho tồn kho
                BigDecimal currentStock = ingredient.getQuantityStock() != null ? ingredient.getQuantityStock() : BigDecimal.ZERO;

                //  CHẶN ĐỨNG LỖI ÂM KHO
                if (currentStock.compareTo(totalDeducted) < 0) {
                    // Trả về lỗi 400 kèm từ khóa để Frontend bắt Pop-up
                    throw new IllegalArgumentException("HẾT_HÀNG|" + food.getFoodName());
                }

                // Đủ điều kiện thì mới trừ kho
                ingredient.setQuantityStock(currentStock.subtract(totalDeducted));
                ingredientRepository.save(ingredient); // Lưu lại kho mới
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setFood(food);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(food.getUnitPrice()); 
            detail.setStatus("PENDING");
            return detail;
        }).collect(Collectors.toList());

        order.setOrderDetails(details);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public SalesInvoice processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if ("PAID".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng này đã được thanh toán");
        }

        BigDecimal totalAmount = order.getOrderDetails().stream()
                .map(detail -> detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setIdInvoice(generateNextInvoiceId());
        invoice.setOrder(order);
        invoice.setCustomerPhone(request.getCustomerPhone());
        invoice.setPaymentDate(LocalDateTime.now());
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setTotalAmount(totalAmount);
        
        salesInvoiceRepository.save(invoice);

        order.setStatus("PAID");
        orderRepository.save(order);
        
        return invoice;
    }
}
