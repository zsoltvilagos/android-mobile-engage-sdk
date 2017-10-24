package com.emarsys.mobileengage.experimental;

public enum MobileEngageFeature implements FlipperFeature {

    IN_APP_MESSAGING;

    @Override
    public String getName() {
        return name().toLowerCase();
    }
}
