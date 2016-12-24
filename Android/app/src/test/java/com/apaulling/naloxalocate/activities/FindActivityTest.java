package com.apaulling.naloxalocate.activities;

import com.apaulling.naloxalocate.adapters.NearbyUser;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
        /**
         * Testing this method requires mocking the JSONArray object and all it's methods
         * This was beyond the scope of the testing possible in the alloted time
         */
        // An example of a mocked object
        JSONArray mockArray = Mockito.mock(JSONArray.class);

        /*
        The test as we would have written it if mocking was not required.
        */
        JSONArray arrayToTest = new JSONArray();

        // Create array element to put into arrayToTest
        JSONArray arrayEl = new JSONArray();
        arrayEl.put(0); // index
        arrayEl.put(0); // distance
        // Put new element into arrayToTest
        arrayToTest.put(arrayEl);

        // Create the expected test result
        ArrayList<NearbyUser> expectedList = new ArrayList<NearbyUser>(1);
        expectedList.add(new NearbyUser(0, 0));

        // Compare
        assertEquals(expectedList, fa.JSONToNearbyUsers(arrayToTest));
    }
}