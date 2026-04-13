package com.fastfood.repository;

import com.fastfood.entity.transaction.StockRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockRequestRepository extends JpaRepository<StockRequest, String> {
    @Query("select max(s.idStockRequest) from StockRequest s")
    String findMaxIdStockRequest();
}
