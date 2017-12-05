package com.emarsys.mobileengage.testUtil;

import com.emarsys.mobileengage.experimental.Experimental;

import java.lang.reflect.Field;
import java.util.Set;

public class ExperimentalTestUtils {

    private ExperimentalTestUtils() {
    }

    public static void resetExperimentalFeatures() throws NoSuchFieldException, IllegalAccessException {
        Field experimentalSet = Experimental.class.getDeclaredField("enabledFeatures");
        experimentalSet.setAccessible(true);
        Set<String> features = (Set<String>) experimentalSet.get(null);
        features.clear();
    }
}
