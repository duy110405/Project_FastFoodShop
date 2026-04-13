package com.fastfood.entity.transaction;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_requests")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockRequest {
    @Id
    @Column(name = "id_stock_request", length = 20)
    String idStockRequest;

    @Column(name = "request_date", nullable = false)
    LocalDate requestDate;

    @Column(name = "requested_by", nullable = false, length = 100)
    String requestedBy;

    @Column(name = "status", nullable = false, length = 20)
    String status;

    @Column(name = "note", length = 255)
    String note;

    @OneToMany(mappedBy = "stockRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<StockRequestDetail> details = new ArrayList<>();
}
