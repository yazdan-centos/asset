package org.mapnaom.asset.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.mapnaom.asset.entity.enums.AssetStatus;
import org.mapnaom.asset.entity.enums.DepreciationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetRequest(
        @NotBlank @Size(max = 64) String plateNumber,
        @NotBlank @Size(max = 255) String title,
        @NotNull @PastOrPresent LocalDate commissioningDate,
        @NotBlank @Size(max = 100) String assetGroup,
        @NotBlank @Size(max = 100) String depreciationMethod,
        @NotBlank String costCenterCode,
        String projectCode,
        @NotBlank String locationCode,
        @NotBlank String custodianPersonnelCode,
        @NotBlank String responsiblePersonnelCode,
        @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal acquisitionCost,
        @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 4) BigDecimal accumulatedDepreciation,
        @NotNull AssetStatus status,
        @NotNull DepreciationStatus depreciationStatus
) {
}
