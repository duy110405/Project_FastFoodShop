package com.fastfood.service.impl;

import com.fastfood.dto.request.StockReceiptDetailRequest;
import com.fastfood.dto.request.StockReceiptRequest;
import com.fastfood.dto.response.StockReceiptResponse;
import com.fastfood.entity.catalog.Ingredient;
import com.fastfood.entity.transaction.StockReceipt;
import com.fastfood.entity.transaction.StockReceiptDetail;
import com.fastfood.entity.transaction.StockReceiptDetailId;
import com.fastfood.repository.IngredientRepository;
import com.fastfood.repository.OrderDetailRepository;
import com.fastfood.repository.StockReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryImplTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private StockReceiptRepository stockReceiptRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    private InventoryImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryImpl(ingredientRepository, stockReceiptRepository, orderDetailRepository);
    }

    @Test
    void updateStockReceipt_whenChoToDaNhap_shouldIncreaseStock() {
        Ingredient ingredient = buildIngredient("NL001", new BigDecimal("10"));
        StockReceipt receipt = buildReceipt("PN001", "CHO", new ArrayList<>());

        StockReceiptRequest request = StockReceiptRequest.builder()
                .receiptDate(LocalDate.now())
                .supplierName("NCC A")
                .status("DA_NHAP")
                .createdBy("U_001")
                .details(List.of(buildDetailRequest("NL001", "2")))
                .build();

        when(stockReceiptRepository.findById("PN001")).thenReturn(Optional.of(receipt));
        when(ingredientRepository.findById("NL001")).thenReturn(Optional.of(ingredient));
        when(stockReceiptRepository.save(any(StockReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockReceiptResponse response = inventoryService.updateStockReceipt("PN001", request);

        assertNotNull(response);
        assertEquals("DA_NHAP", response.getStatus());
        assertEquals(new BigDecimal("12"), ingredient.getQuantityStock());
        verify(ingredientRepository, times(1)).save(ingredient);
    }

    @Test
    void updateStockReceipt_whenChoToHoanTra_shouldNotIncreaseStock() {
        Ingredient ingredient = buildIngredient("NL001", new BigDecimal("10"));
        StockReceipt receipt = buildReceipt("PN001", "CHO", new ArrayList<>());

        StockReceiptRequest request = StockReceiptRequest.builder()
                .receiptDate(LocalDate.now())
                .supplierName("NCC A")
                .status("HOAN_TRA")
                .createdBy("U_001")
                .details(List.of(buildDetailRequest("NL001", "2")))
                .build();

        when(stockReceiptRepository.findById("PN001")).thenReturn(Optional.of(receipt));
        when(ingredientRepository.findById("NL001")).thenReturn(Optional.of(ingredient));
        when(stockReceiptRepository.save(any(StockReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockReceiptResponse response = inventoryService.updateStockReceipt("PN001", request);

        assertNotNull(response);
        assertEquals("HOAN_TRA", response.getStatus());
        assertEquals(new BigDecimal("10"), ingredient.getQuantityStock());
        verify(ingredientRepository, never()).save(any(Ingredient.class));
    }

    @Test
    void deleteStockReceipt_whenDaNhap_shouldRollbackStock() {
        Ingredient ingredient = buildIngredient("NL001", new BigDecimal("10"));

        StockReceipt receipt = buildReceipt("PN001", "DA_NHAP", new ArrayList<>());
        StockReceiptDetail detail = StockReceiptDetail.builder()
                .id(new StockReceiptDetailId("PN001", "NL001"))
                .stockReceipt(receipt)
                .ingredient(ingredient)
                .quantityImport(new BigDecimal("3"))
                .importPrice(new BigDecimal("10000"))
                .build();
        receipt.getDetails().add(detail);

        when(stockReceiptRepository.findById("PN001")).thenReturn(Optional.of(receipt));

        inventoryService.deleteStockReceipt("PN001");

        assertEquals(new BigDecimal("7"), ingredient.getQuantityStock());
        verify(ingredientRepository, times(1)).save(ingredient);
        verify(stockReceiptRepository, times(1)).delete(receipt);
    }

    private Ingredient buildIngredient(String id, BigDecimal stock) {
        Ingredient ingredient = new Ingredient();
        ingredient.setIdIngredient(id);
        ingredient.setIngredientName("Nguyen lieu test");
        ingredient.setUnit("kg");
        ingredient.setQuantityStock(stock);
        return ingredient;
    }

    private StockReceipt buildReceipt(String id, String status, List<StockReceiptDetail> details) {
        StockReceipt receipt = new StockReceipt();
        receipt.setIdReceipt(id);
        receipt.setReceiptDate(LocalDate.now());
        receipt.setSupplierName("NCC");
        receipt.setStatus(status);
        receipt.setCreatedBy("U_001");
        receipt.setDetails(details);
        return receipt;
    }

    private StockReceiptDetailRequest buildDetailRequest(String ingredientId, String qty) {
        return StockReceiptDetailRequest.builder()
                .ingredientId(ingredientId)
                .quantityImport(new BigDecimal(qty))
                .importPrice(new BigDecimal("10000"))
                .build();
    }
}

