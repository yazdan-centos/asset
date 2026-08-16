package org.mapnaom.asset.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssetGroup {
    TECHNICAL_TOOL_STRAIGHT_10_YEARS,
    OFFICE_FURNITURE_STRAIGHT_15_YEARS,
    OFFICE_FURNITURE_STRAIGHT_10_YEARS,
    OFFICE_FURNITURE_STRAIGHT_3_YEARS,
    OFFICE_FURNITURE_STRAIGHT_5_YEARS,
    LAND,
    BUILDING_STRAIGHT_15_YEARS,
    BUILDING_STRAIGHT_25_YEARS,
    SOFTWARE_STRAIGHT_3_YEARS,
    VEHICLE_STRAIGHT_4_YEARS,
    VEHICLE_STRAIGHT_6_YEARS;

    public String getPersianCaption() {
        return switch (this) {
            case TECHNICAL_TOOL_STRAIGHT_10_YEARS -> "ابزارفنی - مستقیم 10ساله";
            case OFFICE_FURNITURE_STRAIGHT_15_YEARS -> "اثاثه اداری - 15 ساله مستقیم";
            case OFFICE_FURNITURE_STRAIGHT_10_YEARS -> "اثاثه اداری - مستقیم 10 ساله";
            case OFFICE_FURNITURE_STRAIGHT_3_YEARS -> "اثاثه اداری - مستقیم 3 ساله";
            case OFFICE_FURNITURE_STRAIGHT_5_YEARS -> "اثاثه اداری - مستقیم 5 ساله";
            case LAND -> "زمین";
            case BUILDING_STRAIGHT_15_YEARS -> "ساختمان خط مستقیم 15 ساله";
            case BUILDING_STRAIGHT_25_YEARS -> "ساختمان مستقیم 25 ساله";
            case SOFTWARE_STRAIGHT_3_YEARS -> "نرم افزار - مستقیم 3 ساله";
            case VEHICLE_STRAIGHT_4_YEARS -> "وسایط نقلیه مستقیم 4 ساله";
            case VEHICLE_STRAIGHT_6_YEARS -> "وسائط نقلیه مستقیم 6 ساله";
        };
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AssetGroup fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Asset group is required");
        }
        String trimmed = value.trim();
        for (AssetGroup item : values()) {
            if (item.name().equalsIgnoreCase(trimmed)
                    || item.getPersianCaption().equals(trimmed)
                    || normalize(item.getPersianCaption()).equals(normalize(trimmed))) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unsupported asset group: " + value);
    }

    @JsonValue
    public String toValue() {
        return name();
    }

    public static AssetGroup fromText(String value) {
        return fromValue(value);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replace(" ", "").replace("-", "").replace("_", "").trim();
    }
}
