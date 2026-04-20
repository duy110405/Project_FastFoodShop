package com.fastfood.service;

import com.fastfood.dto.response.KitchenFoodPendingResponse;
import com.fastfood.dto.response.KitchenTableOrderResponse;

import java.util.List;

public interface IKitchenService {
    List<KitchenTableOrderResponse> getPendingOrdersByTable();
    List<KitchenFoodPendingResponse> getRemainingFoodSummary();
    List<KitchenTableOrderResponse> getCompletedOrders();
    void markOrderItemServed(Long orderDetailId);
}


