package com.simulator.physics;

import com.simulator.model.Rocket;

/**
 * Provides a thrust profile for the rocket over time.
 */
public class ThrustModel {

    /**
     * Returns thrust in Newton for the current ascent time.
     */
    public double getThrustN(Rocket rocket) {
        double t = rocket.getElapsedTimeS();
        if (rocket.getFuelMassKg() <= 0.0) {
            return 0.0;
        }

        if (t < 30.0) {
            return rocket.getMaxThrustN() * 0.88;
        }
        if (t < 90.0) {
            return rocket.getMaxThrustN();
        }
        return rocket.getMaxThrustN() * 0.72;
    }

    /**
     * Simplified mass flow rate model in kg/s.
     */
    public double getFuelBurnRateKgS(Rocket rocket, double thrustN) {
        double maxRate = 260.0;
        return maxRate * (thrustN / Math.max(rocket.getMaxThrustN(), 1.0));
    }
}

