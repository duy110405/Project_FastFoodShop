package com.fastfood.entity.transaction;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import com.fastfood.entity.catalog.Food;

@Entity
@Table(name = "order_details")
@Data @NoArgsConstructor @AllArgsConstructor
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Thêm ID giả định để JPA dễ quản lý

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "food_id")
    private Food food;

    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    private String status; // VD: PENDING, SERVED
}
