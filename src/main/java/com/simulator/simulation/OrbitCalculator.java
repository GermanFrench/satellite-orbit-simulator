package com.simulator.simulation;

import com.simulator.model.Earth;

/**
 * Performs orbital mechanics calculations using standard Newtonian gravity.
 *
 * <p>All inputs and outputs are in SI units (metres, seconds) unless explicitly
 * noted otherwise.</p>
 */
public class OrbitCalculator {

    private final Earth earth;

    /**
     * @param earth the Earth model that provides gravitational parameters
     */
    public OrbitCalculator(Earth earth) {
        this.earth = earth;
    }

    /**
     * Calculates the circular orbital velocity using:
     * <pre>v = sqrt(GM / r)</pre>
     *
     * @param radius orbital radius from Earth's centre in metres
     * @return orbital velocity in m/s
     */
    public double calculateOrbitalVelocity(double radius) {
        return Math.sqrt(Earth.G * earth.getMass() / radius);
    }

    /**
     * Calculates the orbital period using:
     * <pre>T = 2π × sqrt(r³ / GM)</pre>
     *
     * @param radius orbital radius from Earth's centre in metres
     * @return orbital period in seconds
     */
    public double calculateOrbitalPeriod(double radius) {
        return 2.0 * Math.PI * Math.sqrt(Math.pow(radius, 3.0) / (Earth.G * earth.getMass()));
    }
}
