package com.fastfood.repository;

import com.fastfood.entity.transaction.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, String> {

    @Query("SELECT MAX(s.idInvoice) FROM SalesInvoice s")
    String findMaxIdInvoice();

    @Query("""
            SELECT COALESCE(SUM(s.totalAmount), 0)
            FROM SalesInvoice s
            WHERE s.paymentDate >= :fromDateTime
              AND s.paymentDate < :toDateTime
            """)
    BigDecimal sumRevenueInRange(@Param("fromDateTime") LocalDateTime fromDateTime,
                                 @Param("toDateTime") LocalDateTime toDateTime);

    @Query("""
            SELECT COUNT(s)
            FROM SalesInvoice s
            WHERE s.paymentDate >= :fromDateTime
              AND s.paymentDate < :toDateTime
            """)
    long countInvoicesInRange(@Param("fromDateTime") LocalDateTime fromDateTime,
                              @Param("toDateTime") LocalDateTime toDateTime);
}
