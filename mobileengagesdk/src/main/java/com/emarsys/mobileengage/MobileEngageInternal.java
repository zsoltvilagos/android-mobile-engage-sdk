package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.HeaderUtils;

import org.json.JSONException;
import org.json.JSONObject;

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
    private final Handler handler = new Handler(Looper.getMainLooper());

    MobileEngageInternal(final Application application, MobileEngageConfig config, RequestManager manager) {
        this.application = application;
        this.applicationId = config.getApplicationID();
        this.applicationSecret = config.getApplicationSecret();
        this.statusListener = config.getStatusListener();

        this.manager = manager;
        initializeRequestManager(config.getApplicationID(), config.getApplicationSecret());

        this.deviceInfo = new DeviceInfo(application.getApplicationContext());

        this.completionHandler = new CoreCompletionHandler() {

            @Override
            public void onSuccess(final String id, final ResponseModel responseModel) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        statusListener.onStatusLog(id, responseModel.getMessage());
                    }
                });
            }

            @Override
            public void onError(final String id, final Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        statusListener.onError(id, e);
                    }
                });
            }

            @Override
            public void onError(final String id, final ResponseModel responseModel) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Exception exception = new MobileEngageException(
                                responseModel.getStatusCode(),
                                responseModel.getMessage(),
                                responseModel.getBody());
                        statusListener.onError(id, exception);
                    }
                });
            }

        };
    }

    private void initializeRequestManager(String id, String secret) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", HeaderUtils.createBasicAuth(id, secret));
        headers.put("Content-Type", "application/json");
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

    String appLogin() {
        Map<String, Object> payload = injectLoginPayload(createBasePayload());
        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .build();

        manager.submit(model, completionHandler);
        return model.getId();
    }

    String appLogin(int contactField,
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
        return model.getId();
    }

    String appLogout() {
        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_LOGOUT)
                .payload(createBasePayload())
                .build();
        manager.submit(model, completionHandler);
        return model.getId();
    }

    String trackCustomEvent(@NonNull String eventName,
                            @Nullable Map<String, String> eventAttributes) {
        Map<String, Object> payload = createBasePayload();
        payload.put("attributes", eventAttributes);
        RequestModel model = new RequestModel.Builder()
                .url(getEventUrl(eventName))
                .payload(payload)
                .build();
        manager.submit(model, completionHandler);
        return model.getId();
    }

    String trackMessageOpen(Intent intent) {
        String messageId = getMessageId(intent);

        if (messageId != null) {
            Map<String, Object> payload = createBasePayload();
            payload.put("sid", messageId);
            RequestModel model = new RequestModel.Builder()
                    .url(getEventUrl("message_open"))
                    .payload(payload)
                    .build();
            manager.submit(model, completionHandler);
            return model.getId();
        } else {
            final String uuid = RequestModel.nextId();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    statusListener.onError(uuid, new IllegalArgumentException("No messageId found!"));
                }
            });
            return uuid;
        }
    }

    String getMessageId(Intent intent) {
        try {
            if (intent != null) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String customData = bundle.getString("pw_data_json_string");
                    if (customData != null) {
                        String content = new JSONObject(customData).getString("u");
                        return new JSONObject(content).getString("sid");
                    }
                }
            }
        } catch (JSONException je) {
        }
        return null;
    }

    private String getEventUrl(String eventName) {
        return ENDPOINT_BASE + "events/" + eventName;
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
