package com.simulator.physics;

import com.simulator.model.Satellite;

/**
 * Semi-implicit Euler integrator for orbital motion.
 */
public class OrbitIntegrator {

    private final PhysicsEngine physicsEngine;

    public OrbitIntegrator(PhysicsEngine physicsEngine) {
        this.physicsEngine = physicsEngine;
    }

    /**
     * Advances the satellite state by dt seconds.
     */
    public void step(Satellite satellite, double dtSeconds) {
        Vector2D acceleration = physicsEngine.calculateAcceleration(
                satellite.getPositionM(),
                satellite.getMassKg()
        );
        Vector2D nextVelocity = satellite.getVelocityVectorMs().add(acceleration.multiply(dtSeconds));
        Vector2D nextPosition = satellite.getPositionM().add(nextVelocity.multiply(dtSeconds));

        satellite.setAccelerationVectorMs2(acceleration);
        satellite.setVelocityVectorMs(nextVelocity);
        satellite.setPositionM(nextPosition);
    }
}


