package com.emarsys.mobileengage.event.applogin;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AppLoginStorageTest {
    private AppLoginStorage storage;
    private Context context;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext().getApplicationContext();
        storage = new AppLoginStorage(context);
        storage.remove();
    }

    @Test
    public void getLastAppLoginPayloadHashCode_shouldReturnNull_ifTheStorageIsEmpty() throws Exception {
        assertNull(storage.get());
    }

    @Test
    public void set() throws Exception {
        storage.set(42);
        assertEquals((Integer) 42, storage.get());
    }

    @Test
    public void clear_shouldRemoveLastAppLoginPayloadHashCodeValue() {
        storage.set(42);
        storage.remove();

        assertNull(storage.get());
    }

    @Test
    public void setLastAppLoginPayloadHashCode_shouldPreserveValues() throws Exception {
        storage.set(42);
        storage = new AppLoginStorage(context);

        assertEquals((Integer) 42, storage.get());
    }

}