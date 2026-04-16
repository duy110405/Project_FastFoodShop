package com.fastfood.controller;

import com.fastfood.dto.ApiResponse;
import com.fastfood.dto.request.FoodRequest;
import com.fastfood.dto.response.FoodKitchenResponse;
import com.fastfood.dto.response.FoodMenuResponse;
import com.fastfood.service.IFoodService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
//@CrossOrigin("*") // Cho phép React gọi API
public class FoodController {
    public final IFoodService foodService;

    public FoodController(IFoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/menu")
    public ApiResponse<List<FoodMenuResponse>> getAllMenu() {
        return ApiResponse.<List<FoodMenuResponse>>builder()
                .code(200)
                .message("Lấy danh sách thực đơn thành công")
                .data(foodService.getAllMenu())
                .build();
    }

    @GetMapping("/menu/{idFood}")
    public ApiResponse<FoodMenuResponse> getMenuById(@PathVariable String idFood) {
        return ApiResponse.<FoodMenuResponse>builder()
                .code(200)
                .message("Lấy thông tin món ăn thành công")
                .data(foodService.getMenuById(idFood))
                .build();
    }
    @GetMapping("/kitchen/{idFood}")
    public ApiResponse<FoodKitchenResponse> getFoodForKitchen(@PathVariable String idFood) {
        return ApiResponse.<FoodKitchenResponse>builder()
                .code(200)
                .message("Lấy dữ liệu cho nhà bếp thành công")
                .data(foodService.getFoodForKitchen(idFood))
                .build();
    }
    @PostMapping
    public ApiResponse<FoodKitchenResponse> createFood(@RequestBody FoodRequest request) {
        return ApiResponse.<FoodKitchenResponse>builder()
                .code(201)
                .message("Tạo món ăn mới thành công")
                .data(foodService.createFood(request))
                .build();
    }
    @PutMapping("/{idFood}")
    public ApiResponse<FoodKitchenResponse> updateFood(
            @PathVariable String idFood,
            @RequestBody FoodRequest request) {
        return ApiResponse.<FoodKitchenResponse>builder()
                .code(200)
                .message("Cập nhật món ăn thành công")
                .data(foodService.updateFood(idFood, request))
                .build();
    }
    @DeleteMapping("/{idFood}")
    public ApiResponse<Void> deleteFood(@PathVariable String idFood) {
        foodService.deleteFood(idFood);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa món ăn thành công")
                .build();
    }
}
