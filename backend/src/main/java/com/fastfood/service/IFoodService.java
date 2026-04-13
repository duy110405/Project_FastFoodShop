package com.fastfood.service;

import com.fastfood.dto.request.FoodRequest;
import com.fastfood.dto.response.FoodKitchenResponse;
import com.fastfood.dto.response.FoodMenuResponse;

import java.util.List;

public interface IFoodService {
    List<FoodMenuResponse> getAllMenu();
    FoodMenuResponse getMenuById(String idFood);
    FoodKitchenResponse getFoodForKitchen(String idFood);
    FoodKitchenResponse createFood(FoodRequest request);
    FoodKitchenResponse updateFood(String idFood, FoodRequest request);
    void deleteFood(String idFood);
}
