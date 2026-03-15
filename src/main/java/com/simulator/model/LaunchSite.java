package com.simulator.model;

/**
 * Launch site configuration used as initial condition for rocket ascent.
 */
public class LaunchSite {

    private final String name;
    private final double latitudeDeg;
    private final double launchAzimuthDeg;

    public LaunchSite(String name, double latitudeDeg, double launchAzimuthDeg) {
        this.name = name;
        this.latitudeDeg = latitudeDeg;
        this.launchAzimuthDeg = launchAzimuthDeg;
    }

    public String getName() {
        return name;
    }

    public double getLatitudeDeg() {
        return latitudeDeg;
    }

    public double getLaunchAzimuthDeg() {
        return launchAzimuthDeg;
    }
}

