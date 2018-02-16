package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;
import com.google.firebase.iid.FirebaseInstanceId;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.endpoint.Endpoint.*;

public class MobileEngageInternal {
    public static final String MOBILEENGAGE_SDK_VERSION = BuildConfig.VERSION_NAME;

    String pushToken;
    AppLoginParameters appLoginParameters;

    MobileEngageConfig config;
    DeviceInfo deviceInfo;
    Application application;
    RequestManager manager;
    AppLoginStorage appLoginStorage;
    Handler handler;
    MobileEngageCoreCompletionHandler coreCompletionHandler;
    MeIdStorage meIdStorage;
    MeIdSignatureStorage meIdSignatureStorage;
    TimestampProvider timestampProvider;

    MobileEngageInternal(MobileEngageConfig config, RequestManager manager, AppLoginStorage appLoginStorage, MobileEngageCoreCompletionHandler coreCompletionHandler) {
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(manager, "Manager must not be null!");
        Assert.notNull(coreCompletionHandler, "CoreCompletionHandler must not be null!");
        Assert.notNull(appLoginStorage, "AppLoginStorage must not be null!");
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: config %s, manager %s, coreCompletionHandler %s", config, manager, coreCompletionHandler);

        this.config = config;
        this.application = config.getApplication();
        this.appLoginStorage = appLoginStorage;
        this.coreCompletionHandler = coreCompletionHandler;

        this.manager = manager;
        manager.setDefaultHeaders(RequestUtils.createDefaultHeaders(config));

        this.deviceInfo = new DeviceInfo(application.getApplicationContext());

        try {
            this.pushToken = FirebaseInstanceId.getInstance().getToken();
        } catch (Exception e) {
            //no token for you
        }

        this.handler = new Handler(Looper.getMainLooper());
        this.meIdStorage = new MeIdStorage(application);
        this.meIdSignatureStorage = new MeIdSignatureStorage(application);
        this.timestampProvider = new TimestampProvider();
    }

    RequestManager getManager() {
        return manager;
    }

    String getPushToken() {
        return pushToken;
    }

    void setPushToken(String pushToken) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", pushToken);
        this.pushToken = pushToken;
        if (appLoginParameters != null) {
            appLogin();
        }
    }

    void setAppLoginParameters(AppLoginParameters parameters) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", parameters);

        this.appLoginParameters = parameters;
    }

    String appLogin() {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Called");

        RequestModel model;
        Map<String, Object> payload = injectLoginPayload(RequestUtils.createBasePayload(config, appLoginParameters));

        Integer storedHashCode = appLoginStorage.get();
        int currentHashCode = payload.hashCode();

        Map<String, String> headers = RequestUtils.createBaseHeaders_V2(config);

        if (storedHashCode == null || currentHashCode != storedHashCode) {
            model = new RequestModel.Builder()
                    .url(ME_LOGIN_V2)
                    .payload(payload)
                    .headers(headers)
                    .build();
            appLoginStorage.set(currentHashCode);
        } else {
            model = new RequestModel.Builder()
                    .url(ME_LAST_MOBILE_ACTIVITY_V2)
                    .payload(RequestUtils.createBasePayload(config, appLoginParameters))
                    .headers(headers)
                    .build();
        }

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    String appLogout() {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Called");

        RequestModel model = new RequestModel.Builder()
                .url(ME_LOGOUT_V2)
                .payload(RequestUtils.createBasePayload(config, appLoginParameters))
                .headers(RequestUtils.createBaseHeaders_V2(config))
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        meIdStorage.remove();
        appLoginStorage.remove();
        return model.getId();
    }

    String trackCustomEvent(@NonNull String eventName,
                            @Nullable Map<String, String> eventAttributes) {
        if (MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            return trackCustomEvent_V3(eventName, eventAttributes);
        } else {
            return trackCustomEvent_V2(eventName, eventAttributes);
        }
    }

    String trackCustomEvent_V2(@NonNull String eventName,
                               @Nullable Map<String, String> eventAttributes) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: eventName %s, eventAttributes %s", eventName, eventAttributes);

        Map<String, Object> payload = RequestUtils.createBasePayload(config, appLoginParameters);
        if (eventAttributes != null && !eventAttributes.isEmpty()) {
            payload.put("attributes", eventAttributes);
        }
        RequestModel model = new RequestModel.Builder()
                .url(RequestUtils.createEventUrl_V2(eventName))
                .payload(payload)
                .headers(RequestUtils.createBaseHeaders_V2(config))
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    String trackCustomEvent_V3(@NonNull String eventName,
                               @Nullable Map<String, String> eventAttributes) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: eventName %s, eventAttributes %s", eventName, eventAttributes);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "custom");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(timestampProvider.provideTimestamp()));
        if (eventAttributes != null && !eventAttributes.isEmpty()) {
            event.put("attributes", eventAttributes);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));
        payload.put("hardware_id", deviceInfo.getHwid());

        RequestModel model = new RequestModel.Builder()
                .url(RequestUtils.createEventUrl_V3(meIdStorage.get()))
                .payload(payload)
                .headers(RequestUtils.createBaseHeaders_V3(
                        config.getApplicationCode(),
                        meIdStorage,
                        meIdSignatureStorage))
                .build();

        MobileEngageUtils.incrementIdlingResource();
        manager.submit(model);
        return model.getId();
    }

    String trackMessageOpen(Intent intent) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", intent);

        String messageId = getMessageId(intent);
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "MessageId %s", messageId);

        return handleMessageOpen(messageId);
    }

    void trackDeepLinkOpen(Intent intent) {
        Uri uri = intent.getData();

        if (uri != null) {
            String ems_dl = "ems_dl";
            String deepLinkQueryParam = uri.getQueryParameter(ems_dl);

            if (deepLinkQueryParam != null) {
                HashMap<String, Object> payload = new HashMap<>();
                payload.put(ems_dl, deepLinkQueryParam);

                RequestModel model = new RequestModel.Builder()
                        .url(DEEP_LINK_CLICK)
                        .payload(payload)
                        .build();

                MobileEngageUtils.incrementIdlingResource();
                manager.submit(model);
            }
        }
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
            Map<String, Object> payload = RequestUtils.createBasePayload(config, appLoginParameters);
            payload.put("sid", messageId);
            RequestModel model = new RequestModel.Builder()
                    .url(RequestUtils.createEventUrl_V2("message_open"))
                    .payload(payload)
                    .headers(RequestUtils.createBaseHeaders_V2(config))
                    .build();

            MobileEngageUtils.incrementIdlingResource();
            manager.submit(model);
            return model.getId();
        } else {
            final String uuid = RequestModel.nextId();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    coreCompletionHandler.onError(uuid, new IllegalArgumentException("No messageId found!"));
                }
            });
            return uuid;
        }
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

}
