package com.e303.hotel.bean.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.baomidou.mybatisplus.annotation.EnumValue;

public enum Speed {
    HIGH("high", 3), MID("mid", 2), SLOW("slow", 1), STOP("stop", 0);

    @EnumValue // 告诉 MyBatis-Plus：这个字段要映射数据库
    private final String dbValue;

    private final int priority;

    Speed(String dbValue, int priority) {
        this.dbValue = dbValue;
        this.priority = priority;
    }

    public String getDbValue() {
        return dbValue;
    }

    public int getPriority() {
        return priority;
    }

    @JsonCreator
    public static Speed fromDbValue(String value) {
        for (Speed s : values()) {
            if (s.dbValue.equalsIgnoreCase(value)) return s;
        }
        return STOP;
    }
}

