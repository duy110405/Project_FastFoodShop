package com.fastfood.service.impl;

import com.fastfood.dto.request.FoodRequest;
import com.fastfood.dto.response.FoodCostIngredientResponse;
import com.fastfood.dto.response.FoodCostResponse;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
                .map(food -> {
                    // Mapper chuyển đổi các trường cơ bản
                    FoodMenuResponse response = foodMapper.toFoodMenuResponse(food);
                    // Chạy hàm tính toán kho ngầm và set vào cờ
                    response.setAvailable(checkFoodAvailability(food));
                    return response;
                })
                .toList();
    }
    @Override
    public FoodMenuResponse getMenuById(String idFood) {
        Food food = foodRepository.findById(idFood)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn: " + idFood));
        FoodMenuResponse response = foodMapper.toFoodMenuResponse(food);
        response.setAvailable(checkFoodAvailability(food));

        return response;
    }

    @Override
    public FoodKitchenResponse getFoodForKitchen(String idFood) {
        Food food = foodRepository.findById(idFood)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn mã: " + idFood));
        return foodMapper.toFoodKitchenResponse(food);
    }

    @Override
    public List<FoodCostResponse> getFoodCosts() {
        return foodRepository.findAllWithIngredients().stream()
                .map(this::toFoodCostResponse)
                .toList();
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
        //  Tìm món ăn cũ dưới Database
        Food existingFood = foodRepository.findById(idFood)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn mã: " + idFood));

        // MapStruct sẽ cập nhật Tên, Giá, Ảnh, Danh mục... (Nhưng BỎ QUA list nguyên liệu)
        foodMapper.updateFoodFromRequest(request, existingFood);

        // XÓA SẠCH nguyên liệu cũ của món này
        existingFood.getFoodIngredients().clear();

        // THÊM BẰNG TAY nguyên liệu mới từ Request (Frontend gửi xuống)
        if (request.getIngredients() != null) {
            // Duyệt qua từng nguyên liệu mới
            for (var ingredientReq : request.getIngredients()) {
                // Biến DTO thành Entity nhờ Mapper
                FoodIngredient newIngredientItem = foodMapper.toFoodIngredientEntity(ingredientReq);
                // Trói khóa ngoại: Bắt con (nguyên liệu) nhận cha (món ăn)
                newIngredientItem.setFood(existingFood);
                // Nhét đứa con vào danh sách của cha
                existingFood.getFoodIngredients().add(newIngredientItem);
            }
        }
        // Lưu lại một cục (Hibernate sẽ tự động ra lệnh DELETE cũ và INSERT mới)
        Food updatedFood = foodRepository.save(existingFood);
        return foodMapper.toFoodKitchenResponse(updatedFood);
    }

    @Override
    public void deleteFood(String idFood) {
        foodRepository.deleteById(idFood);
    }

    // Hàm chạy ngầm kiểm tra kho cho 1 món ăn
    private boolean checkFoodAvailability(Food food) {
        if (food.getFoodIngredients() == null || food.getFoodIngredients().isEmpty()) {
            return true; // Món không cần nguyên liệu thì coi như luôn có sẵn
        }
        for (FoodIngredient recipe : food.getFoodIngredients()) {
            // Tránh NullPointerException bằng cách lấy giá trị an toàn
            java.math.BigDecimal stock = recipe.getIngredient().getQuantityStock() != null
                    ? recipe.getIngredient().getQuantityStock()
                    : java.math.BigDecimal.ZERO;

            java.math.BigDecimal used = recipe.getQuantityUsed() != null
                    ? recipe.getQuantityUsed()
                    : java.math.BigDecimal.ZERO;

            // Nếu có bất kỳ nguyên liệu nào không đủ -> Hết hàng
            if (stock.compareTo(used) < 0) {
                return false;
            }
        }
        return true; // Tất cả nguyên liệu đều đủ -> Còn hàng
    }

    private FoodCostResponse toFoodCostResponse(Food food) {
        List<FoodCostIngredientResponse> ingredientCosts = (food.getFoodIngredients() == null ? List.<FoodIngredient>of() : food.getFoodIngredients())
                .stream()
                .map(this::toIngredientCost)
                .toList();

        BigDecimal productionCost = ingredientCosts.stream()
                .map(FoodCostIngredientResponse::getCostAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal salePrice = safeValue(food.getUnitPrice());
        BigDecimal grossProfit = salePrice.subtract(productionCost);
        BigDecimal marginPercent = BigDecimal.ZERO;
        if (salePrice.compareTo(BigDecimal.ZERO) > 0) {
            marginPercent = grossProfit
                    .divide(salePrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return FoodCostResponse.builder()
                .idFood(food.getIdFood())
                .foodName(food.getFoodName())
                .salePrice(salePrice)
                .productionCost(productionCost)
                .grossProfit(grossProfit)
                .marginPercent(marginPercent)
                .ingredients(ingredientCosts)
                .build();
    }

    private FoodCostIngredientResponse toIngredientCost(FoodIngredient item) {
        BigDecimal quantityUsed = safeValue(item.getQuantityUsed());
        BigDecimal importPrice = item.getIngredient() != null ? safeValue(item.getIngredient().getImportPrice()) : BigDecimal.ZERO;
        BigDecimal costAmount = quantityUsed.multiply(importPrice);

        return FoodCostIngredientResponse.builder()
                .ingredientId(item.getIngredient() != null ? item.getIngredient().getIdIngredient() : null)
                .ingredientName(item.getIngredient() != null ? item.getIngredient().getIngredientName() : null)
                .unit(item.getIngredient() != null ? item.getIngredient().getUnit() : null)
                .quantityUsed(quantityUsed)
                .importPrice(importPrice)
                .costAmount(costAmount)
                .build();
    }

    private BigDecimal safeValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
