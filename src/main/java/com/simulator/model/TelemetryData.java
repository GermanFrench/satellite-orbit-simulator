package com.simulator.model;

import java.time.LocalDateTime;

/**
 * A snapshot of the satellite's telemetry at a specific instant.
 *
 * <p>Instances are created every time the UI telemetry panel is refreshed so
 * that historical data could be logged if required.</p>
 */
public class TelemetryData {

    /** Satellite identifier to support per-satellite telemetry panels. */
    private final String satelliteId;

    /** Human-readable satellite name. */
    private final String satelliteName;

    /** Altitude above Earth's surface in kilometres. */
    private final double altitude;

    /** Orbital velocity in km/s. */
    private final double velocity;

    /** Orbital period in seconds. */
    private final double orbitalPeriod;

    /** Configured launch speed for this satellite (km/s). */
    private final double initialSpeed;

    /** Configured orbital inclination (degrees). */
    private final double inclination;

    /** Orbital specific mechanical energy (J/kg). */
    private final double specificEnergy;

    /** Satellite mass (kg). */
    private final double massKg;

    /** Wall-clock time when this snapshot was taken. */
    private final LocalDateTime timestamp;

    /**
     * Creates a telemetry snapshot with the current wall-clock timestamp.
     *
     * @param altitude      altitude in km
     * @param velocity      velocity in km/s
     * @param orbitalPeriod orbital period in seconds
     */
    public TelemetryData(String satelliteId,
                         String satelliteName,
                         double altitude,
                         double velocity,
                         double orbitalPeriod,
                         double initialSpeed,
                         double inclination,
                         double specificEnergy,
                         double massKg) {
        this.satelliteId = satelliteId;
        this.satelliteName = satelliteName;
        this.altitude = altitude;
        this.velocity = velocity;
        this.orbitalPeriod = orbitalPeriod;
        this.initialSpeed = initialSpeed;
        this.inclination = inclination;
        this.specificEnergy = specificEnergy;
        this.massKg = massKg;
        this.timestamp = LocalDateTime.now();
    }

    public static TelemetryData fromSatellite(Satellite satellite) {
        return new TelemetryData(
                satellite.getSatelliteId(),
                satellite.getDisplayName(),
                satellite.getAltitude(),
                satellite.getVelocity(),
                satellite.getOrbitalPeriod(),
                satellite.getInitialSpeedKmS(),
                satellite.getOrbit().getInclinationDeg(),
                satellite.getSpecificEnergy(),
                satellite.getMassKg()
        );
    }

    public String getSatelliteId() {
        return satelliteId;
    }

    public String getSatelliteName() {
        return satelliteName;
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

    public double getInitialSpeed() {
        return initialSpeed;
    }

    public double getInclination() {
        return inclination;
    }

    public double getSpecificEnergy() {
        return specificEnergy;
    }

    public double getMassKg() {
        return massKg;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
