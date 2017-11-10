package com.emarsys.mobileengage.iam.specification;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.database.QueryAll;
import com.emarsys.mobileengage.iam.DisplayedIam;
import com.emarsys.mobileengage.iam.DisplayedIamRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RemoveDisplayedIamByCampaignIdTest {

    String campaignId;
    RemoveDisplayedIamByCampaignId specification;
    private Context context;

    @Before
    public void init() {
        campaignId = "campaign2";
        specification = new RemoveDisplayedIamByCampaignId(campaignId);
        context = InstrumentationRegistry.getContext();
        context.deleteDatabase("EmarsysMobileEngage.db");
    }

    @Test
    public void getSql() throws Exception {
        String expected = "campaign_id=?";
        String result = specification.getSql();
        assertEquals(expected, result);
    }

    @Test
    public void getArgs() throws Exception {
        String[] expected = new String[]{campaignId};
        String[] result = specification.getArgs();
        assertArrayEquals(expected, result);
    }

    @Test
    public void testSpecification_shouldWorkAsIntended() {
        DisplayedIamRepository repository = new DisplayedIamRepository(context);

        DisplayedIam iam1 = new DisplayedIam("campaign1", 10L, "event1");
        DisplayedIam iam2 = new DisplayedIam("campaign2", 20L, "event2");
        DisplayedIam iam3 = new DisplayedIam("campaign3", 30L, "event3");
        DisplayedIam iam4 = new DisplayedIam("campaign2", 40L, "event2");

        repository.add(iam1);
        repository.add(iam2);
        repository.add(iam3);
        repository.add(iam4);

        repository.remove(specification);

        List<DisplayedIam> result = repository.query(new QueryAll("displayed_iam"));
        List<DisplayedIam> expected = Arrays.asList(iam1, iam3);

        assertEquals(expected, result);
    }

}