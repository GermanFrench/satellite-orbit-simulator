package com.simulator.simulation;

import com.simulator.model.Earth;
import com.simulator.model.Orbit;
import com.simulator.model.Satellite;
import com.simulator.model.TransferTelemetry;
import com.simulator.physics.HohmannTransferCalculator;
import com.simulator.physics.OrbitalTransfer;
import com.simulator.physics.Vector2D;

import java.util.HashMap;
import java.util.Map;

/**
 * Executes two-impulse Hohmann transfers on satellites within the simulation loop.
 */
public class TransferSimulationController {

    /** Earliest fraction of transfer time where apoapsis detection is considered valid. */
    private static final double APOAPSIS_MIN_FRACTION = 0.35;

    /** Fallback factor if radial-crossing detection misses apoapsis due to coarse time-step. */
    private static final double APOAPSIS_TIME_FALLBACK_FACTOR = 1.20;

    private final Earth earth;
    private final HohmannTransferCalculator calculator;

    private final Map<String, OrbitalTransfer> activeTransfers = new HashMap<>();
    private final Map<String, TransferTelemetry> lastTelemetryBySatellite = new HashMap<>();
    private final Map<String, Double> previousRadialVelocityBySatellite = new HashMap<>();

    public TransferSimulationController(Earth earth) {
        this.earth = earth;
        this.calculator = new HohmannTransferCalculator(earth);
    }

    public OrbitalTransfer executeTransfer(Satellite satellite, Orbit.OrbitType current, Orbit.OrbitType target) {
        if (satellite == null || current == null || target == null || current == target) {
            return null;
        }

        Vector2D periapsisDirection = satellite.getPositionM().normalized();
        OrbitalTransfer transfer = calculator.calculate(current, target, periapsisDirection);

        applyFirstBurn(satellite, transfer);
        transfer.setPhase(OrbitalTransfer.TransferPhase.FIRST_BURN_DONE);
        activeTransfers.put(satellite.getSatelliteId(), transfer);
        lastTelemetryBySatellite.put(satellite.getSatelliteId(), TransferTelemetry.from(transfer));
        previousRadialVelocityBySatellite.put(
                satellite.getSatelliteId(),
                computeRadialVelocity(satellite.getPositionM(), satellite.getVelocityVectorMs())
        );
        return transfer;
    }

    public void updateTransfer(Satellite satellite, double simulatedDtSeconds) {
        OrbitalTransfer transfer = getTransfer(satellite);
        if (transfer == null) {
            return;
        }

        transfer.addElapsedSeconds(simulatedDtSeconds);
        if (transfer.getPhase() == OrbitalTransfer.TransferPhase.FIRST_BURN_DONE) {
            transfer.setPhase(OrbitalTransfer.TransferPhase.COASTING);
        }

        double radialVelocity = computeRadialVelocity(satellite.getPositionM(), satellite.getVelocityVectorMs());
        double previousRadialVelocity = previousRadialVelocityBySatellite.getOrDefault(
                satellite.getSatelliteId(),
                radialVelocity
        );

        boolean inApoapsisWindow = transfer.getElapsedSeconds() >= transfer.getTransferTimeSeconds() * APOAPSIS_MIN_FRACTION;
        boolean crossedApoapsis = inApoapsisWindow && previousRadialVelocity > 0.0 && radialVelocity <= 0.0;
        boolean fallbackByTime = transfer.getElapsedSeconds() >= transfer.getTransferTimeSeconds() * APOAPSIS_TIME_FALLBACK_FACTOR;

        if (!transfer.isApoapsisBurnDone() && (crossedApoapsis || fallbackByTime)) {
            applySecondBurn(satellite);
            transfer.setApoapsisBurnDone(true);
            transfer.setPhase(OrbitalTransfer.TransferPhase.CIRCULARIZED);
            satellite.getOrbit().setType(transfer.getTargetOrbit());
            satellite.getOrbit().setAltitudeKm(transfer.getTargetOrbit().getDefaultAltitudeKm());
            satellite.setInitialSpeedKmS(transfer.getTargetOrbit().getTypicalVelocityKmS());
            transfer.setPhase(OrbitalTransfer.TransferPhase.COMPLETED);
            lastTelemetryBySatellite.put(satellite.getSatelliteId(), TransferTelemetry.from(transfer));
            activeTransfers.remove(satellite.getSatelliteId());
            previousRadialVelocityBySatellite.remove(satellite.getSatelliteId());
            return;
        }

        previousRadialVelocityBySatellite.put(satellite.getSatelliteId(), radialVelocity);

        if (activeTransfers.containsKey(satellite.getSatelliteId())) {
            lastTelemetryBySatellite.put(satellite.getSatelliteId(), TransferTelemetry.from(transfer));
        }
    }

    public OrbitalTransfer getTransfer(Satellite satellite) {
        if (satellite == null) {
            return null;
        }
        return activeTransfers.get(satellite.getSatelliteId());
    }

    public TransferTelemetry getTelemetry(Satellite satellite) {
        if (satellite == null) {
            return TransferTelemetry.idle();
        }
        OrbitalTransfer transfer = getTransfer(satellite);
        if (transfer != null) {
            return TransferTelemetry.from(transfer);
        }
        return lastTelemetryBySatellite.getOrDefault(satellite.getSatelliteId(), TransferTelemetry.idle());
    }

    public boolean cancelTransfer(Satellite satellite) {
        if (satellite == null) {
            return false;
        }

        OrbitalTransfer transfer = activeTransfers.remove(satellite.getSatelliteId());
        previousRadialVelocityBySatellite.remove(satellite.getSatelliteId());
        if (transfer == null) {
            return false;
        }

        transfer.setPhase(OrbitalTransfer.TransferPhase.CANCELLED);
        lastTelemetryBySatellite.put(satellite.getSatelliteId(), TransferTelemetry.from(transfer));
        return true;
    }

    private void applyFirstBurn(Satellite satellite, OrbitalTransfer transfer) {
        Vector2D tangent = getTangentialDirection(satellite.getPositionM(), satellite.getVelocityVectorMs());
        double newSpeed = satellite.getVelocityVectorMs().magnitude() + transfer.getDeltaV1Ms();
        satellite.setVelocityVectorMs(tangent.multiply(newSpeed));
    }

    private void applySecondBurn(Satellite satellite) {
        double mu = Earth.G * earth.getMass();
        double r = Math.max(satellite.getPositionM().magnitude(), 1.0);
        double circularSpeed = Math.sqrt(mu / r);
        Vector2D tangent = getTangentialDirection(satellite.getPositionM(), satellite.getVelocityVectorMs());
        satellite.setVelocityVectorMs(tangent.multiply(circularSpeed));
    }

    private Vector2D getTangentialDirection(Vector2D position, Vector2D velocity) {
        Vector2D tangent = new Vector2D(-position.getY(), position.getX()).normalized();
        // Keep prograde direction aligned with current velocity.
        if (velocity.magnitude() > 1.0) {
            double dot = tangent.getX() * velocity.getX() + tangent.getY() * velocity.getY();
            if (dot < 0.0) {
                tangent = tangent.multiply(-1.0);
            }
        }
        return tangent;
    }

    private double computeRadialVelocity(Vector2D position, Vector2D velocity) {
        Vector2D radial = position.normalized();
        return radial.getX() * velocity.getX() + radial.getY() * velocity.getY();
    }
}






