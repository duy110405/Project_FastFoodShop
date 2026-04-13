package com.fastfood.entity.transaction;

import com.fastfood.entity.catalog.Supplier;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_receipts")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReceipt {
    @Id
    @Column(name = "id_stock_receipt", length = 20)
    String idStockReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    Supplier supplier;

    @Column(name = "receipt_date", nullable = false)
    LocalDate receiptDate;

    @Column(name = "created_by", length = 100)
    String createdBy;

    @Column(name = "note", length = 255)
    String note;

    @OneToMany(mappedBy = "stockReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<StockReceiptDetail> details = new ArrayList<>();
}
