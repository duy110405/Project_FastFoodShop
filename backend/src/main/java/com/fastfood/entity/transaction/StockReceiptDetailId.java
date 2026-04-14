package com.fastfood.entity.transaction;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReceiptDetailId implements Serializable {

    @Column(name = "receipt_id", length = 10)
    String receiptId;

    @Column(name = "ingredient_id", length = 10)
    String ingredientId;
}