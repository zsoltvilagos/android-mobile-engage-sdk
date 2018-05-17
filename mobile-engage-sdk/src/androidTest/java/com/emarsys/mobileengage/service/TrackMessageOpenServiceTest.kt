package com.emarsys.mobileengage.service

import android.app.Application
import android.content.Intent
import android.support.test.runner.AndroidJUnit4
import com.emarsys.mobileengage.MobileEngage
import com.emarsys.mobileengage.config.MobileEngageConfig
import com.emarsys.mobileengage.iam.EventHandler
import com.nhaarman.mockito_kotlin.verify
import org.json.JSONObject

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class TrackMessageOpenServiceTest {

    private val notificationHandler = mock(EventHandler::class.java)
    lateinit var config: MobileEngageConfig

    @Before
    fun init() {
        config = MobileEngageConfig.Builder()
                .application(mock(Application::class.java))
                .credentials("EMSEC-B103E", "RM1ZSuX8mgRBhQIgOsf6m8bn/bMQLAIb")
                .setNotificationEventHandler(notificationHandler)
                .build()
    }

    @Test()
    fun testAction_shouldInvokeHandleEventMethod_onMobileEngageNotificationHandler() {
        MobileEngage.setup(config)

        val actionId = "uniqueActionId"
        val name = "nameOfTheEvent"
        val payload = JSONObject()
                .put("payloadKey", "payloadValue")
        val json = JSONObject()
                .put("actions", JSONObject()
                        .put(actionId, JSONObject()
                                .put("name", name)
                                .put("payload", payload)
                                .put("type", "MEAppEvent")))
        val intent = Intent()
        intent.action = actionId
        intent.putExtra("payload", json.toString())

        val service = TrackMessageOpenService()
        service.onStartCommand(intent, 0, 0)

        verify(notificationHandler.handleEvent(name, payload))
    }

}