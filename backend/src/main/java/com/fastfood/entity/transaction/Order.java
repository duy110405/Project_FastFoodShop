package com.fastfood.entity.transaction;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data @NoArgsConstructor @AllArgsConstructor
public class Order {
    @Id
    @Column(name = "id_order")
    private String idOrder;

    @Column(name = "table_number")
    private String tableNumber;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "order_time")
    private LocalDateTime orderTime;

    @Column(name = "status")
    private String status; // VD: PENDING, COMPLETED

    @Column(name = "created_by")
    private String createdBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;
}
