package com.simulator.physics;

import com.simulator.model.Earth;
import com.simulator.model.Orbit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link HohmannTransferCalculator}.
 */
class HohmannTransferCalculatorTest {

    private static final double TOLERANCE_RELATIVE = 0.01; // 1 %

    private HohmannTransferCalculator calculator;
    private Earth earth;

    @BeforeEach
    void setUp() {
        earth = new Earth();
        calculator = new HohmannTransferCalculator(earth);
    }

    // ── Basic LEO → GEO transfer ───────────────────────────────────────────

    @Test
    void leoToGeo_deltaV1_isPositive() {
        OrbitalTransfer t = calculate(Orbit.OrbitType.LEO, Orbit.OrbitType.GEO);
        assertTrue(t.getDeltaV1Ms() > 0,
                "First burn should accelerate the spacecraft");
    }

    @Test
    void leoToGeo_deltaV2_isPositive() {
        OrbitalTransfer t = calculate(Orbit.OrbitType.LEO, Orbit.OrbitType.GEO);
        assertTrue(t.getDeltaV2Ms() > 0,
                "Second burn should circularise at GEO");
    }

    @Test
    void leoToGeo_totalDeltaV_isApproximately3900mps() {
        OrbitalTransfer t = calculate(Orbit.OrbitType.LEO, Orbit.OrbitType.GEO);
        double totalDv = t.getDeltaV1Ms() + t.getDeltaV2Ms();
        // Standard LEO→GEO Hohmann ΔV is roughly 3.9 – 4.2 km/s
        assertEquals(3_900.0, totalDv, 3_900.0 * 0.08,
                "Total ΔV for LEO→GEO should be ~3.9 km/s");
    }

    @Test
    void leoToGeo_transferTime_isAboutFiveHours() {
        OrbitalTransfer t = calculate(Orbit.OrbitType.LEO, Orbit.OrbitType.GEO);
        double expectedSeconds = 5.3 * 3600.0;
        assertEquals(expectedSeconds, t.getTransferTimeSeconds(),
                expectedSeconds * 0.05,
                "LEO→GEO Hohmann transfer time should be ~5.3 h");
    }

    // ── Downward transfer (GEO → LEO) ─────────────────────────────────────

    @Test
    void geoToLeo_deltaV1_isNegative() {
        OrbitalTransfer t = calculate(Orbit.OrbitType.GEO, Orbit.OrbitType.LEO);
        assertTrue(t.getDeltaV1Ms() < 0,
                "First burn of a descent should decelerate the spacecraft");
    }

    // ── Same-orbit transfer ────────────────────────────────────────────────

    @Test
    void sameOrbit_deltaVs_areZero() {
        OrbitalTransfer t = calculate(Orbit.OrbitType.LEO, Orbit.OrbitType.LEO);
        assertEquals(0.0, t.getDeltaV1Ms(), 1.0,
                "No delta-v needed for same-orbit transfer");
        assertEquals(0.0, t.getDeltaV2Ms(), 1.0,
                "No delta-v needed for same-orbit transfer");
    }

    // ── Transfer orbit geometry ────────────────────────────────────────────

    @Test
    void transferOrbit_semiMajorAxis_isMeanOfBothRadii() {
        OrbitalTransfer t = calculate(Orbit.OrbitType.LEO, Orbit.OrbitType.GEO);
        double r1 = (earth.getRadius() + Orbit.OrbitType.LEO.getDefaultAltitudeKm()) * 1_000.0;
        double r2 = (earth.getRadius() + Orbit.OrbitType.GEO.getDefaultAltitudeKm()) * 1_000.0;
        double expectedA = (r1 + r2) / 2.0;
        assertEquals(expectedA, t.getSemiMajorAxisTransferM(), expectedA * 1.0e-9,
                "Transfer semi-major axis should equal (r1+r2)/2");
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private OrbitalTransfer calculate(Orbit.OrbitType from, Orbit.OrbitType to) {
        Vector2D periDir = new Vector2D(1.0, 0.0);
        return calculator.calculate(from, to, periDir);
    }
}
