package com.fastfood.repository;

import com.fastfood.entity.catalog.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SupplierRepository extends JpaRepository<Supplier, String> {
    @Query("select max(s.idSupplier) from Supplier s")
    String findMaxIdSupplier();
}
