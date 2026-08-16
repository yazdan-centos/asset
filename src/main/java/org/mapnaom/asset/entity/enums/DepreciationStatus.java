package org.mapnaom.asset.entity.enums;

public enum DepreciationStatus {
    NOT_DEPRECIATED,
    NON_DEPRECIABLE,
    DEPRECIATED;

    public String getPersianCaption() {
        return switch (this) {
            case NOT_DEPRECIATED -> "مستهلک نشده";
            case NON_DEPRECIABLE -> "استهلاک ناپذیر";
            case DEPRECIATED -> "مستهلک شده";
        };
    }
}
