package org.mapnaom.asset.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_role", uniqueConstraints = @UniqueConstraint(name = "uk_app_role_name", columnNames = "name"))
public class Role extends BaseEntity {

    @NotBlank
    @Size(max = 64)
    @Column(name = "name", nullable = false, length = 64)
    private String name;
}
