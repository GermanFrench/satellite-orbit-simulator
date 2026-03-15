package com.simulator.model;

/**
 * Snapshot of launch metrics shown in the launch telemetry panel.
 */
public class LaunchTelemetry {

    private final boolean active;
    private final String status;
    private final double altitudeKm;
    private final double velocityKmS;
    private final double thrustKN;

    public LaunchTelemetry(boolean active, String status, double altitudeKm, double velocityKmS, double thrustKN) {
        this.active = active;
        this.status = status;
        this.altitudeKm = altitudeKm;
        this.velocityKmS = velocityKmS;
        this.thrustKN = thrustKN;
    }

    public boolean isActive() {
        return active;
    }

    public String getStatus() {
        return status;
    }

    public double getAltitudeKm() {
        return altitudeKm;
    }

    public double getVelocityKmS() {
        return velocityKmS;
    }

    public double getThrustKN() {
        return thrustKN;
    }

    public static LaunchTelemetry idle() {
        return new LaunchTelemetry(false, "Idle", 0.0, 0.0, 0.0);
    }
}

