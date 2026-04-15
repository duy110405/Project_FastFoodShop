package com.fastfood.entity.transaction;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_invoices")
@Data @NoArgsConstructor @AllArgsConstructor
public class SalesInvoice {
    @Id
    @Column(name = "id_invoice")
    private String idInvoice;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_method")
    private String paymentMethod; // CASH, TRANSFER

    @Column(name = "total_amount")
    private BigDecimal totalAmount;
}
