package com.fastfood.service;

import com.fastfood.dto.request.IngredientRequest;
import com.fastfood.dto.response.IngredientResponse;
import com.fastfood.repository.IngredientRepository;

import java.util.List;

public interface IIngredientService {
    List<IngredientResponse> getAllIngredient();
    IngredientResponse getIngredientById(String idIngredient);
    IngredientResponse saveIngredient(IngredientRequest ingredientRequest);
    IngredientResponse updateIngredient(IngredientRequest ingredientRequest , String idIngredient);
    void deleteIngredient (String idIngredient);
    String generateNextIdIngredient();
}
