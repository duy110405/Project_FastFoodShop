package com.fastfood.service;

import com.fastfood.dto.request.SupplierRequest;
import com.fastfood.dto.response.SupplierResponse;

import java.util.List;

public interface ISupplierService {
    List<SupplierResponse> getAllSuppliers();
    SupplierResponse getSupplierById(String idSupplier);
    SupplierResponse createSupplier(SupplierRequest request);
    SupplierResponse updateSupplier(String idSupplier, SupplierRequest request);
    void deleteSupplier(String idSupplier);
}
