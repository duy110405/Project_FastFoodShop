package com.fastfood.entity.transaction;

import com.fastfood.entity.catalog.Ingredient;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "stock_issue_details")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockIssueDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_issue_id", nullable = false)
    StockIssue stockIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    Ingredient ingredient;

    @Column(name = "quantity", nullable = false)
    Integer quantity;
}
