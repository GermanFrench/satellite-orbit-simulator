package com.simulator.model;

/**
 * Orbital configuration used when launching or resetting a satellite.
 */
public class Orbit {

    public enum OrbitType {
        LEO(400.0, 7.80, 90.0 * 60.0),
        MEO(20200.0, 3.87, 11.97 * 3600.0),
        GEO(35786.0, 3.07, 24.0 * 3600.0),
        CUSTOM(Double.NaN, Double.NaN, Double.NaN);

        private final double defaultAltitudeKm;
        private final double typicalVelocityKmS;
        private final double typicalPeriodSeconds;

        OrbitType(double defaultAltitudeKm, double typicalVelocityKmS, double typicalPeriodSeconds) {
            this.defaultAltitudeKm = defaultAltitudeKm;
            this.typicalVelocityKmS = typicalVelocityKmS;
            this.typicalPeriodSeconds = typicalPeriodSeconds;
        }

        public double getDefaultAltitudeKm() {
            return defaultAltitudeKm;
        }

        public double getTypicalVelocityKmS() {
            return typicalVelocityKmS;
        }

        public double getTypicalPeriodSeconds() {
            return typicalPeriodSeconds;
        }
    }

    private OrbitType type;
    private double altitudeKm;
    private double eccentricity;
    private double inclinationDeg;

    public Orbit(OrbitType type, double altitudeKm, double eccentricity, double inclinationDeg) {
        this.type = type;
        this.altitudeKm = altitudeKm;
        this.eccentricity = clamp(eccentricity, 0.0, 0.85);
        this.inclinationDeg = clamp(inclinationDeg, 0.0, 180.0);
    }

    public OrbitType getType() {
        return type;
    }

    public void setType(OrbitType type) {
        this.type = type;
    }

    public double getAltitudeKm() {
        return altitudeKm;
    }

    public void setAltitudeKm(double altitudeKm) {
        this.altitudeKm = Math.max(120.0, altitudeKm);
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = clamp(eccentricity, 0.0, 0.85);
    }

    public double getInclinationDeg() {
        return inclinationDeg;
    }

    public void setInclinationDeg(double inclinationDeg) {
        this.inclinationDeg = clamp(inclinationDeg, 0.0, 180.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}


