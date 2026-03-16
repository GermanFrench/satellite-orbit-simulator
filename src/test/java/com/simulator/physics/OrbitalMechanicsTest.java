package com.simulator.physics;

import com.simulator.model.Earth;
import com.simulator.model.Orbit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OrbitalMechanics} utility methods.
 */
class OrbitalMechanicsTest {

    private static final double TOLERANCE_RELATIVE = 0.01; // 1 %

    private OrbitalMechanics mechanics;
    private Earth earth;

    @BeforeEach
    void setUp() {
        earth = new Earth();
        mechanics = new OrbitalMechanics();
    }

    // ── Preset orbit parameters ────────────────────────────────────────────

    @Test
    void leoPresetAltitude_is400km() {
        assertEquals(400.0, mechanics.getPresetAltitudeKm(Orbit.OrbitType.LEO), 0.1);
    }

    @Test
    void geoPresetAltitude_is35786km() {
        assertEquals(35_786.0, mechanics.getPresetAltitudeKm(Orbit.OrbitType.GEO), 1.0);
    }

    @Test
    void meoPresetVelocity_isAbout3870mps() {
        double vKmS = mechanics.getTypicalVelocityKmS(Orbit.OrbitType.MEO);
        assertEquals(3.87, vKmS, 3.87 * TOLERANCE_RELATIVE);
    }

    @Test
    void geoPeriod_isAbout24h() {
        double tSeconds = mechanics.getTypicalPeriodSeconds(Orbit.OrbitType.GEO);
        assertEquals(24.0 * 3600.0, tSeconds, 24.0 * 3600.0 * TOLERANCE_RELATIVE);
    }

    // ── Circular velocity ──────────────────────────────────────────────────

    @Test
    void circularVelocityAtLeo_matchesStandardValue() {
        double r = (earth.getRadius() + Orbit.OrbitType.LEO.getDefaultAltitudeKm()) * 1_000.0;
        double v = mechanics.circularVelocity(earth, r);
        assertEquals(7_672.0, v, 7_672.0 * TOLERANCE_RELATIVE,
                "LEO circular velocity should be ~7.67 km/s");
    }

    // ── Vis-viva speed ─────────────────────────────────────────────────────

    @Test
    void speedFromVisViva_circularCase_matchesCircularVelocity() {
        double r = (earth.getRadius() + 400.0) * 1_000.0; // 400 km LEO
        double vCirc = mechanics.circularVelocity(earth, r);
        double vVisviva = mechanics.speedFromVisViva(earth, r, r);
        assertEquals(vCirc, vVisviva, vCirc * 1.0e-6);
    }

    @Test
    void speedFromVisViva_atPericenter_greaterThanCircular() {
        double rPeri = (earth.getRadius() + 400.0) * 1_000.0;
        double rApo = (earth.getRadius() + 1000.0) * 1_000.0;
        double a = (rPeri + rApo) / 2.0;
        double vCirc = mechanics.circularVelocity(earth, rPeri);
        double vPeri = mechanics.speedFromVisViva(earth, rPeri, a);
        assertTrue(vPeri > vCirc,
                "Pericenter speed on ellipse should exceed circular speed at same radius");
    }

    // ── Semi-major axis from periapsis ─────────────────────────────────────

    @Test
    void semiMajorAxisForCircularOrbit_equalsRadius() {
        double r = (earth.getRadius() + 400.0) * 1_000.0;
        double a = mechanics.semiMajorAxisFromPeriapsis(r, 0.0);
        assertEquals(r, a, r * 1.0e-9,
                "For e=0, semi-major axis should equal periapsis radius");
    }

    // ── Period from energy ─────────────────────────────────────────────────

    @Test
    void periodFromEnergy_forBoundOrbit_isPositive() {
        double r = (earth.getRadius() + 400.0) * 1_000.0;
        double mu = Earth.G * earth.getMass();
        double specificEnergy = -mu / (2.0 * r); // circular orbit
        double period = mechanics.estimatePeriodFromEnergy(earth, specificEnergy);
        assertTrue(period > 0, "Period for bound orbit should be positive");
    }

    @Test
    void periodFromEnergy_forUnboundOrbit_isNaN() {
        double period = mechanics.estimatePeriodFromEnergy(earth, 0.0);
        assertTrue(Double.isNaN(period), "Unbound orbit (energy >= 0) should return NaN");
    }
}
