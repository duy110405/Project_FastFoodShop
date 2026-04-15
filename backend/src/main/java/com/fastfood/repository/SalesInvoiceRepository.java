package com.fastfood.repository;

import com.fastfood.entity.transaction.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, String> {

    @Query("SELECT MAX(s.idInvoice) FROM SalesInvoice s")
    String findMaxIdInvoice();
}
