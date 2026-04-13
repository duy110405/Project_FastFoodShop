package com.fastfood.service.impl;

import com.fastfood.dto.request.*;
import com.fastfood.dto.response.*;
import com.fastfood.entity.catalog.Ingredient;
import com.fastfood.entity.catalog.Supplier;
import com.fastfood.entity.transaction.*;
import com.fastfood.repository.*;
import com.fastfood.service.IInventoryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryImpl implements IInventoryService {
    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;
    private final StockReceiptRepository stockReceiptRepository;
    private final StockRequestRepository stockRequestRepository;
    private final StockIssueRepository stockIssueRepository;

    public InventoryImpl(IngredientRepository ingredientRepository,
                         SupplierRepository supplierRepository,
                         StockReceiptRepository stockReceiptRepository,
                         StockRequestRepository stockRequestRepository,
                         StockIssueRepository stockIssueRepository) {
        this.ingredientRepository = ingredientRepository;
        this.supplierRepository = supplierRepository;
        this.stockReceiptRepository = stockReceiptRepository;
        this.stockRequestRepository = stockRequestRepository;
        this.stockIssueRepository = stockIssueRepository;
    }

    @Override
    @Transactional
    public StockReceiptResponse createStockReceipt(StockReceiptRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));

        StockReceipt stockReceipt = StockReceipt.builder()
                .idStockReceipt(generateNextId(stockReceiptRepository.findMaxIdStockReceipt(), "NK"))
                .supplier(supplier)
                .receiptDate(request.getReceiptDate())
                .createdBy(request.getCreatedBy())
                .note(request.getNote())
                .details(new ArrayList<>())
                .build();

        for (StockReceiptDetailRequest detailRequest : request.getDetails()) {
            Ingredient ingredient = findIngredient(detailRequest.getIngredientId());
            ingredient.setQuantityStock(ingredient.getQuantityStock() + detailRequest.getQuantity());
            ingredientRepository.save(ingredient);

            StockReceiptDetail detail = StockReceiptDetail.builder()
                    .stockReceipt(stockReceipt)
                    .ingredient(ingredient)
                    .quantity(detailRequest.getQuantity())
                    .unitPrice(detailRequest.getUnitPrice())
                    .expiryDate(detailRequest.getExpiryDate())
                    .build();
            stockReceipt.getDetails().add(detail);
        }

        return toReceiptResponse(stockReceiptRepository.save(stockReceipt));
    }

    @Override
    public List<StockReceiptResponse> getAllStockReceipts() {
        return stockReceiptRepository.findAll().stream().map(this::toReceiptResponse).toList();
    }

    @Override
    @Transactional
    public StockRequestResponse createStockRequest(StockRequestRequest request) {
        StockRequest stockRequest = StockRequest.builder()
                .idStockRequest(generateNextId(stockRequestRepository.findMaxIdStockRequest(), "YC"))
                .requestDate(request.getRequestDate())
                .requestedBy(request.getRequestedBy())
                .status("PENDING")
                .note(request.getNote())
                .details(new ArrayList<>())
                .build();

        for (StockRequestDetailRequest detailRequest : request.getDetails()) {
            Ingredient ingredient = findIngredient(detailRequest.getIngredientId());
            StockRequestDetail detail = StockRequestDetail.builder()
                    .stockRequest(stockRequest)
                    .ingredient(ingredient)
                    .requestedQuantity(detailRequest.getRequestedQuantity())
                    .build();
            stockRequest.getDetails().add(detail);
        }

        return toStockRequestResponse(stockRequestRepository.save(stockRequest));
    }

    @Override
    public List<StockRequestResponse> getAllStockRequests() {
        return stockRequestRepository.findAll().stream().map(this::toStockRequestResponse).toList();
    }

    @Override
    @Transactional
    public StockIssueResponse createStockIssue(StockIssueRequest request) {
        StockRequest stockRequest = null;
        if (request.getStockRequestId() != null && !request.getStockRequestId().isBlank()) {
            stockRequest = stockRequestRepository.findById(request.getStockRequestId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu yêu cầu"));
        }

        StockIssue stockIssue = StockIssue.builder()
                .idStockIssue(generateNextId(stockIssueRepository.findMaxIdStockIssue(), "XK"))
                .stockRequest(stockRequest)
                .issueDate(request.getIssueDate())
                .issuedBy(request.getIssuedBy())
                .note(request.getNote())
                .details(new ArrayList<>())
                .build();

        for (StockIssueDetailRequest detailRequest : request.getDetails()) {
            Ingredient ingredient = findIngredient(detailRequest.getIngredientId());
            if (ingredient.getQuantityStock() < detailRequest.getQuantity()) {
                throw new RuntimeException("Tồn kho không đủ cho nguyên liệu: " + ingredient.getIngredientName());
            }

            ingredient.setQuantityStock(ingredient.getQuantityStock() - detailRequest.getQuantity());
            ingredientRepository.save(ingredient);

            StockIssueDetail detail = StockIssueDetail.builder()
                    .stockIssue(stockIssue)
                    .ingredient(ingredient)
                    .quantity(detailRequest.getQuantity())
                    .build();
            stockIssue.getDetails().add(detail);
        }

        if (stockRequest != null) {
            stockRequest.setStatus("COMPLETED");
            stockRequestRepository.save(stockRequest);
        }

        return toStockIssueResponse(stockIssueRepository.save(stockIssue));
    }

    @Override
    public List<StockIssueResponse> getAllStockIssues() {
        return stockIssueRepository.findAll().stream().map(this::toStockIssueResponse).toList();
    }

    @Override
    public List<InventoryItemReportResponse> getInventoryReport() {
        return ingredientRepository.findAll().stream()
                .map(this::toInventoryItemReport)
                .toList();
    }

    @Override
    public List<InventoryItemReportResponse> getLowStockItems() {
        return ingredientRepository.findAll().stream()
                .filter(ingredient -> ingredient.getQuantityStock() <= 10)
                .map(this::toInventoryItemReport)
                .toList();
    }

    private Ingredient findIngredient(String idIngredient) {
        return ingredientRepository.findById(idIngredient)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu: " + idIngredient));
    }

    private String generateNextId(String maxId, String prefix) {
        if (maxId == null) return prefix + "001";
        int next = Integer.parseInt(maxId.substring(prefix.length())) + 1;
        return String.format("%s%03d", prefix, next);
    }

    private StockReceiptResponse toReceiptResponse(StockReceipt stockReceipt) {
        return StockReceiptResponse.builder()
                .idStockReceipt(stockReceipt.getIdStockReceipt())
                .supplierId(stockReceipt.getSupplier().getIdSupplier())
                .supplierName(stockReceipt.getSupplier().getSupplierName())
                .receiptDate(stockReceipt.getReceiptDate())
                .createdBy(stockReceipt.getCreatedBy())
                .note(stockReceipt.getNote())
                .details(stockReceipt.getDetails().stream().map(detail -> StockMovementDetailResponse.builder()
                        .ingredientId(detail.getIngredient().getIdIngredient())
                        .ingredientName(detail.getIngredient().getIngredientName())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getUnitPrice())
                        .expiryDate(detail.getExpiryDate())
                        .build()).toList())
                .build();
    }

    private StockRequestResponse toStockRequestResponse(StockRequest stockRequest) {
        return StockRequestResponse.builder()
                .idStockRequest(stockRequest.getIdStockRequest())
                .requestDate(stockRequest.getRequestDate())
                .requestedBy(stockRequest.getRequestedBy())
                .status(stockRequest.getStatus())
                .note(stockRequest.getNote())
                .details(stockRequest.getDetails().stream().map(detail -> StockMovementDetailResponse.builder()
                        .ingredientId(detail.getIngredient().getIdIngredient())
                        .ingredientName(detail.getIngredient().getIngredientName())
                        .quantity(detail.getRequestedQuantity())
                        .build()).toList())
                .build();
    }

    private StockIssueResponse toStockIssueResponse(StockIssue stockIssue) {
        return StockIssueResponse.builder()
                .idStockIssue(stockIssue.getIdStockIssue())
                .stockRequestId(stockIssue.getStockRequest() != null ? stockIssue.getStockRequest().getIdStockRequest() : null)
                .issueDate(stockIssue.getIssueDate())
                .issuedBy(stockIssue.getIssuedBy())
                .note(stockIssue.getNote())
                .details(stockIssue.getDetails().stream().map(detail -> StockMovementDetailResponse.builder()
                        .ingredientId(detail.getIngredient().getIdIngredient())
                        .ingredientName(detail.getIngredient().getIngredientName())
                        .quantity(detail.getQuantity())
                        .build()).toList())
                .build();
    }

    private InventoryItemReportResponse toInventoryItemReport(Ingredient ingredient) {
        int minStock = 10;
        return InventoryItemReportResponse.builder()
                .ingredientId(ingredient.getIdIngredient())
                .ingredientName(ingredient.getIngredientName())
                .unit(ingredient.getUnit())
                .currentStock(ingredient.getQuantityStock())
                .minStock(minStock)
                .lowStock(ingredient.getQuantityStock() <= minStock)
                .build();
    }
}
