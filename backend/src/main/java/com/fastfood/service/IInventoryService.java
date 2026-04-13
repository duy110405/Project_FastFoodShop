package com.fastfood.service;

import com.fastfood.dto.request.StockIssueRequest;
import com.fastfood.dto.request.StockReceiptRequest;
import com.fastfood.dto.request.StockRequestRequest;
import com.fastfood.dto.response.InventoryItemReportResponse;
import com.fastfood.dto.response.StockIssueResponse;
import com.fastfood.dto.response.StockReceiptResponse;
import com.fastfood.dto.response.StockRequestResponse;

import java.util.List;

public interface IInventoryService {
    StockReceiptResponse createStockReceipt(StockReceiptRequest request);
    List<StockReceiptResponse> getAllStockReceipts();
    StockRequestResponse createStockRequest(StockRequestRequest request);
    List<StockRequestResponse> getAllStockRequests();
    StockIssueResponse createStockIssue(StockIssueRequest request);
    List<StockIssueResponse> getAllStockIssues();
    List<InventoryItemReportResponse> getInventoryReport();
    List<InventoryItemReportResponse> getLowStockItems();
}
