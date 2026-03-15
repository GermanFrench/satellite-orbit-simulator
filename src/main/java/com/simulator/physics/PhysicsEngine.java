package com.simulator.physics;

import com.simulator.model.Earth;

/**
 * Computes gravitational quantities for a satellite around Earth.
 */
public class PhysicsEngine {

    private final Earth earth;

    public PhysicsEngine(Earth earth) {
        this.earth = earth;
    }

    /**
     * Gravitational force vector (N) applied to the satellite.
     */
    public Vector2D calculateGravitationalForce(Vector2D satellitePositionM, double satelliteMassKg) {
        Vector2D towardEarth = satellitePositionM.multiply(-1.0);
        double radiusSquared = towardEarth.magnitudeSquared();

        if (radiusSquared < 1.0) {
            return Vector2D.ZERO;
        }

        double forceMagnitude = Earth.G * earth.getMass() * satelliteMassKg / radiusSquared;
        return towardEarth.normalized().multiply(forceMagnitude);
    }

    /**
     * Gravitational acceleration (m/s^2) at the given position.
     */
    public Vector2D calculateAcceleration(Vector2D satellitePositionM, double satelliteMassKg) {
        Vector2D force = calculateGravitationalForce(satellitePositionM, satelliteMassKg);
        return force.divide(satelliteMassKg);
    }

    public double getMu() {
        return Earth.G * earth.getMass();
    }
}


