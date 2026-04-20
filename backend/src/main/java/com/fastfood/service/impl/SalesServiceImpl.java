package com.fastfood.service.impl;

import com.fastfood.dto.request.OrderRequest;
import com.fastfood.dto.request.PaymentRequest;
import com.fastfood.dto.response.CashierOrderDetailResponse;
import com.fastfood.dto.response.CashierOrderItemResponse;
import com.fastfood.dto.response.CashierPaymentResponse;
import com.fastfood.dto.response.CashierTableStatusResponse;
import com.fastfood.entity.catalog.Food;
import com.fastfood.entity.catalog.FoodIngredient;
import com.fastfood.entity.catalog.Ingredient;
import com.fastfood.entity.system.User;
import com.fastfood.entity.transaction.Order;
import com.fastfood.entity.transaction.OrderDetail;
import com.fastfood.entity.transaction.SalesInvoice;
import com.fastfood.repository.FoodRepository;
import com.fastfood.repository.IngredientRepository;
import com.fastfood.repository.OrderRepository;
import com.fastfood.repository.SalesInvoiceRepository;
import com.fastfood.repository.UserRepository;
import com.fastfood.service.ISalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements ISalesService {

    private static final Pattern TABLE_CODE_PATTERN = Pattern.compile("[A-Z]\\d{2}");

    // CÁC THƯ VIỆN ĐƯỢC INJECT BẰNG "private final"
    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final IngredientRepository ingredientRepository;
    
    // ĐÃ THÊM 2 DÒNG NÀY ĐỂ FIX LỖI "CANNOT BE RESOLVED"
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
                .map(this::normalizeTableNumber)
                .filter(table -> table != null && !table.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashierTableStatusResponse> getTableStatuses() {
        Set<String> configuredTables = getConfiguredTableCodesFromAccounts();

        // Lấy tất cả các đơn ĐANG HOẠT ĐỘNG
        List<Order> allActiveOrders = new ArrayList<>();
        allActiveOrders.addAll(orderRepository.findByStatus("PENDING"));
        allActiveOrders.addAll(orderRepository.findByStatus("PAID"));
        allActiveOrders.addAll(orderRepository.findByStatus("SERVED"));

        // CHỈ LẤY ĐƠN MỚI NHẤT CỦA MỖI BÀN
        Map<String, Order> latestOrderPerTable = new HashMap<>();
        for (Order o : allActiveOrders) {
            String table = normalizeTableNumber(o.getTableNumber());
            if (!latestOrderPerTable.containsKey(table) ||
                    o.getOrderTime().isAfter(latestOrderPerTable.get(table).getOrderTime())) {
                latestOrderPerTable.put(table, o);
            }
        }

        List<String> sortedTables = new ArrayList<>(configuredTables);
        sortedTables.sort(String::compareTo);

        return sortedTables.stream()
                .map(table -> {
                    Order latestOrder = latestOrderPerTable.get(table);
                    String currentStatus = latestOrder != null ? latestOrder.getStatus() : "EMPTY";

                    return CashierTableStatusResponse.builder()
                            .tableNumber(table)
                            .unpaid("PENDING".equals(currentStatus))
                            .status(currentStatus) // Ném status chuẩn xuống FE
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CashierOrderDetailResponse getPendingOrderByTable(String tableNumber) {
        String normalizedTable = normalizeTableNumber(tableNumber);

        List<Order> activeOrders = new ArrayList<>();
        activeOrders.addAll(orderRepository.findByStatus("PENDING"));
        activeOrders.addAll(orderRepository.findByStatus("PAID"));
        activeOrders.addAll(orderRepository.findByStatus("SERVED"));

        Order order = activeOrders.stream()
                .filter(candidate -> normalizedTable.equals(normalizeTableNumber(candidate.getTableNumber())))
                .max(Comparator.comparing(Order::getOrderTime))
                .orElseThrow(() -> new RuntimeException("Bàn này trống"));

        List<CashierOrderItemResponse> items = order.getOrderDetails().stream()
                .filter(detail -> detail.getFood() != null)
                .sorted(Comparator.comparing(OrderDetail::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(detail -> {
                    BigDecimal unitPrice = detail.getUnitPrice() == null ? BigDecimal.ZERO : detail.getUnitPrice();
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(detail.getQuantity() == null ? 0 : detail.getQuantity()));
                    return CashierOrderItemResponse.builder()
                            .orderDetailId(detail.getId())
                            .foodId(detail.getFood().getIdFood())
                            .foodName(detail.getFood().getFoodName())
                            .imageUrlFood(detail.getFood().getImageUrlFood())
                            .quantity(detail.getQuantity() == null ? 0 : detail.getQuantity())
                            .unitPrice(unitPrice)
                            .lineTotal(lineTotal)
                            .build();
                })
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CashierOrderItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CashierOrderDetailResponse.builder()
                .orderId(order.getIdOrder())
                .tableNumber(normalizeTableNumber(order.getTableNumber()))
                .customerName(order.getCustomerName())
                .orderTime(order.getOrderTime())
                .totalAmount(totalAmount)
                .status(order.getStatus())
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public Order placeOrder(OrderRequest request) {
        String normalizedTable = normalizeTableNumber(request.getTableNumber());
        Set<String> configuredTables = getConfiguredTableCodesFromAccounts();
        if (normalizedTable.isBlank() || !configuredTables.contains(normalizedTable)) {
            throw new RuntimeException("Bàn không hợp lệ hoặc chưa được cấu hình account bàn");
        }

        Order order = new Order();
        order.setIdOrder(generateNextOrderId());
        order.setTableNumber(normalizedTable);
        order.setCustomerName(request.getCustomerName());
        order.setOrderTime(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setCreatedBy(request.getCreatedBy());

        var details = request.getItems().stream().map(item -> {
            Food food = foodRepository.findById(item.getFoodId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn"));
            for (FoodIngredient recipe : food.getFoodIngredients()) {
                Ingredient ingredient = recipe.getIngredient();

                BigDecimal qtyUsed = recipe.getQuantityUsed() != null ? recipe.getQuantityUsed() : BigDecimal.ZERO;
                BigDecimal totalDeducted = qtyUsed.multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal currentStock = ingredient.getQuantityStock() != null ? ingredient.getQuantityStock() : BigDecimal.ZERO;

                if (currentStock.compareTo(totalDeducted) < 0) {
                    throw new IllegalArgumentException("HẾT_HÀNG|" + food.getFoodName());
                }

                ingredient.setQuantityStock(currentStock.subtract(totalDeducted));
                ingredientRepository.save(ingredient); 
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
        Order savedOrder = orderRepository.save(order);

        // BẮN TÍN HIỆU WEBSOCKET
       // messagingTemplate.convertAndSend("/topic/kitchen", "NEW_ORDER");

        return savedOrder;
    }

    @Override
    @Transactional
    public CashierPaymentResponse processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
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
        messagingTemplate.convertAndSend("/topic/kitchen", "NEW_ORDER");

        return CashierPaymentResponse.builder()
                .invoiceId(invoice.getIdInvoice())
                .orderId(order.getIdOrder())
                .tableNumber(normalizeTableNumber(order.getTableNumber()))
                .paymentMethod(invoice.getPaymentMethod())
                .totalAmount(invoice.getTotalAmount())
                .paymentDate(invoice.getPaymentDate())
                .build();
    }

    private String normalizeTableNumber(String tableNumber) {
        if (tableNumber == null || tableNumber.isBlank()) {
            return "";
        }

        String trimmed = tableNumber.trim().toUpperCase(Locale.ROOT)
                .replace("BÀN", "")
                .replace("BAN", "")
                .trim();

        Matcher matcher = TABLE_CODE_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group();
        }

        if (trimmed.matches("\\d{2}")) {
            return "N" + trimmed;
        }

        if (trimmed.matches("[A-Z]\\d{2}")) {
            return trimmed;
        }

        return "";
    }

    private Set<String> getConfiguredTableCodesFromAccounts() {
        return userRepository.findAllWithRole().stream()
                .filter(this::isTableRoleAccount)
                .map(this::extractTableCodeFromAccount)
                .filter(code -> !code.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isTableRoleAccount(User user) {
        if (user == null) {
            return false;
        }

        String roleName = user.getRole() != null && user.getRole().getRoleName() != null
                ? user.getRole().getRoleName().trim().toLowerCase(Locale.ROOT)
                : "";

        if (roleName.contains("admin") || roleName.contains("thu ngân") || roleName.contains("thu ngan")
                || roleName.contains("bếp") || roleName.contains("bep")) {
            return false;
        }

        if (roleName.contains("khách") || roleName.contains("khach") || roleName.contains("ban") || roleName.contains("bàn")) {
            return true;
        }

        return !extractTableCodeFromAccount(user).isBlank();
    }

    private String extractTableCodeFromAccount(User user) {
        if (user == null) {
            return "";
        }

        String fromFullName = normalizeTableNumber(user.getFullName());
        if (!fromFullName.isBlank()) {
            return fromFullName;
        }

        return normalizeTableNumber(user.getUsername());
    }

    @Override
    @Transactional
    public void completeOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Chỉ cho phép dọn bàn khi bếp đã nấu xong (SERVED)
        if (!"SERVED".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Không thể dọn bàn khi bếp chưa phục vụ xong!");
        }

        // Đổi trạng thái thành COMPLETED (Hoàn tất)
        order.setStatus("COMPLETED");
        orderRepository.save(order);

        // Bắn tín hiệu WebSocket báo dọn bàn thành công (để các máy khác cập nhật thành Xanh)
        messagingTemplate.convertAndSend("/topic/cashier", "TABLE_CLEARED");
    }
}