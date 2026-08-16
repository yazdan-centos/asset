package org.mapnaom.asset.entity;

import org.mapnaom.asset.entity.enums.AssetStatus;
import org.mapnaom.asset.entity.enums.DepreciationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString(callSuper = false, exclude = {"costCenter", "project", "location", "custodian", "responsiblePerson"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "asset",
        uniqueConstraints = @UniqueConstraint(name = "uk_asset_plate_number", columnNames = "plate_number"),
        indexes = {
                @Index(name = "idx_asset_cost_center", columnList = "cost_center_id"),
                @Index(name = "idx_asset_project", columnList = "project_id"),
                @Index(name = "idx_asset_location", columnList = "location_id"),
                @Index(name = "idx_asset_custodian", columnList = "custodian_id"),
                @Index(name = "idx_asset_responsible_person", columnList = "responsible_person_id"),
                @Index(name = "idx_asset_status", columnList = "status"),
                @Index(name = "idx_asset_depreciation_status", columnList = "depreciation_status")
        }
)
public class Asset extends BaseEntity {

    // شماره پلاک
    @EqualsAndHashCode.Include
    @NotBlank
    @Size(max = 64)
    @Column(name = "plate_number", nullable = false, length = 64)
    private String plateNumber;

    // عنوان
    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    // تاریخ بهره برداری
    @NotNull
    @PastOrPresent
    @Column(name = "commissioning_date", nullable = false)
    private LocalDate commissioningDate;

    // گروه
    @NotBlank
    @Size(max = 100)
    @Column(name = "asset_group", nullable = false, length = 100)
    private String assetGroup;

    // روش استهلاک
    @NotBlank
    @Size(max = 100)
    @Column(name = "depreciation_method", nullable = false, length = 100)
    private String depreciationMethod;

    // مرکز هزینه
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id", nullable = false, foreignKey = @ForeignKey(name = "fk_asset_cost_center"))
    private CostCenter costCenter;

    // پروژه
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "fk_asset_project"))
    private Project project;

    // محل استقرار
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_asset_location"))
    private Location location;

    // جمع دار
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodian_id", nullable = false, foreignKey = @ForeignKey(name = "fk_asset_custodian"))
    private Person custodian;

    // مسئول
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_asset_responsible_person"))
    private Person responsiblePerson;

    // بهای تمام شده
    @NotNull
    @PositiveOrZero
    @Digits(integer = 15, fraction = 4)
    @Column(name = "acquisition_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal acquisitionCost;

    // استهلاک انباشته
    @NotNull
    @PositiveOrZero
    @Digits(integer = 15, fraction = 4)
    @Column(name = "accumulated_depreciation", nullable = false, precision = 19, scale = 4)
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    // وضعیت
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AssetStatus status;

    // وضعیت استهلاک
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_status", nullable = false, length = 32)
    private DepreciationStatus depreciationStatus;

    /**
     * ارزش دفتری — derived, never persisted.
     * bookValue = acquisitionCost - accumulatedDepreciation, computed on read to avoid
     * a second source of truth that could drift from the underlying columns.
     */
    @Transient
    public BigDecimal getBookValue() {
        if (acquisitionCost == null || accumulatedDepreciation == null) {
            return null;
        }
        return acquisitionCost.subtract(accumulatedDepreciation);
    }
}
