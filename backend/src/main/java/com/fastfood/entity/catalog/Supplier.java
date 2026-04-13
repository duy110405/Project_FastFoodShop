package com.fastfood.entity.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "suppliers")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {
    @Id
    @Column(name = "id_supplier", length = 20)
    String idSupplier;

    @Column(name = "supplier_name", nullable = false, length = 100)
    String supplierName;

    @Column(name = "phone", length = 20)
    String phone;

    @Column(name = "email", length = 100)
    String email;

    @Column(name = "address", length = 255)
    String address;

    @Column(name = "is_active")
    boolean active = true;
}
