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
 * محل استقرار — physical location where an asset is deployed (site / building / room).
 */
@Getter
@Setter
@ToString(callSuper = false)
@Entity
@Table(name = "location", uniqueConstraints = @UniqueConstraint(name = "uk_location_code", columnNames = "code"))
public class Location extends BaseEntity {

    @EqualsAndHashCode.Include
    @NotBlank
    @Size(max = 32)
    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Location location = (Location) o;
        return getId() != null && Objects.equals(getId(), location.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
