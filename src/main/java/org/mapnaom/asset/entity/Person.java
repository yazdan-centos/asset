package org.mapnaom.asset.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Used as both جمع دار (custodian) and مسئول (responsible person) on Asset.
 */
@Getter
@Setter
@ToString(callSuper = false)
@Entity
@Table(name = "person", uniqueConstraints = @UniqueConstraint(name = "uk_person_personnel_code", columnNames = "personnel_code"))
public class Person extends BaseEntity {

    @EqualsAndHashCode.Include
    @NotBlank
    @Size(max = 32)
    @Column(name = "personnel_code", nullable = false, length = 32)
    private String personnelCode;

    @NotBlank
    @Size(max = 150)
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Person person = (Person) o;
        return getId() != null && Objects.equals(getId(), person.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
