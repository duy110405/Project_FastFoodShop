package com.fastfood.repository;

import com.fastfood.entity.transaction.StockIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockIssueRepository extends JpaRepository<StockIssue, String> {
    @Query("select max(s.idStockIssue) from StockIssue s")
    String findMaxIdStockIssue();
}
