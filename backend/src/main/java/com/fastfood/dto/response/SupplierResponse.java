package com.fastfood.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupplierResponse {
    String idSupplier;
    String supplierName;
    String phone;
    String email;
    String address;
    boolean active;
}
