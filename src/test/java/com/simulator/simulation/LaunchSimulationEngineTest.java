package com.simulator.simulation;

import com.simulator.model.Earth;
import com.simulator.model.LaunchSite;
import com.simulator.model.LaunchTelemetry;
import com.simulator.model.Orbit;
import com.simulator.model.Satellite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchSimulationEngineTest {

    @Test
    void launchTelemetryChangesDuringAscent() {
        Earth earth = new Earth();
        LaunchSimulationEngine launchEngine = new LaunchSimulationEngine(earth);

        Orbit orbit = new Orbit(Orbit.OrbitType.LEO, 400.0, 0.0, 15.0);
        Satellite satellite = new Satellite("TestSat", 750.0, orbit, 7.8);
        LaunchSite site = new LaunchSite("KSC", 28.5, 90.0);

        boolean started = launchEngine.startLaunch(satellite, site, 220.0, 7.6);
        assertTrue(started, "El lanzamiento debe iniciar correctamente");

        LaunchTelemetry telemetry0 = launchEngine.getTelemetry();
        launchEngine.update(2.0);
        LaunchTelemetry telemetry1 = launchEngine.getTelemetry();
        launchEngine.update(2.0);
        LaunchTelemetry telemetry2 = launchEngine.getTelemetry();

        assertNotNull(telemetry0);
        assertNotNull(telemetry1);
        assertNotNull(telemetry2);
        assertTrue(telemetry1.isActive(), "La telemetria debe estar activa durante ascenso");
        assertTrue(telemetry2.getAltitudeKm() > telemetry0.getAltitudeKm(), "La altitud debe aumentar durante el ascenso");
        assertTrue(telemetry2.getVelocityKmS() > telemetry0.getVelocityKmS(), "La velocidad debe aumentar durante el ascenso");
    }
}

