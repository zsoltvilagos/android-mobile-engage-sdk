package com.emarsys.mobileengage.service;

import android.content.Intent;

import com.emarsys.mobileengage.notification.command.NotificationCommandFactory;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrackMessageOpenServiceImplementationTest {

    @Test
    public void testStartCommand_runNotificationCommand() {
        NotificationCommandFactory factory = mock(NotificationCommandFactory.class);
        Intent intent = mock(Intent.class);
        Runnable command = mock(Runnable.class);

        when(factory.createNotificationCommand(intent)).thenReturn(command);

        NotificationActionHandler.handleAction(intent, factory);
        verify(command).run();
    }
}