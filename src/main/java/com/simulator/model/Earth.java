package com.simulator.model;

/**
 * Represents the Earth with its fundamental physical properties used in
 * orbital mechanics calculations.
 */
public class Earth {

    /** Universal gravitational constant (m³ kg⁻¹ s⁻²). */
    public static final double G = 6.674e-11;

    /** Mass of the Earth in kilograms. */
    private final double mass = 5.972e24;

    /** Mean radius of the Earth in kilometres. */
    private final double radius = 6371.0;

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }
}
