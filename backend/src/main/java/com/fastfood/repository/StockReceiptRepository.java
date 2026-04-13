package com.fastfood.repository;

import com.fastfood.entity.transaction.StockReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockReceiptRepository extends JpaRepository<StockReceipt, String> {
    @Query("select max(s.idStockReceipt) from StockReceipt s")
    String findMaxIdStockReceipt();
}
