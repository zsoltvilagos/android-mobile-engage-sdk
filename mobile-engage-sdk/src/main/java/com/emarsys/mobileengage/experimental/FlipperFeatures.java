package com.emarsys.mobileengage.experimental;

public enum FlipperFeatures implements FlipperFeature {

    INAPP_MESSAGING("inapp_messaging");

    private String name;

    FlipperFeatures(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
