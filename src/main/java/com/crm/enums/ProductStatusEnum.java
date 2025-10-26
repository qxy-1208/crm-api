package com.crm.enums;

import lombok.Getter;

@Getter
public enum ProductStatusEnum {
    INIT(0, "初始化"),
    ON_SALE(1, "上架"),
    OFF_SALE(2, "下架");

    private final Integer code;
    private final String desc;

    ProductStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Byte code) {
        if (code == null) {
            return "";
        }
        for (ProductStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status.desc;
            }
        }
        return "";
    }
}