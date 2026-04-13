package com.fastfood.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupplierRequest {
    String supplierName;
    String phone;
    String email;
    String address;
    Boolean active;
}
