package com.fastfood.entity.catalog;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Entity
@Table(name = "food_ingredients")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodIngredient {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_food" , referencedColumnName = "id_food" , columnDefinition = "varchar(20)")
    @JsonIgnore // cắt đứt vòng lặp mã food
    Food food;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ingredient" , referencedColumnName = "id_ingredient" , columnDefinition = "varchar(20)")
    Ingredient ingredient;

    @Column(name = "quantity_used", nullable = false)
    Double quantityUsed;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class FoodIngredientId implements Serializable {
        String food ;
        String ingredient;
    }
}
