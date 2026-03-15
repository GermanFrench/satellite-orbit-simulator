package com.simulator.simulation;

import com.simulator.model.Earth;

/**
 * Performs orbital mechanics calculations using standard Newtonian gravity.
 *
 * <p>All inputs and outputs are in SI units (metres, seconds) unless explicitly
 * noted otherwise.</p>
 */
public class OrbitCalculator {

    /** Numerical convergence threshold for solving Kepler's equation. */
    private static final double KEPLER_TOLERANCE = 1.0e-8;

    /** Maximum Newton-Raphson iterations for Kepler equation solving. */
    private static final int KEPLER_MAX_ITERATIONS = 20;

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

    /**
     * Calculates mean motion for an orbit with semi-major axis {@code a}:
     * <pre>n = sqrt(mu / a^3)</pre>
     *
     * @param semiMajorAxisM semi-major axis in metres
     * @return mean motion in rad/s
     */
    public double calculateMeanMotion(double semiMajorAxisM) {
        double mu = Earth.G * earth.getMass();
        return Math.sqrt(mu / Math.pow(semiMajorAxisM, 3.0));
    }

    /**
     * Solves Kepler's equation {@code M = E - e sin(E)} for eccentric anomaly.
     *
     * @param meanAnomaly mean anomaly (rad)
     * @param eccentricity orbital eccentricity [0, 1)
     * @return eccentric anomaly E (rad)
     */
    public double solveEccentricAnomaly(double meanAnomaly, double eccentricity) {
        double e = clampEccentricity(eccentricity);
        double m = normalizeAngle(meanAnomaly);

        // Good initial guess for low and moderate eccentricities.
        double eccentricAnomaly = (e < 0.8) ? m : Math.PI;

        for (int i = 0; i < KEPLER_MAX_ITERATIONS; i++) {
            double f = eccentricAnomaly - e * Math.sin(eccentricAnomaly) - m;
            double fp = 1.0 - e * Math.cos(eccentricAnomaly);
            double delta = f / fp;
            eccentricAnomaly -= delta;
            if (Math.abs(delta) < KEPLER_TOLERANCE) {
                break;
            }
        }
        return eccentricAnomaly;
    }

    /**
     * Converts eccentric anomaly to true anomaly.
     *
     * @param eccentricAnomaly Eccentric anomaly E (rad)
     * @param eccentricity orbital eccentricity [0, 1)
     * @return true anomaly ν (rad)
     */
    public double calculateTrueAnomaly(double eccentricAnomaly, double eccentricity) {
        double e = clampEccentricity(eccentricity);
        double cosE = Math.cos(eccentricAnomaly);
        double sinE = Math.sin(eccentricAnomaly);
        double factor = Math.sqrt(1.0 - e * e);
        return Math.atan2(factor * sinE, cosE - e);
    }

    /**
     * Calculates instantaneous orbital radius from true anomaly:
     * <pre>r = a(1 - e^2) / (1 + e cos(nu))</pre>
     *
     * @param semiMajorAxisM semi-major axis in metres
     * @param eccentricity orbital eccentricity [0, 1)
     * @param trueAnomaly true anomaly ν (rad)
     * @return radius from Earth's centre in metres
     */
    public double calculateRadiusAtTrueAnomaly(double semiMajorAxisM, double eccentricity, double trueAnomaly) {
        double e = clampEccentricity(eccentricity);
        return semiMajorAxisM * (1.0 - e * e) / (1.0 + e * Math.cos(trueAnomaly));
    }

    /**
     * Calculates instantaneous velocity with vis-viva equation:
     * <pre>v = sqrt(mu * (2/r - 1/a))</pre>
     *
     * @param radiusM instantaneous radius in metres
     * @param semiMajorAxisM semi-major axis in metres
     * @return velocity in m/s
     */
    public double calculateVelocityVisViva(double radiusM, double semiMajorAxisM) {
        double mu = Earth.G * earth.getMass();
        return Math.sqrt(mu * (2.0 / radiusM - 1.0 / semiMajorAxisM));
    }

    private double clampEccentricity(double eccentricity) {
        return Math.max(0.0, Math.min(0.95, eccentricity));
    }

    private double normalizeAngle(double angleRad) {
        double twoPi = 2.0 * Math.PI;
        double value = angleRad % twoPi;
        return (value < 0.0) ? value + twoPi : value;
    }
}
