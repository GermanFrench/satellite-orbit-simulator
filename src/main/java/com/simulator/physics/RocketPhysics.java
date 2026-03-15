package com.simulator.physics;

import com.simulator.model.Earth;
import com.simulator.model.Rocket;

/**
 * Rocket ascent physics: thrust + gravity + simple drag.
 */
public class RocketPhysics {

    private final Earth earth;

    public RocketPhysics(Earth earth) {
        this.earth = earth;
    }

    public Vector2D calculateGravityAcceleration(Vector2D positionM) {
        double radius = Math.max(positionM.magnitude(), 1.0);
        double mu = Earth.G * earth.getMass();
        return positionM.normalized().multiply(-mu / (radius * radius));
    }

    public Vector2D calculateDragAcceleration(Vector2D velocityMs, double altitudeKm, double massKg) {
        double speed = velocityMs.magnitude();
        if (speed < 1.0) {
            return Vector2D.ZERO;
        }

        // Very simple exponential atmosphere model for educational visuals.
        double rho0 = 1.225;
        double scaleHeightM = 8500.0;
        double altitudeM = Math.max(0.0, altitudeKm * 1_000.0);
        double rho = rho0 * Math.exp(-altitudeM / scaleHeightM);

        double cdA = 8.0;
        double dragMagnitude = 0.5 * rho * speed * speed * cdA;
        return velocityMs.normalized().multiply(-dragMagnitude / Math.max(massKg, 1.0));
    }

    public Vector2D calculateThrustAcceleration(double thrustN, Vector2D thrustDirection, double massKg) {
        return thrustDirection.normalized().multiply(thrustN / Math.max(massKg, 1.0));
    }

    public Vector2D calculateTotalAcceleration(Rocket rocket, double thrustN, Vector2D thrustDirection) {
        Vector2D gravity = calculateGravityAcceleration(rocket.getPositionM());
        double altitudeKm = rocket.getPositionM().magnitude() / 1_000.0 - earth.getRadius();
        Vector2D drag = calculateDragAcceleration(rocket.getVelocityMs(), altitudeKm, rocket.getMassKg());
        Vector2D thrust = calculateThrustAcceleration(thrustN, thrustDirection, rocket.getMassKg());
        return gravity.add(drag).add(thrust);
    }
}

