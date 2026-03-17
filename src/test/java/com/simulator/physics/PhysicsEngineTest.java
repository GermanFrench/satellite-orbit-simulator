package com.simulator.physics;

import com.simulator.model.Earth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicsEngineTest {

    @Test
    void accelerationPointsTowardEarthAndHasReasonableMagnitudeAtLeo() {
        Earth earth = new Earth();
        PhysicsEngine physics = new PhysicsEngine(earth);

        double radiusM = (earth.getRadius() + 400.0) * 1_000.0;
        Vector2D position = new Vector2D(radiusM, 0.0);

        Vector2D acceleration = physics.calculateAcceleration(position, 750.0);

        assertTrue(acceleration.getX() < 0.0, "La aceleracion debe apuntar hacia el centro de la Tierra");
        assertEquals(0.0, acceleration.getY(), 1e-9);

        double expected = physics.getMu() / (radiusM * radiusM);
        assertEquals(expected, acceleration.magnitude(), expected * 0.01);
    }

    @Test
    void gravitationalForceScalesWithSatelliteMass() {
        Earth earth = new Earth();
        PhysicsEngine physics = new PhysicsEngine(earth);

        double radiusM = (earth.getRadius() + 800.0) * 1_000.0;
        Vector2D position = new Vector2D(0.0, radiusM);

        Vector2D force500 = physics.calculateGravitationalForce(position, 500.0);
        Vector2D force1000 = physics.calculateGravitationalForce(position, 1_000.0);

        assertEquals(force500.magnitude() * 2.0, force1000.magnitude(), force1000.magnitude() * 1e-9);
        assertTrue(force1000.getY() < 0.0);
    }
}

