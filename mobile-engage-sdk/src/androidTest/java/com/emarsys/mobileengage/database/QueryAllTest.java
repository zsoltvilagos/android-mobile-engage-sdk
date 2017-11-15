package com.emarsys.mobileengage.database;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.iam.model.DisplayedIam;
import com.emarsys.mobileengage.iam.model.DisplayedIamRepository;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QueryAllTest {

    private QueryAll specification;
    private Context context;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        DatabaseTestUtils.deleteMobileEngageDatabase();

        context = InstrumentationRegistry.getContext();

        specification = new QueryAll("displayed_iam");
    }

    @Test
    public void testGetSql() {
        String expected = "SELECT * FROM displayed_iam;";
        String result = specification.getSql();

        assertEquals(expected, result);
    }

    @Test
    public void testGetArgs() {
        assertNull(specification.getArgs());
    }

    @Test
    public void testSpecification_shouldWorkAsIntended() {
        DisplayedIamRepository repository = new DisplayedIamRepository(context);

        DisplayedIam iam1 = new DisplayedIam("campaign1", 10L, "event1");
        DisplayedIam iam2 = new DisplayedIam("campaign2", 20L, "event2");
        DisplayedIam iam3 = new DisplayedIam("campaign3", 30L, "event3");

        repository.add(iam1);
        repository.add(iam2);
        repository.add(iam3);

        List<DisplayedIam> result = repository.query(specification);
        List<DisplayedIam> expected = Arrays.asList(iam1, iam2, iam3);

        assertEquals(expected, result);
    }
}
