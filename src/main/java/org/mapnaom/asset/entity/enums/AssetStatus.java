package org.mapnaom.asset.entity.enums;

public enum AssetStatus {
    PLATED,
    PENDING_TRANSFER,
    OUT_OF_ORGANIZATION,
    SOLD,
    SCRAPPED,
    DELETED,
    PENDING_EXIT_FROM_ORGANIZATION,
    ASSET_SET_ASIDE,
    TEMPORARY_EXIT,
    TRANSFERRED_TO_WAREHOUSE,
    REPLATED;

    public String getPersianCaption() {
        return switch (this) {
            case PLATED -> "پلاک شده";
            case PENDING_TRANSFER -> "در انتظار انتقال";
            case OUT_OF_ORGANIZATION -> "خارج شده از سازمان";
            case SOLD -> "فروش رفته";
            case SCRAPPED -> "اسقاط شده";
            case DELETED -> "حذف شده";
            case PENDING_EXIT_FROM_ORGANIZATION -> "در انتظار خروج از سازمان";
            case ASSET_SET_ASIDE -> "کنارگذاری دارایی";
            case TEMPORARY_EXIT -> "خروج موقت";
            case TRANSFERRED_TO_WAREHOUSE -> "انتقال به انبار";
            case REPLATED -> "پلاک گذاری مجدد";
        };
    }
}
