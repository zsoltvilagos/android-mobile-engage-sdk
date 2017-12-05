package com.emarsys.mobileengage.testUtil;

import com.emarsys.mobileengage.experimental.MobileEngageExperimental;

import java.lang.reflect.Field;
import java.util.Set;

public class ExperimentalTestUtils {

    private ExperimentalTestUtils() {
    }

    public static void resetExperimentalFeatures() throws NoSuchFieldException, IllegalAccessException {
        Field experimentalSet = MobileEngageExperimental.class.getDeclaredField("enabledFeatures");
        experimentalSet.setAccessible(true);
        Set<String> features = (Set<String>) experimentalSet.get(null);
        features.clear();
    }
}
