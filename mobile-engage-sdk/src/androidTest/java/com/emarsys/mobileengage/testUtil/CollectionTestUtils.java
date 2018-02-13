package com.emarsys.mobileengage.testUtil;

import java.util.List;

public class CollectionTestUtils {
    public static int numberOfElementsIn(List list, Class type) {
        int count = 0;

        for (Object object : list) {
            if (object.getClass().equals(type)) {
                count++;
            }
        }

        return count;
    }

    public static int numberOfElementsIn(Object[] array, Class type) {
        int count = 0;

        for (Object object : array) {
            if (object.getClass().equals(type)) {
                count++;
            }
        }

        return count;
    }
}
