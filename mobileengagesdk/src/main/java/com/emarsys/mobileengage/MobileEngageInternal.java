package com.emarsys.mobileengage;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.HeaderUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class MobileEngageInternal {
    private static String ENDPOINT_BASE = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    private static String ENDPOINT_LOGIN = ENDPOINT_BASE + "users/login";
    private static String ENDPOINT_LOGOUT = ENDPOINT_BASE + "users/logout";

    private final String applicationId;
    private final String applicationSecret;
    private String pushToken;
    private final DeviceInfo deviceInfo;
    private final Application application;
    private final RequestManager manager;
    private final CoreCompletionHandler completionHandler;
    private final MobileEngageStatusListener statusListener;

    MobileEngageInternal(Application application, MobileEngageConfig config, RequestManager manager) {
        this.application = application;
        this.applicationId = config.getApplicationID();
        this.applicationSecret = config.getApplicationSecret();
        this.statusListener = config.getStatusListener();

        this.manager = manager;
        initializeRequestManager(config.getApplicationID(), config.getApplicationSecret());

        this.deviceInfo = new DeviceInfo(application.getApplicationContext());

        this.completionHandler = new CoreCompletionHandler() {
            @Override
            public void onSuccess(String s, ResponseModel responseModel) {

            }

            @Override
            public void onError(String s, Exception e) {

            }
        };
    }

    private void initializeRequestManager(String id, String secret) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", HeaderUtils.createBasicAuth(id, secret));
        this.manager.setDefaultHeaders(headers);
    }

    MobileEngageStatusListener getStatusListener() {
        return statusListener;
    }

    CoreCompletionHandler getCompletionHandler() {
        return completionHandler;
    }

    RequestManager getManager() {
        return manager;
    }

    void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    String getPushToken() {
        return pushToken;
    }

    void appLogin() {
        Map<String, Object> payload = injectLoginPayload(createBasePayload());
        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .build();

        manager.submit(model, completionHandler);
    }

    void appLogin(int contactField,
                  @NonNull String contactFieldValue) {
        Map<String, Object> additionalPayload = new HashMap<>();
        additionalPayload.put("contact_field_id", contactField);
        additionalPayload.put("contact_field_value", contactFieldValue);

        Map<String, Object> payload = injectLoginPayload(createBasePayload(additionalPayload));

        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .build();

        manager.submit(model, completionHandler);
    }

    void appLogout() {
        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_LOGOUT)
                .payload(createBasePayload())
                .build();
        manager.submit(model, completionHandler);
    }

    void trackCustomEvent(@NonNull String eventName,
                          @Nullable Map<String, String>  eventAttributes) {
    }

    private Map<String, Object> createBasePayload() {
        return createBasePayload(Collections.EMPTY_MAP);
    }

    private Map<String, Object> createBasePayload(Map<String, Object> additionalPayload) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", applicationId);
        payload.put("hardware_id", deviceInfo.getHwid());

        for (Map.Entry<String, Object> entry : additionalPayload.entrySet()) {
            payload.put(entry.getKey(), entry.getValue());
        }
        return payload;
    }

    private Map<String, Object> injectLoginPayload(Map<String, Object> payload) {
        payload.put("platform", deviceInfo.getPlatform());
        payload.put("language", deviceInfo.getLanguage());
        payload.put("timezone", deviceInfo.getTimezone());
        payload.put("device_model", deviceInfo.getModel());
        payload.put("application_version", deviceInfo.getApplicationVersion());
        payload.put("os_version", deviceInfo.getOsVersion());

        if (pushToken == null) {
            payload.put("push_token", false);
        } else {
            payload.put("push_token", pushToken);
        }

        return payload;
    }
}
