package com.fastfood.entity.transaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

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
    @Column(name = "id_receipt", length = 10)
    String idReceipt;

    @Column(name = "receipt_date")
    LocalDate receiptDate;

    @Column(name = "supplier_name", length = 100)
    String supplierName;

    @Column(name = "status", length = 20)
    String status;

    @Column(name = "created_by", length = 10)
    String createdBy;

    @OneToMany(mappedBy = "stockReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<StockReceiptDetail> details = new ArrayList<>();
}