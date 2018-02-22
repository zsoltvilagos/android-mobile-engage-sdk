package com.emarsys.mobileengage;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import static com.emarsys.mobileengage.endpoint.Endpoint.DEEP_LINK_CLICK;
import static com.emarsys.mobileengage.endpoint.Endpoint.ME_LAST_MOBILE_ACTIVITY_V2;
import static com.emarsys.mobileengage.endpoint.Endpoint.ME_LOGIN_V2;
import static com.emarsys.mobileengage.endpoint.Endpoint.ME_LOGOUT_V2;

public class MobileEngageInternal {
    public static final String MOBILEENGAGE_SDK_VERSION = BuildConfig.VERSION_NAME;
    private static final String EMS_DEEP_LINK_TRACKED_KEY = "ems_deep_link_tracked";

    String pushToken;
    AppLoginParameters appLoginParameters;

    MobileEngageConfig config;
    DeviceInfo deviceInfo;
    Application application;
    RequestManager manager;
    AppLoginStorage appLoginStorage;
    Handler uiHandler;
    MobileEngageCoreCompletionHandler coreCompletionHandler;
    MeIdStorage meIdStorage;
    MeIdSignatureStorage meIdSignatureStorage;
    TimestampProvider timestampProvider;

    public MobileEngageInternal(
            MobileEngageConfig config,
            RequestManager manager,
            AppLoginStorage appLoginStorage,
            MobileEngageCoreCompletionHandler coreCompletionHandler,
            DeviceInfo deviceInfo,
            Handler uiHandler,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider) {
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

        this.deviceInfo = deviceInfo;
        this.uiHandler = uiHandler;
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
        this.timestampProvider = timestampProvider;

        try {
            this.pushToken = FirebaseInstanceId.getInstance().getToken();
        } catch (Exception ignore) {
        }
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

    public String appLogin() {
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

    public String appLogout() {
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

    public String trackCustomEvent(@NonNull String eventName,
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

    public String trackInternalCustomEvent(@NonNull String eventName,
                                           @Nullable Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Arguments: eventName %s, eventAttributes %s", eventName, eventAttributes);

        if (meIdStorage.get() != null && meIdSignatureStorage.get() != null) {
            RequestModel model = RequestUtils.createInternalCustomEvent(
                    eventName,
                    eventAttributes,
                    config.getApplicationCode(),
                    meIdStorage,
                    meIdSignatureStorage,
                    timestampProvider);

            MobileEngageUtils.incrementIdlingResource();
            manager.submit(model);
            return model.getId();
        } else {
            return RequestModel.nextId();
        }
    }

    public String trackMessageOpen(Intent intent) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", intent);

        String messageId = getMessageId(intent);
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "MessageId %s", messageId);

        return handleMessageOpen(messageId);
    }

    public void trackDeepLinkOpen(Activity activity, Intent intent) {
        Uri uri = intent.getData();
        Intent intentFromActivity = activity.getIntent();
        boolean isLinkTracked = intentFromActivity.getBooleanExtra(EMS_DEEP_LINK_TRACKED_KEY, false);

        if (!isLinkTracked && uri != null) {
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
                intentFromActivity.putExtra(EMS_DEEP_LINK_TRACKED_KEY, true);
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
            uiHandler.post(new Runnable() {
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
