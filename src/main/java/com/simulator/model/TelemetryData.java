package com.simulator.model;

import java.time.LocalDateTime;

/**
 * A snapshot of the satellite's telemetry at a specific instant.
 *
 * <p>Instances are created every time the UI telemetry panel is refreshed so
 * that historical data could be logged if required.</p>
 */
public class TelemetryData {

    /** Altitude above Earth's surface in kilometres. */
    private final double altitude;

    /** Orbital velocity in km/s. */
    private final double velocity;

    /** Orbital period in seconds. */
    private final double orbitalPeriod;

    /** Wall-clock time when this snapshot was taken. */
    private final LocalDateTime timestamp;

    /**
     * Creates a telemetry snapshot with the current wall-clock timestamp.
     *
     * @param altitude      altitude in km
     * @param velocity      velocity in km/s
     * @param orbitalPeriod orbital period in seconds
     */
    public TelemetryData(double altitude, double velocity, double orbitalPeriod) {
        this.altitude = altitude;
        this.velocity = velocity;
        this.orbitalPeriod = orbitalPeriod;
        this.timestamp = LocalDateTime.now();
    }

    public double getAltitude() {
        return altitude;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getOrbitalPeriod() {
        return orbitalPeriod;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
