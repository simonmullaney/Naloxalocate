package com.apaulling.naloxalocate.activities;

import com.apaulling.naloxalocate.adapters.NearbyUser;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by psdco on 22/12/2016.
 */
public class FindActivityTest {
    private FindActivity fa;

    @Before
    public void setUp() throws Exception {
        fa = new FindActivity();
    }

    @Test
    public void JSONToNearbyUsers() throws Exception {
        JSONArray array = new JSONArray();

        // Create array element, also an array
        JSONArray arrayEl = new JSONArray();
        arrayEl.put(0); // index
        arrayEl.put(0); // distance

        array.put(arrayEl);

        ArrayList<NearbyUser> expectedList = new ArrayList<NearbyUser>(1);
        expectedList.add(new NearbyUser(0, 0));

        assertEquals(expectedList, fa.JSONToNearbyUsers(array));
    }
}