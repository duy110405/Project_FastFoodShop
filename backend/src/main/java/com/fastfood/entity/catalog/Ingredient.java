package com.fastfood.entity.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "ingredients")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @Column(name = "id_ingredient", length = 20)
    String idIngredient;

    @Column(name = "image_url_Ingredient", length = 500)
    private String imageUrlIngredient;

    @Column(name = "ingredient_name", nullable = false, length = 100)
    String ingredientName;

    @Column(name = "unit", length = 20)
    String unit;

    @Column(name = "quantity_stock", precision = 18, scale = 2)
    BigDecimal quantityStock;
}