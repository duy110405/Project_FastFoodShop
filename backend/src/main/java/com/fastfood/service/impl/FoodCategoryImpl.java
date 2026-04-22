package com.fastfood.service.impl;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fastfood.dto.request.FoodCategoryRequest;
import com.fastfood.dto.response.FoodCategoryResponse;
import com.fastfood.entity.catalog.FoodCategory;
import com.fastfood.mapper.FoodCategoryMapper;
import com.fastfood.repository.FoodCategoryRepository;
import com.fastfood.service.IFoodCategoryService;

import jakarta.transaction.Transactional;

@Service
public class FoodCategoryImpl implements IFoodCategoryService {

    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodCategoryMapper foodCategoryMapper;

    public FoodCategoryImpl(FoodCategoryRepository foodCategoryRepository,
                            FoodCategoryMapper foodCategoryMapper) {
        this.foodCategoryRepository = foodCategoryRepository;
        this.foodCategoryMapper = foodCategoryMapper;
    }

    @Override
    public List<FoodCategoryResponse> getAllCategory() {
        return foodCategoryRepository.findAll()
                .stream()
                .map(foodCategoryMapper::toResponse)
                .toList();
    }

    @Override
    public FoodCategoryResponse getCategoryById(String idCategory) {
        FoodCategory foodCategory = findById(idCategory);
        return foodCategoryMapper.toResponse(foodCategory);
    }

    @Override
    @Transactional
    public FoodCategoryResponse saveCategory(FoodCategoryRequest request) {
        FoodCategory foodCategory = foodCategoryMapper.toEntity(request);
        foodCategory.setIdCategory(generateNextIdCategory());
        FoodCategory saved = foodCategoryRepository.save(foodCategory);
        return foodCategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FoodCategoryResponse updateCategory(String idCategory, FoodCategoryRequest request) {
        FoodCategory foodCategory = findById(idCategory);
        foodCategoryMapper.updateEntity(request, foodCategory);
        FoodCategory updated = foodCategoryRepository.save(foodCategory);
        return foodCategoryMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(String idCategory) {
        FoodCategory foodCategory = findById(idCategory);
        try {
            foodCategoryRepository.delete(foodCategory);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Không thể xóa danh mục vì đang được sử dụng");
        }
    }

    @Override
    public String generateNextIdCategory() {
        String maxId = foodCategoryRepository.findMaxIdCategory();
        if (maxId == null) return "LH001";
        int next = Integer.parseInt(maxId.substring(2)) + 1;
        return String.format("LH%03d", next);
    }

    private FoodCategory findById(String idCategory) {
        return foodCategoryRepository.findById(idCategory)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục: " + idCategory));
    }
}