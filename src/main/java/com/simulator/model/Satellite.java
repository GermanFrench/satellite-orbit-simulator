package com.simulator.model;

/**
 * Represents a satellite with its orbital attributes and current position.
 *
 * <p>Positions ({@code x}, {@code y}) are in screen-pixel coordinates and are
 * recalculated by {@link #updatePosition(double, double, double)} on every
 * animation frame.</p>
 */
public class Satellite {

    /** Altitude above Earth's surface in kilometres. */
    private double altitude;

    /** Orbital velocity in km/s (computed from orbital mechanics). */
    private double velocity;

    /** Orbital period in seconds (computed from orbital mechanics). */
    private double orbitalPeriod;

    /** Current orbital angle in radians (0 = 3 o'clock, increases counter-clockwise). */
    private double angle;

    /** Screen X coordinate of the satellite (pixels). */
    private double x;

    /** Screen Y coordinate of the satellite (pixels). */
    private double y;

    /**
     * Creates a satellite at the given altitude.
     *
     * @param altitude altitude above Earth's surface in kilometres
     */
    public Satellite(double altitude) {
        this.altitude = altitude;
        this.angle = 0.0;
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
}
