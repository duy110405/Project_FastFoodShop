package com.fastfood.service.impl;

import com.fastfood.dto.request.FoodRequest;
import com.fastfood.dto.response.FoodKitchenResponse;
import com.fastfood.dto.response.FoodMenuResponse;
import com.fastfood.entity.catalog.Food;
import com.fastfood.entity.catalog.FoodIngredient;
import com.fastfood.mapper.FoodMapper;
import com.fastfood.repository.FoodRepository;
import com.fastfood.service.IFoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FoodImpl implements IFoodService {
    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    public FoodImpl(FoodRepository foodRepository, FoodMapper foodMapper) {
        this.foodRepository = foodRepository;
        this.foodMapper = foodMapper;
    }

    private String generateNextIdFood() {
        String maxId = foodRepository.findMaxidFood();
        if (maxId == null) return "H001";
        int nextNumber = Integer.parseInt(maxId.substring(1)) + 1;
        return String.format("H%03d", nextNumber);
    }

    @Override
    public List<FoodMenuResponse> getAllMenu() {
        return foodRepository.findAll().stream()
                .map(foodMapper::toFoodMenuResponse)
                .toList();
    }
    @Override
    public FoodMenuResponse getMenuById(String idFood) {
        Food food = foodRepository.findById(idFood)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn: " + idFood));
        return foodMapper.toFoodMenuResponse(food);
    }

    @Override
    public FoodKitchenResponse getFoodForKitchen(String idFood) {
        Food food = foodRepository.findById(idFood)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn mã: " + idFood));
        return foodMapper.toFoodKitchenResponse(food);
    }

    @Override
    @Transactional
    public FoodKitchenResponse createFood(FoodRequest request) {
        Food newFood = foodMapper.toFoodEntity(request);
        newFood.setIdFood(generateNextIdFood());

        if (newFood.getFoodIngredients() != null) {
            for (FoodIngredient item : newFood.getFoodIngredients()) {
                item.setFood(newFood);
            }
        }
        Food savedFood = foodRepository.save(newFood);

        return foodMapper.toFoodKitchenResponse(savedFood);
    }

    @Override
    @Transactional
    public FoodKitchenResponse updateFood(String idFood, FoodRequest request) {
        Food existingFood = foodRepository.findById(idFood)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn mã: " + idFood));

        existingFood.getFoodIngredients().clear();

        foodMapper.updateFoodFromRequest(request, existingFood);

        if (existingFood.getFoodIngredients() != null) {
            for (FoodIngredient item : existingFood.getFoodIngredients()) {
                item.setFood(existingFood);
            }
        }

        Food updatedFood = foodRepository.save(existingFood);
        return foodMapper.toFoodKitchenResponse(updatedFood);
    }

    @Override
    public void deleteFood(String idFood) {
        foodRepository.deleteById(idFood);
    }
}
