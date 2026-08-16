package org.mapnaom.asset.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DepreciationMethod {
    STRAIGHT_LINE_10_YEARS,
    STRAIGHT_LINE_15_YEARS,
    STRAIGHT_LINE_25_YEARS,
    STRAIGHT_LINE_3_YEARS,
    STRAIGHT_LINE_4_YEARS,
    STRAIGHT_LINE_5_YEARS,
    STRAIGHT_LINE_6_YEARS;

    public String getPersianCaption() {
        return switch (this) {
            case STRAIGHT_LINE_10_YEARS -> "خط مستقیم 10 ساله";
            case STRAIGHT_LINE_15_YEARS -> "خط مستقیم 15 ساله";
            case STRAIGHT_LINE_25_YEARS -> "خط مستقیم 25 ساله";
            case STRAIGHT_LINE_3_YEARS -> "خط مستقیم 3 ساله";
            case STRAIGHT_LINE_4_YEARS -> "خط مستقیم 4 ساله";
            case STRAIGHT_LINE_5_YEARS -> "خط مستقیم 5 ساله";
            case STRAIGHT_LINE_6_YEARS -> "خط مستقیم 6 ساله";
        };
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DepreciationMethod fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Depreciation method is required");
        }
        String trimmed = value.trim();
        for (DepreciationMethod item : values()) {
            if (item.name().equalsIgnoreCase(trimmed)
                    || item.getPersianCaption().equals(trimmed)
                    || normalize(item.getPersianCaption()).equals(normalize(trimmed))) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unsupported depreciation method: " + value);
    }

    @JsonValue
    public String toValue() {
        return name();
    }

    public static DepreciationMethod fromText(String value) {
        return fromValue(value);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replace(" ", "").replace("-", "").replace("_", "").trim();
    }
}
