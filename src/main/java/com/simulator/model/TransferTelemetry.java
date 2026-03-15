package com.simulator.model;

import com.simulator.physics.OrbitalTransfer;

/**
 * Telemetry snapshot for Hohmann transfer visualization.
 */
public class TransferTelemetry {

    private final String initialOrbit;
    private final String targetOrbit;
    private final double deltaV1KmS;
    private final double deltaV2KmS;
    private final double transferTimeMinutes;
    private final String phase;

    public TransferTelemetry(String initialOrbit,
                             String targetOrbit,
                             double deltaV1KmS,
                             double deltaV2KmS,
                             double transferTimeMinutes,
                             String phase) {
        this.initialOrbit = initialOrbit;
        this.targetOrbit = targetOrbit;
        this.deltaV1KmS = deltaV1KmS;
        this.deltaV2KmS = deltaV2KmS;
        this.transferTimeMinutes = transferTimeMinutes;
        this.phase = phase;
    }

    public static TransferTelemetry from(OrbitalTransfer transfer) {
        if (transfer == null) {
            return idle();
        }
        return new TransferTelemetry(
                transfer.getInitialOrbit().name(),
                transfer.getTargetOrbit().name(),
                transfer.getDeltaV1Ms() / 1_000.0,
                transfer.getDeltaV2Ms() / 1_000.0,
                transfer.getTransferTimeSeconds() / 60.0,
                transfer.getPhase().name()
        );
    }

    public static TransferTelemetry idle() {
        return new TransferTelemetry("--", "--", 0.0, 0.0, 0.0, "IDLE");
    }

    public String getInitialOrbit() {
        return initialOrbit;
    }

    public String getTargetOrbit() {
        return targetOrbit;
    }

    public double getDeltaV1KmS() {
        return deltaV1KmS;
    }

    public double getDeltaV2KmS() {
        return deltaV2KmS;
    }

    public double getTransferTimeMinutes() {
        return transferTimeMinutes;
    }

    public String getPhase() {
        return phase;
    }
}

