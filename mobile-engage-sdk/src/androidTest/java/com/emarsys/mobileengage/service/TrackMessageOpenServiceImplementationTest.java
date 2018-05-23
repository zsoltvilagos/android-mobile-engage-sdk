package com.emarsys.mobileengage.service;

import android.content.Intent;

import com.emarsys.mobileengage.notification.command.NotificationCommandFactory;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrackMessageOpenServiceImplementationTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

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