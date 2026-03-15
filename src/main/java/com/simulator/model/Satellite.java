package com.simulator.model;

import com.simulator.physics.Vector2D;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a satellite with its orbital attributes and current position.
 *
 * <p>Positions ({@code x}, {@code y}) are in screen-pixel coordinates and are
 * recalculated by {@link #updatePosition(double, double, double)} on every
 * animation frame.</p>
 */
public class Satellite {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    public static final double DEFAULT_MASS_KG = 750.0;

    private final String satelliteId;
    private String displayName;

    /** Physical mass used by the force calculation (kg). */
    private double massKg;

    /** Orbit configuration used for resets and edited launch settings. */
    private Orbit orbit;

    /** Altitude above Earth's surface in kilometres. */
    private double altitude;

    /** Orbital velocity in km/s (computed from orbital mechanics). */
    private double velocity;

    /** Orbital period in seconds (computed from orbital mechanics). */
    private double orbitalPeriod;

    /** Current orbital angle in radians (0 = 3 o'clock, increases counter-clockwise). */
    private double angle;

    /** Optional initial launch speed controlled by the UI in km/s. */
    private double initialSpeedKmS;

    /** Orbital specific mechanical energy in J/kg. */
    private double specificEnergy;

    /** Screen X coordinate of the satellite (pixels). */
    private double x;

    /** Screen Y coordinate of the satellite (pixels). */
    private double y;

    /** Physical position in metres in an Earth-centred inertial 2D frame. */
    private Vector2D positionM = Vector2D.ZERO;

    /** Physical velocity in m/s in the same inertial frame. */
    private Vector2D velocityVectorMs = Vector2D.ZERO;

    /** Physical acceleration in m/s^2 due to gravity. */
    private Vector2D accelerationVectorMs2 = Vector2D.ZERO;

    /**
     * Creates a satellite at the given altitude.
     *
     * @param altitude altitude above Earth's surface in kilometres
     */
    public Satellite(String displayName, double massKg, Orbit orbit, double initialSpeedKmS) {
        this.satelliteId = "SAT-" + COUNTER.getAndIncrement();
        this.displayName = displayName;
        this.massKg = massKg;
        this.orbit = orbit;
        this.altitude = orbit.getAltitudeKm();
        this.angle = 0.0;
        this.initialSpeedKmS = initialSpeedKmS;
    }

    /**
     * Recalculates the satellite's screen position from its current angle and the
     * supplied orbital screen radius.
     *
     * @param centerX     x-coordinate of the orbit centre on the canvas (pixels)
     * @param centerY     y-coordinate of the orbit centre on the canvas (pixels)
     * @param screenRadius radius of the orbit on the canvas (pixels)
     */
    public void updatePosition(double centerX, double centerY, double screenRadius) {
        x = centerX + screenRadius * Math.cos(angle);
        y = centerY + screenRadius * Math.sin(angle);
    }

    // -------------------------------------------------------------------------
    // Altitude
    // -------------------------------------------------------------------------

    /**
     * Sets the satellite's altitude and resets derived quantities so they are
     * recalculated before the next frame.
     *
     * @param altitude new altitude in kilometres
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public String getSatelliteId() {
        return satelliteId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getMassKg() {
        return massKg;
    }

    public void setMassKg(double massKg) {
        this.massKg = massKg;
    }

    public Orbit getOrbit() {
        return orbit;
    }

    public void setOrbit(Orbit orbit) {
        this.orbit = orbit;
    }

    // -------------------------------------------------------------------------
    // Derived orbital properties
    // -------------------------------------------------------------------------

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getOrbitalPeriod() {
        return orbitalPeriod;
    }

    public void setOrbitalPeriod(double orbitalPeriod) {
        this.orbitalPeriod = orbitalPeriod;
    }

    public double getInitialSpeedKmS() {
        return initialSpeedKmS;
    }

    public void setInitialSpeedKmS(double initialSpeedKmS) {
        this.initialSpeedKmS = initialSpeedKmS;
    }

    public double getSpecificEnergy() {
        return specificEnergy;
    }

    public void setSpecificEnergy(double specificEnergy) {
        this.specificEnergy = specificEnergy;
    }

    // -------------------------------------------------------------------------
    // Position
    // -------------------------------------------------------------------------

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setScreenPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // -------------------------------------------------------------------------
    // Elliptical orbit properties
    // -------------------------------------------------------------------------

    public Vector2D getPositionM() {
        return positionM;
    }

    public void setPositionM(Vector2D positionM) {
        this.positionM = positionM;
    }

    public Vector2D getVelocityVectorMs() {
        return velocityVectorMs;
    }

    public void setVelocityVectorMs(Vector2D velocityVectorMs) {
        this.velocityVectorMs = velocityVectorMs;
    }

    public Vector2D getAccelerationVectorMs2() {
        return accelerationVectorMs2;
    }

    public void setAccelerationVectorMs2(Vector2D accelerationVectorMs2) {
        this.accelerationVectorMs2 = accelerationVectorMs2;
    }

    @Override
    public String toString() {
        return displayName + " [" + orbit.getType().name() + "]";
    }
}
