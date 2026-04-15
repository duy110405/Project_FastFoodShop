package com.fastfood.service;

import com.fastfood.dto.request.OrderRequest;
import com.fastfood.dto.request.PaymentRequest;
import com.fastfood.entity.transaction.Order;
import com.fastfood.entity.transaction.SalesInvoice;

import java.util.Set;

public interface ISalesService {
    
    // Hỗ trợ sinh mã
    String generateNextOrderId();
    String generateNextInvoiceId();

    // Nghiệp vụ POS
    Set<String> getOccupiedTableNumbers();
    Order placeOrder(OrderRequest request);
    SalesInvoice processPayment(PaymentRequest request);
}
