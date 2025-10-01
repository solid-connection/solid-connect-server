package com.example.solidconnection.common.helper;

import com.example.solidconnection.common.BaseEntity;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TestTimeHelper {
    public static void setCreatedAt(BaseEntity entity, ZonedDateTime time) {
        try {
            Field field = BaseEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(entity, time.truncatedTo(ChronoUnit.MICROS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
