package com.fastfood.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fastfood.dto.ApiResponse;
import com.fastfood.dto.request.StockReceiptRequest;
import com.fastfood.dto.response.InventoryConsumptionGroupResponse;
import com.fastfood.dto.response.InventoryItemReportResponse;
import com.fastfood.dto.response.StockReceiptResponse;
import com.fastfood.service.IInventoryService;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final IInventoryService inventoryService;

    public InventoryController(IInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/receipts")
    public ApiResponse<StockReceiptResponse> createReceipt(@RequestBody StockReceiptRequest request) {
        return ApiResponse.<StockReceiptResponse>builder()
                .code(201)
                .message("Nhập kho thành công")
                .data(inventoryService.createStockReceipt(request))
                .build();
    }

    @GetMapping("/receipts")
    public ApiResponse<List<StockReceiptResponse>> getAllReceipts() {
        return ApiResponse.<List<StockReceiptResponse>>builder()
                .code(200)
                .message("Lấy danh sách phiếu nhập thành công")
                .data(inventoryService.getAllStockReceipts())
                .build();
    }

    @PutMapping("/receipts/{idReceipt}")
    public ApiResponse<StockReceiptResponse> updateReceipt(
            @PathVariable String idReceipt,
            @RequestBody StockReceiptRequest request
    ) {
        return ApiResponse.<StockReceiptResponse>builder()
                .code(200)
                .message("Cập nhật phiếu nhập thành công")
                .data(inventoryService.updateStockReceipt(idReceipt, request))
                .build();
    }

    @DeleteMapping("/receipts/{idReceipt}")
    public ApiResponse<String> deleteReceipt(@PathVariable String idReceipt) {
        inventoryService.deleteStockReceipt(idReceipt);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Xóa phiếu nhập thành công")
                .data(idReceipt)
                .build();
    }

    @GetMapping("/receipts/search")
    public ApiResponse<List<StockReceiptResponse>> searchReceipts(
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String ingredientId
    ) {
        return ApiResponse.<List<StockReceiptResponse>>builder()
                .code(200)
                .message("Tìm phiếu nhập thành công")
                .data(inventoryService.searchStockReceipts(
                        supplierName, fromDate, toDate, ingredientId
                ))
                .build();
    }

    @GetMapping("/report")
    public ApiResponse<List<InventoryItemReportResponse>> getInventoryReport() {
        return ApiResponse.<List<InventoryItemReportResponse>>builder()
                .code(200)
                .message("Lấy báo cáo tồn kho thành công")
                .data(inventoryService.getInventoryReport())
                .build();
    }

    @GetMapping("/low-stock")
    public ApiResponse<List<InventoryItemReportResponse>> getLowStockItems() {
        return ApiResponse.<List<InventoryItemReportResponse>>builder()
                .code(200)
                .message("Lấy danh sách nguyên liệu sắp hết thành công")
                .data(inventoryService.getLowStockItems())
                .build();
    }

    @GetMapping("/consumption-history")
    public ApiResponse<List<InventoryConsumptionGroupResponse>> getConsumptionHistory(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
        return ApiResponse.<List<InventoryConsumptionGroupResponse>>builder()
                .code(200)
                .message("Lấy lịch sử tiêu thụ nguyên liệu thành công")
                .data(inventoryService.getConsumptionHistory(fromDate, toDate))
                .build();
    }
}