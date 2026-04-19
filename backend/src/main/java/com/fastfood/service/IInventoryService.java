package com.fastfood.service;

import java.time.LocalDate;
import java.util.List;

import com.fastfood.dto.request.StockReceiptRequest;
import com.fastfood.dto.response.InventoryConsumptionGroupResponse;
import com.fastfood.dto.response.InventoryItemReportResponse;
import com.fastfood.dto.response.StockReceiptResponse;

public interface IInventoryService {
    StockReceiptResponse createStockReceipt(StockReceiptRequest request);

    List<StockReceiptResponse> getAllStockReceipts();

    StockReceiptResponse updateStockReceipt(String idReceipt, StockReceiptRequest request);

    void deleteStockReceipt(String idReceipt);

    List<StockReceiptResponse> searchStockReceipts(String supplierName,
                                                   LocalDate fromDate,
                                                   LocalDate toDate,
                                                   String ingredientId);

    List<InventoryItemReportResponse> getInventoryReport();

    List<InventoryItemReportResponse> getLowStockItems();

    List<InventoryConsumptionGroupResponse> getConsumptionHistory(LocalDate fromDate, LocalDate toDate);
}