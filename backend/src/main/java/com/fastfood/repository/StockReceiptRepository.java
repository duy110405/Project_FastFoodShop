package com.fastfood.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fastfood.entity.transaction.StockReceipt;

public interface StockReceiptRepository extends JpaRepository<StockReceipt, String> {

    @Query("select max(s.idReceipt) from StockReceipt s")
    String findMaxIdReceipt();

    @Query("""
        select distinct s
        from StockReceipt s
        left join s.details d
        left join d.ingredient i
        where (:supplierName is null or lower(s.supplierName) like lower(concat('%', :supplierName, '%')))
          and (:fromDate is null or s.receiptDate >= :fromDate)
          and (:toDate is null or s.receiptDate <= :toDate)
          and (:ingredientId is null or i.idIngredient = :ingredientId)
        order by s.receiptDate desc, s.idReceipt desc
    """)
    List<StockReceipt> searchReceipts(
            @Param("supplierName") String supplierName,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("ingredientId") String ingredientId
    );
}