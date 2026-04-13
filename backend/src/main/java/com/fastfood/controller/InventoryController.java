package com.fastfood.controller;

import com.fastfood.dto.ApiResponse;
import com.fastfood.dto.request.StockIssueRequest;
import com.fastfood.dto.request.StockReceiptRequest;
import com.fastfood.dto.request.StockRequestRequest;
import com.fastfood.dto.response.InventoryItemReportResponse;
import com.fastfood.dto.response.StockIssueResponse;
import com.fastfood.dto.response.StockReceiptResponse;
import com.fastfood.dto.response.StockRequestResponse;
import com.fastfood.service.IInventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin("*")
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

    @PostMapping("/requests")
    public ApiResponse<StockRequestResponse> createRequest(@RequestBody StockRequestRequest request) {
        return ApiResponse.<StockRequestResponse>builder()
                .code(201)
                .message("Tạo phiếu yêu cầu nguyên liệu thành công")
                .data(inventoryService.createStockRequest(request))
                .build();
    }

    @GetMapping("/requests")
    public ApiResponse<List<StockRequestResponse>> getAllRequests() {
        return ApiResponse.<List<StockRequestResponse>>builder()
                .code(200)
                .message("Lấy danh sách phiếu yêu cầu thành công")
                .data(inventoryService.getAllStockRequests())
                .build();
    }

    @PostMapping("/issues")
    public ApiResponse<StockIssueResponse> createIssue(@RequestBody StockIssueRequest request) {
        return ApiResponse.<StockIssueResponse>builder()
                .code(201)
                .message("Xuất kho thành công")
                .data(inventoryService.createStockIssue(request))
                .build();
    }

    @GetMapping("/issues")
    public ApiResponse<List<StockIssueResponse>> getAllIssues() {
        return ApiResponse.<List<StockIssueResponse>>builder()
                .code(200)
                .message("Lấy danh sách phiếu xuất thành công")
                .data(inventoryService.getAllStockIssues())
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
}
