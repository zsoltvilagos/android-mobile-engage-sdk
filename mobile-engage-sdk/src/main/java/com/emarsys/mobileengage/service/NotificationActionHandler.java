package com.emarsys.mobileengage.service;

import android.content.Intent;

import com.emarsys.mobileengage.notification.command.NotificationCommandFactory;

public class NotificationActionHandler {

    public static void handleAction(Intent intent, NotificationCommandFactory commandFactory) {
        Runnable command = commandFactory.createNotificationCommand(intent);
        command.run();
    }

}
