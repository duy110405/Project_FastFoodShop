package com.fastfood.controller;

import com.fastfood.dto.request.OrderRequest;
import com.fastfood.dto.request.PaymentRequest;
import com.fastfood.service.ISalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SalesController {

    private final ISalesService salesService;

    // API lấy trạng thái bàn cho màn hình thu ngân
    @GetMapping("/tables/status")
    public ResponseEntity<Set<String>> getTablesStatus() {
        return ResponseEntity.ok(salesService.getOccupiedTableNumbers());
    }

    // API đặt món từ màn hình khách hàng
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(salesService.placeOrder(request));
    }

    // API thanh toán từ màn hình thu ngân
    @PostMapping("/payments")
    public ResponseEntity<?> checkout(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(salesService.processPayment(request));
    }
}