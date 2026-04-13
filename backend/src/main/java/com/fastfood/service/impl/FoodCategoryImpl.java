package com.fastfood.service.impl;

import com.fastfood.dto.request.FoodCategoryRequest;
import com.fastfood.dto.response.FoodCategoryResponse;
import com.fastfood.entity.catalog.FoodCategory;
import com.fastfood.mapper.FoodCategoryMapper;
import com.fastfood.repository.FoodCategoryRepository;
import com.fastfood.service.IFoodCategoryService;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodCategoryImpl implements IFoodCategoryService {
    public final FoodCategoryRepository foodCategoryRepository;
    public final FoodCategoryMapper foodCategoryMapper;


    public FoodCategoryImpl(FoodCategoryRepository foodCategoryRepository, FoodCategoryMapper foodCategoryMapper) {
        this.foodCategoryRepository = foodCategoryRepository;
        this.foodCategoryMapper = foodCategoryMapper;
    }
    public FoodCategory findCategoryById(String idCategory){
        return foodCategoryRepository.findById(idCategory).orElseThrow(()-> new RuntimeException("Không tìm thấy danh mục"));
    }

    @Override
    public List<FoodCategoryResponse> getAllCategory(){
        return foodCategoryRepository.findAll().stream().map(foodCategoryMapper :: toFoodCategoryResponse).toList();
    }

    @Override
    public FoodCategoryResponse getCategoryById(String idCategory){
        FoodCategory foodCategory = findCategoryById(idCategory);
        return foodCategoryMapper.toFoodCategoryResponse(foodCategory);
    }

    @Override
    public String generateNextIdCategory() {
        String maxId = foodCategoryRepository.findMaxidCategory();
        if (maxId == null) return "LH001";
        int nextNumber = Integer.parseInt(maxId.substring(2)) + 1;
        return String.format("LH%03d", nextNumber);
    }

    @Override
    @Transactional
    public FoodCategoryResponse saveCategory (FoodCategoryRequest foodCategoryRequest){
        FoodCategory foodCategory = foodCategoryMapper.toFoodCategory(foodCategoryRequest);
        foodCategory.setIdCategory(generateNextIdCategory());
         FoodCategory savedCategory = foodCategoryRepository.save(foodCategory);
         return foodCategoryMapper.toFoodCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public FoodCategoryResponse updateCategory(String idCategory , FoodCategoryRequest foodCategoryRequest){
        FoodCategory existingCategory = findCategoryById(idCategory);
        foodCategoryMapper.updateFoodCategoryFromRequest(foodCategoryRequest , existingCategory);
        FoodCategory updatedCategory = foodCategoryRepository.save(existingCategory);
        return foodCategoryMapper.toFoodCategoryResponse(updatedCategory);
    }

    @Override

    public void deleteCategory(String idCategory){
        try {
            foodCategoryRepository.deleteById(idCategory);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Không thể xóa danh mục này vì đang có món ăn sử dụng nó!");
        }
    }

}
