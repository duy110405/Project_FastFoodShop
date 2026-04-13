package com.fastfood.service.impl;

import com.fastfood.dto.request.SupplierRequest;
import com.fastfood.dto.response.SupplierResponse;
import com.fastfood.entity.catalog.Supplier;
import com.fastfood.repository.SupplierRepository;
import com.fastfood.service.ISupplierService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierImpl implements ISupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public SupplierResponse getSupplierById(String idSupplier) {
        return toResponse(findById(idSupplier));
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .idSupplier(generateNextId())
                .supplierName(request.getSupplierName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .active(request.getActive() == null || request.getActive())
                .build();
        return toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(String idSupplier, SupplierRequest request) {
        Supplier supplier = findById(idSupplier);
        supplier.setSupplierName(request.getSupplierName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        if (request.getActive() != null) {
            supplier.setActive(request.getActive());
        }
        return toResponse(supplierRepository.save(supplier));
    }

    @Override
    public void deleteSupplier(String idSupplier) {
        supplierRepository.delete(findById(idSupplier));
    }

    private Supplier findById(String idSupplier) {
        return supplierRepository.findById(idSupplier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp: " + idSupplier));
    }

    private String generateNextId() {
        String maxId = supplierRepository.findMaxIdSupplier();
        if (maxId == null) return "NCC001";
        int next = Integer.parseInt(maxId.substring(3)) + 1;
        return String.format("NCC%03d", next);
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .idSupplier(supplier.getIdSupplier())
                .supplierName(supplier.getSupplierName())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .active(supplier.isActive())
                .build();
    }
}
