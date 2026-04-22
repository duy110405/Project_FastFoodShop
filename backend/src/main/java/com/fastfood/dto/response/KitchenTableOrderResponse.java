package com.fastfood.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class KitchenTableOrderResponse {
    private String tableNumber;
    private LocalDateTime firstOrderTime;
    private long elapsedMinutes;
    private List<KitchenOrderItemResponse> items = new ArrayList<>();
}

