package com.apaulling.naloxalocate.adapters;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by psdco on 22/12/2016.
 * Tests the constructor, getters, and setters of the NearbyUser class
 */
public class NearbyUserTest {

    private static final double tolerance = 0.01;

    NearbyUser userToTest;
    private int initialId;
    private double initialDistance;

    @Before
    public void setUp() throws Exception {
        initialId = 10;
        initialDistance = 155.22;
        userToTest = new NearbyUser(initialId, initialDistance);
    }

    @Test
    public void getId() throws Exception {
        int id = userToTest.getId();
        assertEquals(initialId, id);
    }

    @Test
    public void setId() throws Exception {
        int expectedId = 1;
        userToTest.setId(expectedId);
        assertEquals(expectedId, userToTest.getId());
    }

    @Test
    public void getDistance() throws Exception {
        double expectedDist = userToTest.getDistance();
        assertEquals(initialDistance, expectedDist, tolerance);
    }

    @Test
    public void setDistance() throws Exception {
        double expected = 1.01;
        userToTest.setDistance(expected);
        assertEquals(expected, userToTest.getDistance(), tolerance);
    }

}