package com.apaulling.naloxalocate.adapters;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by psdco on 22/12/2016.
 */
public class NearbyUserTest {

    NearbyUser userToTest;

    @Before
    public void setUp() throws Exception {
        userToTest = new NearbyUser(0, 0);
    }

    @Test
    public void getId() throws Exception {
        int id = userToTest.getId();
        assertEquals(0, id);
    }

    @Test
    public void setId() throws Exception {
        int expected = 1;
        userToTest.setId(expected);
        assertEquals(expected, userToTest.getId());
    }

    @Test
    public void getDistance() throws Exception {
        double dist = userToTest.getDistance();
        assertEquals(0, dist, 0.1);
    }

    @Test
    public void setDistance() throws Exception {
        double expected = 1.01;
        userToTest.setDistance(expected);
        assertEquals(expected, userToTest.getId(), 0.1);
    }

}