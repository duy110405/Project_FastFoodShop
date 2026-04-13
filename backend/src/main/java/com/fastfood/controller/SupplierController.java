package com.fastfood.controller;

import com.fastfood.dto.ApiResponse;
import com.fastfood.dto.request.SupplierRequest;
import com.fastfood.dto.response.SupplierResponse;
import com.fastfood.service.ISupplierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin("*")
public class SupplierController {
    private final ISupplierService supplierService;

    public SupplierController(ISupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ApiResponse<List<SupplierResponse>> getAllSuppliers() {
        return ApiResponse.<List<SupplierResponse>>builder()
                .code(200)
                .message("Lấy danh sách nhà cung cấp thành công")
                .data(supplierService.getAllSuppliers())
                .build();
    }

    @GetMapping("/{idSupplier}")
    public ApiResponse<SupplierResponse> getSupplierById(@PathVariable String idSupplier) {
        return ApiResponse.<SupplierResponse>builder()
                .code(200)
                .message("Lấy nhà cung cấp thành công")
                .data(supplierService.getSupplierById(idSupplier))
                .build();
    }

    @PostMapping
    public ApiResponse<SupplierResponse> createSupplier(@RequestBody SupplierRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .code(201)
                .message("Thêm nhà cung cấp thành công")
                .data(supplierService.createSupplier(request))
                .build();
    }

    @PutMapping("/{idSupplier}")
    public ApiResponse<SupplierResponse> updateSupplier(@PathVariable String idSupplier, @RequestBody SupplierRequest request) {
        return ApiResponse.<SupplierResponse>builder()
                .code(200)
                .message("Cập nhật nhà cung cấp thành công")
                .data(supplierService.updateSupplier(idSupplier, request))
                .build();
    }

    @DeleteMapping("/{idSupplier}")
    public ApiResponse<Void> deleteSupplier(@PathVariable String idSupplier) {
        supplierService.deleteSupplier(idSupplier);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa nhà cung cấp thành công")
                .build();
    }
}
