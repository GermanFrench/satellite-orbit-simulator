package com.simulator.physics;

import com.simulator.model.Earth;
import com.simulator.model.Orbit;

/**
 * Utility class with reusable orbital formulas.
 */
public class OrbitalMechanics {

    public double getPresetAltitudeKm(Orbit.OrbitType type) {
        return type.getDefaultAltitudeKm();
    }

    public double getTypicalVelocityKmS(Orbit.OrbitType type) {
        return type.getTypicalVelocityKmS();
    }

    public double getTypicalPeriodSeconds(Orbit.OrbitType type) {
        return type.getTypicalPeriodSeconds();
    }

    public double circularVelocity(Earth earth, double radiusM) {
        return Math.sqrt(Earth.G * earth.getMass() / radiusM);
    }

    public double semiMajorAxisFromPeriapsis(double periapsisRadiusM, double eccentricity) {
        return periapsisRadiusM / Math.max(1.0 - eccentricity, 1.0e-6);
    }

    public double speedFromVisViva(Earth earth, double radiusM, double semiMajorAxisM) {
        double mu = Earth.G * earth.getMass();
        return Math.sqrt(mu * (2.0 / radiusM - 1.0 / semiMajorAxisM));
    }

    public double estimatePeriodFromEnergy(Earth earth, double specificEnergy) {
        if (specificEnergy >= 0.0) {
            return Double.NaN;
        }
        double mu = Earth.G * earth.getMass();
        double semiMajorAxis = -mu / (2.0 * specificEnergy);
        return 2.0 * Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3.0) / mu);
    }
}


