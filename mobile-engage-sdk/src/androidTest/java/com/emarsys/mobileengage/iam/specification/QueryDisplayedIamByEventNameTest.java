package com.emarsys.mobileengage.iam.specification;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.iam.model.DisplayedIam;
import com.emarsys.mobileengage.iam.model.DisplayedIamRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class QueryDisplayedIamByEventNameTest {

    String eventName;
    QueryDisplayedIamByEventName specification;
    private Context context;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        eventName = "event2";
        specification = new QueryDisplayedIamByEventName(eventName);
        context = InstrumentationRegistry.getContext();
        context.deleteDatabase("EmarsysMobileEngage.db");
    }

    @Test
    public void getSql() throws Exception {
        String expected = String.format("SELECT * FROM displayed_iam WHERE event_name=?;");
        String result = specification.getSql();
        assertEquals(expected, result);
    }

    @Test
    public void getArgs() throws Exception {
        String[] expected = new String[]{eventName};
        String[] result = specification.getArgs();
        assertArrayEquals(expected, result);
    }

    @Test
    public void testSpecification_shouldWorkAsIntended() {
        DisplayedIamRepository repository = new DisplayedIamRepository(context);

        DisplayedIam iam1 = new DisplayedIam("campaign1", 10L, "event1");
        DisplayedIam iam2 = new DisplayedIam("campaign2", 20L, "event2");
        DisplayedIam iam3 = new DisplayedIam("campaign3", 30L, "event3");
        DisplayedIam iam4 = new DisplayedIam("campaign4", 40L, "event2");

        repository.add(iam1);
        repository.add(iam2);
        repository.add(iam3);
        repository.add(iam4);

        List<DisplayedIam> result = repository.query(specification);
        List<DisplayedIam> expected = Arrays.asList(iam2, iam4);

        assertEquals(expected, result);
    }

}