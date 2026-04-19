package com.fastfood.service.impl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fastfood.dto.request.StockReceiptDetailRequest;
import com.fastfood.dto.request.StockReceiptRequest;
import com.fastfood.dto.response.InventoryConsumptionGroupResponse;
import com.fastfood.dto.response.InventoryConsumptionItemResponse;
import com.fastfood.dto.response.InventoryItemReportResponse;
import com.fastfood.dto.response.StockMovementDetailResponse;
import com.fastfood.dto.response.StockReceiptResponse;
import com.fastfood.entity.catalog.Ingredient;
import com.fastfood.entity.transaction.StockReceipt;
import com.fastfood.entity.transaction.StockReceiptDetail;
import com.fastfood.entity.transaction.StockReceiptDetailId;
import com.fastfood.repository.IngredientRepository;
import com.fastfood.repository.OrderDetailRepository;
import com.fastfood.repository.StockReceiptRepository;
import com.fastfood.service.IInventoryService;

import jakarta.transaction.Transactional;

@Service
public class InventoryImpl implements IInventoryService {

    private final IngredientRepository ingredientRepository;
    private final StockReceiptRepository stockReceiptRepository;
    private final OrderDetailRepository orderDetailRepository;

    public InventoryImpl(IngredientRepository ingredientRepository,
                         StockReceiptRepository stockReceiptRepository,
                         OrderDetailRepository orderDetailRepository) {
        this.ingredientRepository = ingredientRepository;
        this.stockReceiptRepository = stockReceiptRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    @Transactional
    public StockReceiptResponse createStockReceipt(StockReceiptRequest request) {
        StockReceipt stockReceipt = StockReceipt.builder()
                .idReceipt(generateNextId(stockReceiptRepository.findMaxIdReceipt(), "PN"))
                .receiptDate(request.getReceiptDate())
                .supplierName(request.getSupplierName())
                .status(request.getStatus())
                .createdBy(request.getCreatedBy())
                .details(new ArrayList<>())
                .build();

        if (request.getDetails() != null) {
            for (StockReceiptDetailRequest detailRequest : request.getDetails()) {
                Ingredient ingredient = findIngredient(detailRequest.getIngredientId());

                BigDecimal currentStock = getIngredientQuantityStock(ingredient);
                BigDecimal importQty = detailRequest.getQuantityImport() == null
                        ? BigDecimal.ZERO
                        : detailRequest.getQuantityImport();

                setIngredientQuantityStock(ingredient, currentStock.add(importQty));
                ingredientRepository.save(ingredient);

                StockReceiptDetail detail = StockReceiptDetail.builder()
                        .id(new StockReceiptDetailId(stockReceipt.getIdReceipt(), getIngredientId(ingredient)))
                        .stockReceipt(stockReceipt)
                        .ingredient(ingredient)
                        .quantityImport(detailRequest.getQuantityImport())
                        .importPrice(detailRequest.getImportPrice())
                        .build();

                stockReceipt.getDetails().add(detail);
            }
        }

        StockReceipt saved = stockReceiptRepository.save(stockReceipt);
        return toReceiptResponse(saved);
    }

    @Override
    public List<StockReceiptResponse> getAllStockReceipts() {
        return stockReceiptRepository.findAll()
                .stream()
                .map(this::toReceiptResponse)
                .toList();
    }

    @Override
    @Transactional
    public StockReceiptResponse updateStockReceipt(String idReceipt, StockReceiptRequest request) {
        StockReceipt stockReceipt = stockReceiptRepository.findById(idReceipt)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập: " + idReceipt));

        if (stockReceipt.getDetails() != null) {
            for (StockReceiptDetail oldDetail : stockReceipt.getDetails()) {
                Ingredient ingredient = oldDetail.getIngredient();
                BigDecimal currentStock = getIngredientQuantityStock(ingredient);
                BigDecimal oldQty = oldDetail.getQuantityImport() == null
                        ? BigDecimal.ZERO
                        : oldDetail.getQuantityImport();

                setIngredientQuantityStock(ingredient, currentStock.subtract(oldQty));
                ingredientRepository.save(ingredient);
            }
        }

        stockReceipt.getDetails().clear();

        stockReceipt.setReceiptDate(request.getReceiptDate());
        stockReceipt.setSupplierName(request.getSupplierName());
        stockReceipt.setStatus(request.getStatus());
        stockReceipt.setCreatedBy(request.getCreatedBy());

        if (request.getDetails() != null) {
            for (StockReceiptDetailRequest detailRequest : request.getDetails()) {
                Ingredient ingredient = findIngredient(detailRequest.getIngredientId());

                BigDecimal currentStock = getIngredientQuantityStock(ingredient);
                BigDecimal importQty = detailRequest.getQuantityImport() == null
                        ? BigDecimal.ZERO
                        : detailRequest.getQuantityImport();

                setIngredientQuantityStock(ingredient, currentStock.add(importQty));
                ingredientRepository.save(ingredient);

                StockReceiptDetail detail = StockReceiptDetail.builder()
                        .id(new StockReceiptDetailId(stockReceipt.getIdReceipt(), getIngredientId(ingredient)))
                        .stockReceipt(stockReceipt)
                        .ingredient(ingredient)
                        .quantityImport(detailRequest.getQuantityImport())
                        .importPrice(detailRequest.getImportPrice())
                        .build();

                stockReceipt.getDetails().add(detail);
            }
        }

        StockReceipt saved = stockReceiptRepository.save(stockReceipt);
        return toReceiptResponse(saved);
    }

    @Override
    @Transactional
    public void deleteStockReceipt(String idReceipt) {
        StockReceipt stockReceipt = stockReceiptRepository.findById(idReceipt)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập: " + idReceipt));

        if (stockReceipt.getDetails() != null) {
            for (StockReceiptDetail detail : stockReceipt.getDetails()) {
                Ingredient ingredient = detail.getIngredient();
                BigDecimal currentStock = getIngredientQuantityStock(ingredient);
                BigDecimal qty = detail.getQuantityImport() == null
                        ? BigDecimal.ZERO
                        : detail.getQuantityImport();

                setIngredientQuantityStock(ingredient, currentStock.subtract(qty));
                ingredientRepository.save(ingredient);
            }
        }

        stockReceiptRepository.delete(stockReceipt);
    }

    @Override
    public List<StockReceiptResponse> searchStockReceipts(String supplierName,
                                                          LocalDate fromDate,
                                                          LocalDate toDate,
                                                          String ingredientId) {
        return stockReceiptRepository.searchReceipts(
                        emptyToNull(supplierName),
                        fromDate,
                        toDate,
                        emptyToNull(ingredientId)
                )
                .stream()
                .map(this::toReceiptResponse)
                .toList();
    }

    @Override
    public List<InventoryItemReportResponse> getInventoryReport() {
        return ingredientRepository.findAll()
                .stream()
                .map(this::toInventoryItemReport)
                .toList();
    }

    @Override
    public List<InventoryItemReportResponse> getLowStockItems() {
        return ingredientRepository.findAll()
                .stream()
                .filter(ingredient -> getIngredientQuantityStock(ingredient).compareTo(BigDecimal.TEN) <= 0)
                .map(this::toInventoryItemReport)
                .toList();
    }

    @Override
    public List<InventoryConsumptionGroupResponse> getConsumptionHistory(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> rows = orderDetailRepository.getIngredientConsumptionHistory(fromDate, toDate);

        Map<LocalDate, List<InventoryConsumptionItemResponse>> grouped = new LinkedHashMap<>();

        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row[0]);
            if (date == null) {
                continue;
            }

            String ingredientId = row[1] != null ? row[1].toString() : null;
            String imageUrlIngredient = row[2] != null ? row[2].toString() : null;
            String ingredientName = row[3] != null ? row[3].toString() : null;
            String unit = row[4] != null ? row[4].toString() : null;
            BigDecimal currentStock = toBigDecimal(row[5]);
            BigDecimal consumedQuantity = toBigDecimal(row[6]);

            grouped.computeIfAbsent(date, k -> new ArrayList<>())
                    .add(
                            InventoryConsumptionItemResponse.builder()
                                    .ingredientId(ingredientId)
                                    .imageUrlIngredient(imageUrlIngredient)
                                    .ingredientName(ingredientName)
                                    .unit(unit)
                                    .currentStock(currentStock)
                                    .consumedQuantity(consumedQuantity)
                                    .build()
                    );
        }

        List<InventoryConsumptionGroupResponse> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<InventoryConsumptionItemResponse>> entry : grouped.entrySet()) {
            result.add(
                    InventoryConsumptionGroupResponse.builder()
                            .date(entry.getKey())
                            .items(entry.getValue())
                            .build()
            );
        }

        return result;
    }

    private Ingredient findIngredient(String idIngredient) {
        return ingredientRepository.findById(idIngredient)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu: " + idIngredient));
    }

    private String generateNextId(String maxId, String prefix) {
        if (maxId == null) {
            return prefix + "001";
        }
        int next = Integer.parseInt(maxId.substring(prefix.length())) + 1;
        return String.format("%s%03d", prefix, next);
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private StockReceiptResponse toReceiptResponse(StockReceipt stockReceipt) {
        List<StockMovementDetailResponse> detailResponses = new ArrayList<>();

        if (stockReceipt.getDetails() != null) {
            for (StockReceiptDetail detail : stockReceipt.getDetails()) {
                detailResponses.add(
                        StockMovementDetailResponse.builder()
                                .ingredientId(getIngredientId(detail.getIngredient()))
                                .imageUrlIngredient(getIngredientImageUrl(detail.getIngredient()))
                                .ingredientName(getIngredientName(detail.getIngredient()))
                                .quantityImport(detail.getQuantityImport())
                                .importPrice(detail.getImportPrice())
                                .build()
                );
            }
        }

        return StockReceiptResponse.builder()
                .idReceipt(stockReceipt.getIdReceipt())
                .receiptDate(stockReceipt.getReceiptDate())
                .supplierName(stockReceipt.getSupplierName())
                .status(stockReceipt.getStatus())
                .createdBy(stockReceipt.getCreatedBy())
                .details(detailResponses)
                .build();
    }

    private InventoryItemReportResponse toInventoryItemReport(Ingredient ingredient) {
        BigDecimal stock = getIngredientQuantityStock(ingredient);

        return InventoryItemReportResponse.builder()
                .ingredientId(getIngredientId(ingredient))
                .imageUrlIngredient(getIngredientImageUrl(ingredient))
                .ingredientName(getIngredientName(ingredient))
                .unit(getIngredientUnit(ingredient))
                .currentStock(stock)
                .minStock(BigDecimal.TEN)
                .lowStock(stock.compareTo(BigDecimal.TEN) <= 0)
                .build();
    }

    private BigDecimal getIngredientQuantityStock(Ingredient ingredient) {
        try {
            Method method = ingredient.getClass().getMethod("getQuantityStock");
            Object value = method.invoke(ingredient);
            return value == null ? BigDecimal.ZERO : (BigDecimal) value;
        } catch (Exception ignored) {
        }

        return BigDecimal.ZERO;
    }

    private void setIngredientQuantityStock(Ingredient ingredient, BigDecimal value) {
        try {
            Method method = ingredient.getClass().getMethod("setQuantityStock", BigDecimal.class);
            method.invoke(ingredient, value);
        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật số lượng tồn kho cho nguyên liệu");
        }
    }

    private String getIngredientId(Ingredient ingredient) {
        try {
            Method method = ingredient.getClass().getMethod("getIdIngredient");
            Object value = method.invoke(ingredient);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            throw new RuntimeException("Không thể lấy mã nguyên liệu");
        }
    }

    private String getIngredientName(Ingredient ingredient) {
        try {
            Method method = ingredient.getClass().getMethod("getIngredientName");
            Object value = method.invoke(ingredient);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String getIngredientUnit(Ingredient ingredient) {
        try {
            Method method = ingredient.getClass().getMethod("getUnit");
            Object value = method.invoke(ingredient);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String getIngredientImageUrl(Ingredient ingredient) {
        try {
            Method method = ingredient.getClass().getMethod("getImageUrlIngredient");
            Object value = method.invoke(ingredient);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate localDate) return localDate;
        if (value instanceof Date sqlDate) return sqlDate.toLocalDate();
        return LocalDate.parse(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bigDecimal) return bigDecimal;
        return new BigDecimal(value.toString());
    }
}