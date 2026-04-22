package com.fastfood.service;

import com.fastfood.dto.request.OrderRequest;
import com.fastfood.dto.request.PaymentRequest;
import com.fastfood.dto.response.CashierOrderDetailResponse;
import com.fastfood.dto.response.CashierPaymentResponse;
import com.fastfood.dto.response.CashierTableStatusResponse;
import com.fastfood.entity.transaction.Order;

import java.util.List;
import java.util.Set;

public interface ISalesService {
    
    // Hỗ trợ sinh mã
    String generateNextOrderId();
    String generateNextInvoiceId();

    // Nghiệp vụ POS
    Set<String> getOccupiedTableNumbers();
    List<CashierTableStatusResponse> getTableStatuses();
    CashierOrderDetailResponse getPendingOrderByTable(String tableNumber);
    Order placeOrder(OrderRequest request);
    CashierPaymentResponse processPayment(PaymentRequest request);
    void completeOrder(String orderId);
}
