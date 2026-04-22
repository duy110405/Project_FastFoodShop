package com.fastfood.entity.catalog;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "foods")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Food {
    @Id
    @Column(name = "id_food" , length = 20)
    String idFood ;

    @Column(name = "image_url_food", length = 500)
    private String imageUrlFood;

    @Column(name = "food_name" , length = 50)
    String foodName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category" , referencedColumnName = "id_category" , columnDefinition = "varchar(20)")
    FoodCategory foodCategory;

    @Column(name = "unit_price")
    BigDecimal unitPrice ;

    @Column(name = "description", length = 500)
    private String description;

    // Dòng này giúp Spring Boot tự động móc sang bảng FoodIngredient để lấy nguyên liệu
    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FoodIngredient> foodIngredients = new ArrayList<>();
}
