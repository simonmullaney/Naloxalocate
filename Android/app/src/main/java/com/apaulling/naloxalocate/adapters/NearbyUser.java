package com.apaulling.naloxalocate.adapters;

/**
 * Created by psdco on 12/12/2016.
 */

public class NearbyUser {
    private int id;
    private double distance;

    public NearbyUser(int id, double distance) {
        this.id = id;
        this.distance = distance;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
