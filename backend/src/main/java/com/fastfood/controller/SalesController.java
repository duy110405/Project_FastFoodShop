package com.fastfood.controller;

import com.fastfood.dto.ApiResponse;
import com.fastfood.dto.request.OrderRequest;
import com.fastfood.dto.request.PaymentRequest;
import com.fastfood.dto.response.CashierOrderDetailResponse;
import com.fastfood.dto.response.CashierPaymentResponse;
import com.fastfood.dto.response.CashierTableStatusResponse;
import com.fastfood.service.ISalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final ISalesService salesService;

    // API lấy trạng thái bàn cho màn hình thu ngân
    @GetMapping("/tables/status")
    public ResponseEntity<Set<String>> getTablesStatus() {
        return ResponseEntity.ok(salesService.getOccupiedTableNumbers());
    }

    @GetMapping("/tables")
    public ApiResponse<List<CashierTableStatusResponse>> getTableStatusList() {
        return ApiResponse.<List<CashierTableStatusResponse>>builder()
                .code(200)
                .message("Lấy trạng thái bàn thành công")
                .data(salesService.getTableStatuses())
                .build();
    }

    @GetMapping("/tables/{tableNumber}/order")
    public ApiResponse<CashierOrderDetailResponse> getPendingOrderByTable(@PathVariable String tableNumber) {
        return ApiResponse.<CashierOrderDetailResponse>builder()
                .code(200)
                .message("Lấy đơn hàng theo bàn thành công")
                .data(salesService.getPendingOrderByTable(tableNumber))
                .build();
    }

    // API đặt món từ màn hình khách hàng
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        // Vẫn gọi hàm lưu bình thường
        salesService.placeOrder(request);

        // NHƯNG chỉ trả về một câu thông báo (hoặc một cục JSON nhỏ nhắn)
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Đặt món thành công! Bếp đang chuẩn bị món ăn cho bạn."
        ));
    }

    // API thanh toán từ màn hình thu ngân
    @PostMapping("/payments")
    public ApiResponse<CashierPaymentResponse> checkout(@RequestBody PaymentRequest request) {
        return ApiResponse.<CashierPaymentResponse>builder()
                .code(200)
                .message("Thanh toán thành công")
                .data(salesService.processPayment(request))
                .build();
    }

    @PostMapping("/orders/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable String orderId) {
        try {
            salesService.completeOrder(orderId);
            return ResponseEntity.ok().body(Map.of("message", "Đã dọn bàn và hoàn tất đơn hàng thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}