package com.fastfood.controller;

import com.fastfood.dto.ApiResponse;
import com.fastfood.dto.request.IngredientRequest;
import com.fastfood.dto.response.IngredientResponse;
import com.fastfood.service.IIngredientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient")
@CrossOrigin("*") // Cho phép React gọi API
public class IngredientController {
    private final IIngredientService ingredientService;

    public IngredientController(IIngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public ApiResponse<List<IngredientResponse>> getAllIngredient(){
        return ApiResponse.<List<IngredientResponse>>builder()
                .code(200)
                .message("Lấy nguyên liệu thành công")
                .data(ingredientService.getAllIngredient())
                .build();
    }

    @GetMapping("/{idIngredient}")
    public ApiResponse<IngredientResponse> getIngredientById(@PathVariable String idIngredient){
        return ApiResponse.<IngredientResponse>builder()
                .code(200)
                .message("lấy nguyên liệu thành công")
                .data(ingredientService.getIngredientById(idIngredient))
                .build();
    }

    @PostMapping
    public ApiResponse<IngredientResponse> createIngredient(@RequestBody IngredientRequest ingredientRequest){
        return ApiResponse.<IngredientResponse>builder()
                .code(201)
                .message("thêm thành công")
                .data(ingredientService.saveIngredient(ingredientRequest))
                .build();
    }

    @PutMapping("/{idIngredient}")
    public ApiResponse<IngredientResponse> updateIngredient(@PathVariable String idIngredient ,@RequestBody IngredientRequest ingredientRequest){
        return ApiResponse.<IngredientResponse>builder()
                .code(200)
                .message("Cập nhật thành công")
                .data(ingredientService.updateIngredient(ingredientRequest, idIngredient))
                .build();
    }

    @DeleteMapping("/{idIngredient}")
    public ApiResponse<IngredientResponse> deleteIngredient(@PathVariable String idIngredient){
        ingredientService.deleteIngredient(idIngredient);
        return ApiResponse.<IngredientResponse>builder()
                .code(200)
                .message("Xóa thành công")
                .data(null)
                .build();
    }

}
