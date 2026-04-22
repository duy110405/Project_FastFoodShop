package com.fastfood.entity.system;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Role {
    @Id
    @Column(name = "id_role", length = 10)
    private String idRole;

    @Column(name = "role_name", length = 50, nullable = false)
    private String roleName;
}