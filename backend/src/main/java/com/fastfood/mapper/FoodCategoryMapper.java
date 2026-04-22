package com.fastfood.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.fastfood.dto.request.FoodCategoryRequest;
import com.fastfood.dto.response.FoodCategoryResponse;
import com.fastfood.entity.catalog.FoodCategory;

@Mapper(componentModel = "spring")
public interface FoodCategoryMapper {

    FoodCategoryResponse toResponse(FoodCategory foodCategory);

    @Mapping(target = "idCategory", ignore = true)
    FoodCategory toEntity(FoodCategoryRequest request);

    @Mapping(target = "idCategory", ignore = true)
    void updateEntity(FoodCategoryRequest request, @MappingTarget FoodCategory foodCategory);
}