package com.fastfood.entity.transaction;

import java.math.BigDecimal;

import com.fastfood.entity.catalog.Ingredient;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "stock_receipt_details")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReceiptDetail {

    @EmbeddedId
    StockReceiptDetailId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("receiptId")
    @JoinColumn(name = "receipt_id")
    StockReceipt stockReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredient_id")
    Ingredient ingredient;

    @Column(name = "quantity_import", precision = 18, scale = 2)
    BigDecimal quantityImport;

    @Column(name = "import_price", precision = 18, scale = 2)
    BigDecimal importPrice;
}