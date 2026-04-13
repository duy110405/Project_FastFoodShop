package com.fastfood.entity.transaction;

import com.fastfood.entity.catalog.Ingredient;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "stock_request_details")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockRequestDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_request_id", nullable = false)
    StockRequest stockRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    Ingredient ingredient;

    @Column(name = "requested_quantity", nullable = false)
    Integer requestedQuantity;
}
