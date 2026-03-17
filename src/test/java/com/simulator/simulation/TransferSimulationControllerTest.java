package com.simulator.simulation;

import com.simulator.model.Earth;
import com.simulator.model.Orbit;
import com.simulator.model.Satellite;
import com.simulator.model.TransferTelemetry;
import com.simulator.physics.OrbitalTransfer;
import com.simulator.physics.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferSimulationControllerTest {

    @Test
    void hohmannTelemetryProgressesAndCompletes() {
        Earth earth = new Earth();
        TransferSimulationController transferController = new TransferSimulationController(earth);

        Orbit orbit = new Orbit(Orbit.OrbitType.LEO, Orbit.OrbitType.LEO.getDefaultAltitudeKm(), 0.0, 0.0);
        Satellite satellite = new Satellite("TransferSat", 750.0, orbit, Orbit.OrbitType.LEO.getTypicalVelocityKmS());

        double radiusM = (earth.getRadius() + Orbit.OrbitType.LEO.getDefaultAltitudeKm()) * 1_000.0;
        satellite.setPositionM(new Vector2D(radiusM, 0.0));
        satellite.setVelocityVectorMs(new Vector2D(0.0, Orbit.OrbitType.LEO.getTypicalVelocityKmS() * 1_000.0));

        OrbitalTransfer transfer = transferController.executeTransfer(satellite, Orbit.OrbitType.LEO, Orbit.OrbitType.GEO);
        assertNotNull(transfer, "La transferencia debe crearse");

        transferController.updateTransfer(satellite, transfer.getTransferTimeSeconds() * 0.10);
        TransferTelemetry telemetryMid = transferController.getTelemetry(satellite);

        assertEquals("COASTING", telemetryMid.getPhase(), "La fase debe pasar a COASTING durante el vuelo de transferencia");
        assertTrue(telemetryMid.getDeltaV1KmS() > 0.0);
        assertTrue(telemetryMid.getDeltaV2KmS() > 0.0);

        transferController.updateTransfer(satellite, transfer.getTransferTimeSeconds() * 1.30);
        assertNull(transferController.getTransfer(satellite), "La transferencia activa debe finalizar al completar la circularizacion");

        TransferTelemetry telemetryEnd = transferController.getTelemetry(satellite);
        assertEquals("COMPLETED", telemetryEnd.getPhase(), "La telemetria final debe reflejar transferencia completada");
    }
}

