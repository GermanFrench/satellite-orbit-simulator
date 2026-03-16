package com.simulator.simulation;

import com.simulator.model.Earth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OrbitCalculator} orbital mechanics calculations.
 */
class OrbitCalculatorTest {

    private static final double TOLERANCE_RELATIVE = 0.01; // 1 %
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    private OrbitCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new OrbitCalculator(new Earth());
    }

    // ── Circular orbital velocity ──────────────────────────────────────────

    @Test
    void circularVelocityAtLeo_isApproximately7800mps() {
        double r = EARTH_RADIUS_M + 400_000.0; // 400 km LEO
        double v = calculator.calculateOrbitalVelocity(r);
        assertEquals(7_672.0, v, 7_672.0 * TOLERANCE_RELATIVE,
                "LEO circular velocity should be ~7.67 km/s");
    }

    @Test
    void circularVelocityAtGeo_isApproximately3070mps() {
        double r = EARTH_RADIUS_M + 35_786_000.0; // GEO
        double v = calculator.calculateOrbitalVelocity(r);
        assertEquals(3_075.0, v, 3_075.0 * TOLERANCE_RELATIVE,
                "GEO circular velocity should be ~3.07 km/s");
    }

    @Test
    void circularVelocityDecreases_withIncreasingAltitude() {
        double vLow = calculator.calculateOrbitalVelocity(EARTH_RADIUS_M + 200_000.0);
        double vHigh = calculator.calculateOrbitalVelocity(EARTH_RADIUS_M + 2_000_000.0);
        assertTrue(vLow > vHigh, "Higher orbit should have lower velocity");
    }

    // ── Orbital period ─────────────────────────────────────────────────────

    @Test
    void orbitalPeriodAtLeo_isApproximately92min() {
        double r = EARTH_RADIUS_M + 400_000.0;
        double t = calculator.calculateOrbitalPeriod(r);
        // Physics gives ~92.4 min; the "90 min" often quoted is an approximation
        assertEquals(92.4 * 60.0, t, 92.4 * 60.0 * TOLERANCE_RELATIVE,
                "LEO period should be ~92 minutes");
    }

    @Test
    void orbitalPeriodAtGeo_isApproximately24h() {
        double r = EARTH_RADIUS_M + 35_786_000.0;
        double t = calculator.calculateOrbitalPeriod(r);
        assertEquals(24.0 * 3600.0, t, 24.0 * 3600.0 * TOLERANCE_RELATIVE,
                "GEO period should be ~24 hours");
    }

    @Test
    void orbitalPeriodIncreases_withAltitude() {
        double tLow = calculator.calculateOrbitalPeriod(EARTH_RADIUS_M + 200_000.0);
        double tHigh = calculator.calculateOrbitalPeriod(EARTH_RADIUS_M + 20_000_000.0);
        assertTrue(tLow < tHigh, "Higher orbit should have longer period");
    }

    // ── Vis-viva equation ──────────────────────────────────────────────────

    @Test
    void visVivaAtPericenter_matchesCircularForCircularOrbit() {
        double r = EARTH_RADIUS_M + 400_000.0;
        double vCircular = calculator.calculateOrbitalVelocity(r);
        double vVisViva = calculator.calculateVelocityVisViva(r, r); // a = r for circular
        assertEquals(vCircular, vVisViva, vCircular * 1.0e-6,
                "Vis-viva should equal circular velocity for a circular orbit");
    }

    // ── Eccentric anomaly (Kepler's equation) ─────────────────────────────

    @Test
    void eccentricAnomaly_forCircularOrbit_equalsMeanAnomaly() {
        double m = Math.PI / 3.0;
        double e = calculator.solveEccentricAnomaly(m, 0.0);
        assertEquals(m, e, 1.0e-9, "e=0 → E should equal M");
    }

    @Test
    void eccentricAnomaly_forModerateEccentricity_satisfiesKeplersEquation() {
        double m = 1.0;
        double ecc = 0.3;
        double bigE = calculator.solveEccentricAnomaly(m, ecc);
        double residual = bigE - ecc * Math.sin(bigE) - m;
        assertEquals(0.0, residual, 1.0e-7, "Kepler residual should be near zero");
    }

    // ── Mean motion ────────────────────────────────────────────────────────

    @Test
    void meanMotionAndPeriod_areConsistent() {
        double a = EARTH_RADIUS_M + 400_000.0;
        double n = calculator.calculateMeanMotion(a);
        double t = calculator.calculateOrbitalPeriod(a);
        // n = 2π / T
        assertEquals(2.0 * Math.PI / t, n, n * 1.0e-9,
                "Mean motion should equal 2π/T");
    }
}
