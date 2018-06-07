package com.emarsys.mobileengage.testUtil;

import java.lang.reflect.Field;

public class ReflectionTestUtils {

    public static void setStaticField(Class type, String fieldName, Object value) throws Exception {
        Field containerField = type.getDeclaredField(fieldName);
        containerField.setAccessible(true);
        containerField.set(null, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getStaticField(Class type, String fieldName) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object result = field.get(null);
        return (T) result;
    }

}
