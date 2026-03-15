package com.simulator.physics;

import com.simulator.model.Orbit;

/**
 * Immutable Hohmann transfer definition and runtime state.
 */
public class OrbitalTransfer {

    public enum TransferPhase {
        PLANNED,
        FIRST_BURN_DONE,
        COASTING,
        CIRCULARIZED,
        COMPLETED,
        CANCELLED
    }

    private final Orbit.OrbitType initialOrbit;
    private final Orbit.OrbitType targetOrbit;

    private final double radiusInitialM;
    private final double radiusTargetM;
    private final double semiMajorAxisTransferM;
    private final double eccentricity;

    private final double deltaV1Ms;
    private final double deltaV2Ms;
    private final double transferTimeSeconds;

    private final Vector2D periapsisDirection;

    private TransferPhase phase;
    private double elapsedSeconds;
    private boolean apoapsisBurnDone;

    public OrbitalTransfer(Orbit.OrbitType initialOrbit,
                           Orbit.OrbitType targetOrbit,
                           double radiusInitialM,
                           double radiusTargetM,
                           double semiMajorAxisTransferM,
                           double eccentricity,
                           double deltaV1Ms,
                           double deltaV2Ms,
                           double transferTimeSeconds,
                           Vector2D periapsisDirection) {
        this.initialOrbit = initialOrbit;
        this.targetOrbit = targetOrbit;
        this.radiusInitialM = radiusInitialM;
        this.radiusTargetM = radiusTargetM;
        this.semiMajorAxisTransferM = semiMajorAxisTransferM;
        this.eccentricity = eccentricity;
        this.deltaV1Ms = deltaV1Ms;
        this.deltaV2Ms = deltaV2Ms;
        this.transferTimeSeconds = transferTimeSeconds;
        this.periapsisDirection = periapsisDirection.normalized();
        this.phase = TransferPhase.PLANNED;
    }

    public Orbit.OrbitType getInitialOrbit() {
        return initialOrbit;
    }

    public Orbit.OrbitType getTargetOrbit() {
        return targetOrbit;
    }

    public double getRadiusInitialM() {
        return radiusInitialM;
    }

    public double getRadiusTargetM() {
        return radiusTargetM;
    }

    public double getSemiMajorAxisTransferM() {
        return semiMajorAxisTransferM;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public double getDeltaV1Ms() {
        return deltaV1Ms;
    }

    public double getDeltaV2Ms() {
        return deltaV2Ms;
    }

    public double getTransferTimeSeconds() {
        return transferTimeSeconds;
    }

    public Vector2D getPeriapsisDirection() {
        return periapsisDirection;
    }

    public TransferPhase getPhase() {
        return phase;
    }

    public void setPhase(TransferPhase phase) {
        this.phase = phase;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void addElapsedSeconds(double dtSeconds) {
        this.elapsedSeconds += dtSeconds;
    }

    public boolean isApoapsisBurnDone() {
        return apoapsisBurnDone;
    }

    public void setApoapsisBurnDone(boolean apoapsisBurnDone) {
        this.apoapsisBurnDone = apoapsisBurnDone;
    }
}

