package com.emarsys.mobileengage.endpoint;

public class Endpoint {
    public static final String ME_BASE_V2 = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    public static final String ME_LOGIN_V2 = ME_BASE_V2 + "users/login";
    public static final String ME_LOGOUT_V2 = ME_BASE_V2 + "users/logout";
    public static final String ME_LAST_MOBILE_ACTIVITY_V2 = ME_BASE_V2 + "events/ems_lastMobileActivity";

    public static final String ME_BASE_V3 = "https://mobile-events.eservice.emarsys.net/v3/devices/";

    public static final String INBOX_BASE = "https://me-inbox.eservice.emarsys.net/api/";
    public static final String INBOX_RESET_BADGE_COUNT_V1 = INBOX_BASE + "reset-badge-count";
    public static final String INBOX_FETCH_V1 = INBOX_BASE + "notifications";
    public static final String INBOX_FETCH_V2 = INBOX_BASE + "v1/notifications/%s";

    public static final String DEEP_LINK_BASE = "https://deep-link.eservice.emarsys.net/api/";
    public static final String DEEP_LINK_CLICK = DEEP_LINK_BASE + "clicks";
}
