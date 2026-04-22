package com.fastfood.entity.catalog;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "food_categories")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodCategory {
    @Id
    @Column(name = "id_category" , length = 20)
    String idCategory;

    @Column(name = "category_name" , length = 50)
    String categoryName;
}
