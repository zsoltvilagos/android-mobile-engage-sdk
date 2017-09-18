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
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.queue.sqlite.SqliteQueue;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.util.DefaultHeaderUtils;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MobileEngageInternal {
    public static final String MOBILEENGAGE_SDK_VERSION = BuildConfig.VERSION_NAME;
    private static String ENDPOINT_BASE = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    private static String ENDPOINT_LOGIN = ENDPOINT_BASE + "users/login";
    private static String ENDPOINT_LOGOUT = ENDPOINT_BASE + "users/logout";

    String pushToken;
    AppLoginParameters appLoginParameters;

    WeakReference<MobileEngageStatusListener> weakStatusListener;

    MobileEngageConfig config;
    DeviceInfo deviceInfo;
    Application application;
    RequestManager manager;
    Handler handler;
    CoreCompletionHandler coreCompletionHandler;

    MobileEngageInternal(MobileEngageConfig config) {
        coreCompletionHandler = new CoreCompletionHandler() {

            @Override
            public void onSuccess(final String id, final ResponseModel responseModel) {
                MobileEngageUtils.decrementIdlingResource();
                if (weakStatusListener.get() != null) {
                    weakStatusListener.get().onStatusLog(id, responseModel.getMessage());
                }
            }

            @Override
            public void onError(final String id, final Exception cause) {
                MobileEngageUtils.decrementIdlingResource();
                handleOnError(id, cause);
            }

            @Override
            public void onError(final String id, final ResponseModel responseModel) {
                MobileEngageUtils.decrementIdlingResource();
                Exception exception = new MobileEngageException(
                        responseModel.getStatusCode(),
                        responseModel.getMessage(),
                        responseModel.getBody());
                handleOnError(id, exception);
            }

        };

        init(config,
                new RequestManager(
                        new ConnectionWatchDog(config.getApplication()),
                        new SqliteQueue(config.getApplication()),
                        coreCompletionHandler));
    }

    MobileEngageInternal(MobileEngageConfig config, RequestManager manager) {
        init(config, manager);
    }

    private void init(MobileEngageConfig config, RequestManager manager) {
        this.application = config.getApplication();
        this.config = config;
        this.weakStatusListener = new WeakReference<>(config.getStatusListener());

        this.manager = manager;
        manager.setDefaultHeaders(DefaultHeaderUtils.createDefaultHeaders(config));

        this.deviceInfo = new DeviceInfo(application.getApplicationContext());

        try {
            this.pushToken = FirebaseInstanceId.getInstance().getToken();
        } catch (Exception e) {
            //no token for you
        }

        this.handler = new Handler(Looper.getMainLooper());
    }

    MobileEngageStatusListener getStatusListener() {
        return weakStatusListener.get();
    }

    RequestManager getManager() {
        return manager;
    }

    public AppLoginParameters getAppLoginParameters() {
        return appLoginParameters;
    }

    void setPushToken(String pushToken) {
        this.pushToken = pushToken;
        if (appLoginParameters != null) {
            if (appLoginParameters.hasCredentials()) {
                appLogin(appLoginParameters.getContactFieldId(), appLoginParameters.getContactFieldValue());
            } else {
                appLogin();
            }
        }
    }

    void setStatusListener(MobileEngageStatusListener listener) {
        this.weakStatusListener = new WeakReference<>(listener);
    }

    String getPushToken() {
        return pushToken;
    }

    String appLogin() {
        this.appLoginParameters = new AppLoginParameters();

        Map<String, Object> payload = injectLoginPayload(createBasePayload());
        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    String appLogin(int contactFieldId,
                    @NonNull String contactFieldValue) {
        this.appLoginParameters = new AppLoginParameters(contactFieldId, contactFieldValue);

        Map<String, Object> additionalPayload = new HashMap<>();
        additionalPayload.put("contact_field_id", contactFieldId);
        additionalPayload.put("contact_field_value", contactFieldValue);

        Map<String, Object> payload = injectLoginPayload(createBasePayload(additionalPayload));

        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    String appLogout() {
        this.appLoginParameters = null;

        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_LOGOUT)
                .payload(createBasePayload())
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    String trackCustomEvent(@NonNull String eventName,
                            @Nullable Map<String, String> eventAttributes) {
        Map<String, Object> payload = createBasePayload();
        if (eventAttributes != null && !eventAttributes.isEmpty()) {
            payload.put("attributes", eventAttributes);
        }
        RequestModel model = new RequestModel.Builder()
                .url(getEventUrl(eventName))
                .payload(payload)
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    String trackMessageOpen(Intent intent) {
        String messageId = getMessageId(intent);

        return handleMessageOpen(messageId);
    }

    String trackMessageOpen(Notification message) {
        Map<String, Object> payload = createBasePayload();
        payload.put("source", "inbox");
        payload.put("sid", message.getSid());
        RequestModel model = new RequestModel.Builder()
                .url(getEventUrl("message_open"))
                .payload(payload)
                .build();

        manager.submit(model);
        return model.getId();
    }

    String getMessageId(Intent intent) {
        String sid = null;
        Bundle payload = intent.getBundleExtra("payload");
        if (payload != null) {
            String customData = payload.getString("u");
            try {
                sid = new JSONObject(customData).getString("sid");
            } catch (JSONException e) {
            }
        }
        return sid;
    }

    private String handleMessageOpen(String messageId) {
        if (messageId != null) {
            Map<String, Object> payload = createBasePayload();
            payload.put("sid", messageId);
            RequestModel model = new RequestModel.Builder()
                    .url(getEventUrl("message_open"))
                    .payload(payload)
                    .build();

            MobileEngageUtils.incrementIdlingResource();
            manager.submit(model);
            return model.getId();
        } else {
            final String uuid = RequestModel.nextId();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    handleOnError(uuid, new IllegalArgumentException("No messageId found!"));
                }
            });
            return uuid;
        }
    }

    private String getEventUrl(String eventName) {
        return ENDPOINT_BASE + "events/" + eventName;
    }

    private Map<String, Object> createBasePayload() {
        return createBasePayload(Collections.EMPTY_MAP);
    }

    private Map<String, Object> createBasePayload(Map<String, Object> additionalPayload) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", config.getApplicationCode());
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
        payload.put("ems_sdk", MOBILEENGAGE_SDK_VERSION);

        if (pushToken == null) {
            payload.put("push_token", false);
        } else {
            payload.put("push_token", pushToken);
        }

        return payload;
    }

    private void handleOnError(String id, Exception cause) {
        if (weakStatusListener.get() != null) {
            weakStatusListener.get().onError(id, cause);
        }
    }
}
