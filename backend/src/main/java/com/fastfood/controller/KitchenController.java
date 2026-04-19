package com.fastfood.controller;

import com.fastfood.dto.ApiResponse;
import com.fastfood.dto.response.KitchenFoodPendingResponse;
import com.fastfood.dto.response.KitchenTableOrderResponse;
import com.fastfood.service.IKitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final IKitchenService kitchenService;

    @GetMapping("/orders")
    public ApiResponse<List<KitchenTableOrderResponse>> getPendingOrdersByTable() {
        return ApiResponse.<List<KitchenTableOrderResponse>>builder()
                .code(200)
                .message("Lấy danh sách đơn hàng nhà bếp thành công")
                .data(kitchenService.getPendingOrdersByTable())
                .build();
    }

    @GetMapping("/foods/remaining")
    public ApiResponse<List<KitchenFoodPendingResponse>> getRemainingFoodSummary() {
        return ApiResponse.<List<KitchenFoodPendingResponse>>builder()
                .code(200)
                .message("Lấy danh sách món còn lại thành công")
                .data(kitchenService.getRemainingFoodSummary())
                .build();
    }

    @PostMapping("/orders/items/{orderDetailId}/served")
    public ApiResponse<Void> markOrderItemServed(@PathVariable Long orderDetailId) {
        kitchenService.markOrderItemServed(orderDetailId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã cập nhật món sang trạng thái hoàn thành")
                .build();
    }
}


