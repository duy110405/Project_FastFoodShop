package com.fastfood.controller;

import com.fastfood.dto.ApiResponse;
import com.fastfood.dto.request.FoodCategoryRequest;
import com.fastfood.dto.response.FoodCategoryResponse;
import com.fastfood.entity.catalog.Food;
import com.fastfood.service.IFoodCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foodCategory")
@CrossOrigin("*") // Cho phép React gọi API
public class FoodCategoryController {
    public final IFoodCategoryService foodCategoryService;

    public FoodCategoryController(IFoodCategoryService foodCategoryService) {
        this.foodCategoryService = foodCategoryService;
    }

    @GetMapping
    public ApiResponse<List<FoodCategoryResponse>> getAllCategory(){
        return ApiResponse.<List<FoodCategoryResponse>>builder()
                .code(200)
                .message("Lấy loại danh mục thành công")
                .data(foodCategoryService.getAllCategory())
                .build();
    }

    @GetMapping("/{idCategory}")
    public ApiResponse<FoodCategoryResponse> getCategoryById(@PathVariable String idCategory){
        return ApiResponse.<FoodCategoryResponse>builder()
                .code(200)
                .message("Lấy loại danh mục thành công")
                .data(foodCategoryService.getCategoryById(idCategory))
                .build();
    }

    @PostMapping
    public ApiResponse<FoodCategoryResponse> createCategory(@RequestBody FoodCategoryRequest foodCategoryRequest){
        return ApiResponse.<FoodCategoryResponse>builder()
                .code(201)
                .message("thêm thành công")
                .data(foodCategoryService.saveCategory(foodCategoryRequest))
                .build();
    }

    @PutMapping("/{idCategory}")
    public ApiResponse<FoodCategoryResponse> updateCategory(@PathVariable String idCategory , @RequestBody FoodCategoryRequest foodCategoryRequest){
        return ApiResponse.<FoodCategoryResponse>builder()
                .code(200)
                .message("cập nhật thành công")
                .data(foodCategoryService.updateCategory(idCategory , foodCategoryRequest))
                .build();
    }

    @DeleteMapping("/{idCategory}")
    public ApiResponse<FoodCategoryResponse> deleteCategory(@PathVariable String idCategory){
        foodCategoryService.deleteCategory(idCategory);
        return ApiResponse.<FoodCategoryResponse>builder()
                .code(200)
                .message("Xóa thành công")
                .build();
    }
}
