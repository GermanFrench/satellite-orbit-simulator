package com.simulator.physics;

import com.simulator.model.Earth;
import com.simulator.model.Orbit;

/**
 * Computes ideal impulsive Hohmann transfer parameters between two circular orbits.
 */
public class HohmannTransferCalculator {

    private final Earth earth;

    public HohmannTransferCalculator(Earth earth) {
        this.earth = earth;
    }

    public OrbitalTransfer calculate(Orbit.OrbitType initial,
                                     Orbit.OrbitType target,
                                     Vector2D periapsisDirection) {
        double r1 = (earth.getRadius() + initial.getDefaultAltitudeKm()) * 1_000.0;
        double r2 = (earth.getRadius() + target.getDefaultAltitudeKm()) * 1_000.0;

        double mu = Earth.G * earth.getMass();
        double aT = 0.5 * (r1 + r2);
        double e = Math.abs(r2 - r1) / (r1 + r2);

        double v1 = Math.sqrt(mu / r1);
        double v2 = Math.sqrt(mu / r2);
        double vPeriTransfer = Math.sqrt(mu * (2.0 / r1 - 1.0 / aT));
        double vApoTransfer = Math.sqrt(mu * (2.0 / r2 - 1.0 / aT));

        double dv1 = vPeriTransfer - v1;
        double dv2 = v2 - vApoTransfer;
        double tTransfer = Math.PI * Math.sqrt(Math.pow(aT, 3.0) / mu);

        return new OrbitalTransfer(
                initial,
                target,
                r1,
                r2,
                aT,
                e,
                dv1,
                dv2,
                tTransfer,
                periapsisDirection
        );
    }
}

