package com.emarsys.mobileengage.iam.ui;

import com.emarsys.mobileengage.iam.DialogOwner;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IamJsBridgeTest {
    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldNotAcceptNull() {
        new IamJsBridge(null);
    }

    @Test
    public void testClose_shouldInvokeCloseOnTheDialogOfTheMessageHandler() {
        DialogOwner dialogOwner = mock(DialogOwner.class);
        IamDialog iamDialog = mock(IamDialog.class);
        when(dialogOwner.getIamDialog()).thenReturn(iamDialog);

        IamJsBridge jsBridge = new IamJsBridge(dialogOwner);
        jsBridge.close("");

        verify(iamDialog).dismiss();
    }
}