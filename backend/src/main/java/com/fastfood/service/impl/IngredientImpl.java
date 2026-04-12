package com.fastfood.service.impl;

import com.fastfood.dto.request.IngredientRequest;
import com.fastfood.dto.response.IngredientResponse;
import com.fastfood.entity.catalog.Ingredient;
import com.fastfood.mapper.IngredientMapper;
import com.fastfood.repository.IngredientRepository;
import com.fastfood.service.IIngredientService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientImpl implements IIngredientService {
    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public IngredientImpl(IngredientRepository ingredientRepository, IngredientMapper ingredientMapper) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
    }
    // tìm nguyên liệu theo mã
    public Ingredient findIngredientById (String idIngredient){
           return ingredientRepository.findById(idIngredient)
                   .orElseThrow(() -> new RuntimeException("Không tìm thấy mã "));
    }

    // hàm lấy tất cả
    @Override
    public List<IngredientResponse> getAllIngredient(){
          return  ingredientRepository.findAll().stream().map(ingredientMapper :: toIngredientResponse).toList();
    }

    // hàm lấy theo id
    @Override
    public IngredientResponse getIngredientById(String idIngredient){
           Ingredient ingredient = findIngredientById(idIngredient);
           return ingredientMapper.toIngredientResponse(ingredient);
    }

    @Override
    // hàm tự sinh mã
    public String generateNextIdIngredient() {
        String maxId = ingredientRepository.findMaxidIngredient();
        if (maxId == null) return "NL001";
        // Tách phần số: NL005 -> 5
        int nextNumber = Integer.parseInt(maxId.substring(2)) + 1;
        // Định dạng lại thành NL + 3 chữ số: NL006
        return String.format("NL%03d", nextNumber);
    }

    // hàm lưu
    @Override
    public IngredientResponse saveIngredient (IngredientRequest ingredientRequest){
        //Map từ Dto sang entity
        Ingredient ingredient =ingredientMapper.toIngredientEntity(ingredientRequest);
        //tự sinh mã
        ingredient.setIdIngredient(generateNextIdIngredient());
        // lưu xuống db
         Ingredient savedIngredient = ingredientRepository.save(ingredient);
         //Trả về DTO
        return ingredientMapper.toIngredientResponse(savedIngredient);
    }

    // hàm update
    @Override
    public IngredientResponse updateIngredient (IngredientRequest ingredientRequest , String idIngredient){
        // tìm entity cũ trong db
        Ingredient existingIngredient =findIngredientById(idIngredient);
        //Dùng MapStruct đổ tự động các trường từ Request sang Entity cũ
        ingredientMapper.updateIngredienFromRequest(ingredientRequest , existingIngredient);
        //lưu và map sang response
        Ingredient updatedIngredient = ingredientRepository.save(existingIngredient);
        return ingredientMapper.toIngredientResponse(updatedIngredient);
    }

    // Hàm xóa
    public void deleteIngredient(String idIngredient ){
         ingredientRepository.deleteById(idIngredient);
    }
}
