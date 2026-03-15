package com.simulator.physics;

/**
 * Immutable 2D vector utility used by the orbital physics simulation.
 */
public class Vector2D {

    public static final Vector2D ZERO = new Vector2D(0.0, 0.0);

    private final double x;
    private final double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public Vector2D divide(double scalar) {
        return new Vector2D(x / scalar, y / scalar);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double magnitudeSquared() {
        return x * x + y * y;
    }

    public Vector2D normalized() {
        double mag = magnitude();
        if (mag == 0.0) {
            return ZERO;
        }
        return divide(mag);
    }
}

