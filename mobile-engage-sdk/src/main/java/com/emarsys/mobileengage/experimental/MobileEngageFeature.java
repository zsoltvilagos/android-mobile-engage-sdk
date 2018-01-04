package com.emarsys.mobileengage.experimental;

import java.util.Locale;

public enum MobileEngageFeature implements FlipperFeature {

    IN_APP_MESSAGING;

    @Override
    public String getName() {
        return name().toLowerCase(Locale.US);
    }
}
