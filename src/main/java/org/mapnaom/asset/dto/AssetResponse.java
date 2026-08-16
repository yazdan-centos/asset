package org.mapnaom.asset.dto;

import org.mapnaom.asset.entity.enums.AssetGroup;
import org.mapnaom.asset.entity.enums.AssetStatus;
import org.mapnaom.asset.entity.enums.DepreciationMethod;
import org.mapnaom.asset.entity.enums.DepreciationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record AssetResponse(
        Long id,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        String plateNumber,
        String title,
        LocalDate commissioningDate,
        AssetGroup assetGroup,
        DepreciationMethod depreciationMethod,
        ReferenceValue costCenter,
        ReferenceValue project,
        ReferenceValue location,
        PersonValue custodian,
        PersonValue responsiblePerson,
        BigDecimal acquisitionCost,
        BigDecimal accumulatedDepreciation,
        BigDecimal bookValue,
        AssetStatus status,
        DepreciationStatus depreciationStatus
) {
    public record ReferenceValue(Long id, String code, String name) {
    }

    public record PersonValue(Long id, String personnelCode, String fullName) {
    }
}
