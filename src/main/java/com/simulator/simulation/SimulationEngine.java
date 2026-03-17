package com.simulator.simulation;

import com.simulator.model.Earth;
import com.simulator.model.Orbit;
import com.simulator.model.Satellite;
import com.simulator.model.TransferTelemetry;
import com.simulator.physics.OrbitalTransfer;
import com.simulator.physics.OrbitIntegrator;
import com.simulator.physics.OrbitalMechanics;
import com.simulator.physics.PhysicsEngine;
import com.simulator.physics.Vector2D;
import javafx.animation.AnimationTimer;

import java.util.List;

/**
 * Drives the real-time simulation loop.
 *
 * <p>A JavaFX {@link AnimationTimer} is used so that all position updates
 * happen on the JavaFX Application Thread, keeping UI access thread-safe.</p>
 *
 * <p>Simulation speed is a multiplier applied on top of real-time angular
 * velocity.  A value of {@code 100} means 100 × real-time, which lets a
 * low-Earth orbit (~92 min period) complete in about 55 seconds of wall-clock
 * time at the default slider position.</p>
 */
public class SimulationEngine {

    /** Time acceleration factor applied on top of the UI speed slider value. */
    private static final double TIME_ACCELERATION = 180.0;

    /** Maximum internal integration step in simulated seconds. */
    private static final double MAX_INTEGRATION_STEP = 1.2;

    private final Earth earth;
    private final SatelliteManager satelliteManager;
    private final PhysicsEngine physicsEngine;
    private final OrbitalMechanics orbitalMechanics;
    private final OrbitIntegrator orbitIntegrator;
    private final LaunchSimulationEngine launchSimulationEngine;
    private final TransferSimulationController transferSimulationController;

    /** JavaFX animation loop — recreated on each start to reset timing. */
    private AnimationTimer animationTimer;

    /** Whether the simulation is currently running. */
    private boolean running = false;

    /** Nanosecond timestamp of the previous animation frame (0 = not started). */
    private long lastUpdate = 0;

    /**
     * Speed multiplier supplied by the UI speed slider (range 0.5 – 10).
     * The effective time-acceleration is {@code simulationSpeed × 100}.
     */
    private double simulationSpeed = 1.0;

    /**
     * Callback invoked on the JavaFX Application Thread after every position
     * update.  Typically triggers a canvas redraw and telemetry refresh.
     */
    private Runnable onUpdate;

    /**
     * Creates a simulation engine for Earth and the shared satellite manager.
     *
     * @param earth            the Earth model
     * @param satelliteManager manager for active satellites and selection
     */
    public SimulationEngine(Earth earth, SatelliteManager satelliteManager) {
        this.earth = earth;
        this.satelliteManager = satelliteManager;
        this.physicsEngine = new PhysicsEngine(earth);
        this.orbitalMechanics = new OrbitalMechanics();
        this.orbitIntegrator = new OrbitIntegrator(physicsEngine);
        this.launchSimulationEngine = new LaunchSimulationEngine(earth);
        this.transferSimulationController = new TransferSimulationController(earth);
    }

    // -------------------------------------------------------------------------
    // Simulation control
    // -------------------------------------------------------------------------

    /**
     * Starts (or resumes) the simulation animation loop.
     * Calling this when already running has no effect.
     */
    public void startSimulation() {
        if (!running) {
            running = true;
            lastUpdate = 0;
            animationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    updateSatellitePosition(now);
                }
            };
            animationTimer.start();
        }
    }

    /**
     * Pauses the simulation, preserving the satellite's current angle.
     * Calling this when already paused has no effect.
     */
    public void pauseSimulation() {
        if (running && animationTimer != null) {
            running = false;
            animationTimer.stop();
        }
    }

    /**
     * Stops the simulation and resets the satellite angle to zero (start position).
     * After reset the simulation is paused; call {@link #startSimulation()} to
     * restart.
     */
    public void resetSimulation() {
        pauseSimulation();
        launchSimulationEngine.abortLaunch();
        for (Satellite satellite : satelliteManager.getSatellites()) {
            initializePhysicalState(satellite);
        }
        lastUpdate = 0;
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    // -------------------------------------------------------------------------
    // Per-frame update
    // -------------------------------------------------------------------------

    /**
     * Advances the satellite's angle by the angular distance covered in the
     * elapsed time since the previous frame.
     *
     * <p>Called automatically by the {@link AnimationTimer} on every rendered
     * frame.</p>
     *
     * @param now current nanosecond timestamp provided by the AnimationTimer
     */
    public void updateSatellitePosition(long now) {
        if (lastUpdate == 0) {
            // First frame after (re)start — skip to avoid a huge delta-time jump.
            lastUpdate = now;
            return;
        }

        double deltaTime = (now - lastUpdate) / 1_000_000_000.0; // ns → s
        lastUpdate = now;

        double simulatedDt = deltaTime * simulationSpeed * TIME_ACCELERATION;
        int steps = Math.max(1, (int) Math.ceil(simulatedDt / MAX_INTEGRATION_STEP));
        double dtStep = simulatedDt / steps;

        launchSimulationEngine.update(simulatedDt);

        for (Satellite satellite : satelliteManager.getSatellites()) {
            if (launchSimulationEngine.isSatelliteUnderLaunch(satellite)) {
                refreshDerivedState(satellite);
                continue;
            }
            for (int i = 0; i < steps; i++) {
                orbitIntegrator.step(satellite, dtStep);
            }
            transferSimulationController.updateTransfer(satellite, simulatedDt);
            refreshDerivedState(satellite);
        }

        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    // -------------------------------------------------------------------------
    // Property updates
    // -------------------------------------------------------------------------

    /**
     * Recalculates the satellite's velocity and period for the current altitude.
     * Called whenever the altitude changes.
     */
    private void initializePhysicalState(Satellite satellite) {
        Orbit orbit = satellite.getOrbit();
        double eccentricity = orbit.getEccentricity();

        double periapsisM = (earth.getRadius() + orbit.getAltitudeKm()) * 1_000.0;
        double semiMajorAxisM = orbitalMechanics.semiMajorAxisFromPeriapsis(periapsisM, eccentricity);
        double recommendedSpeedMs = orbitalMechanics.speedFromVisViva(earth, periapsisM, semiMajorAxisM);
        if (satellite.getInitialSpeedKmS() <= 0.0) {
            satellite.setInitialSpeedKmS(recommendedSpeedMs / 1_000.0);
        }

        double theta = Math.toRadians(orbit.getInclinationDeg());
        Vector2D position = new Vector2D(periapsisM * Math.cos(theta), periapsisM * Math.sin(theta));
        Vector2D tangent = new Vector2D(-Math.sin(theta), Math.cos(theta));
        Vector2D velocity = tangent.multiply(satellite.getInitialSpeedKmS() * 1_000.0);

        satellite.setPositionM(position);
        satellite.setVelocityVectorMs(velocity);
        satellite.setAccelerationVectorMs2(
                physicsEngine.calculateAcceleration(satellite.getPositionM(), satellite.getMassKg())
        );
        refreshDerivedState(satellite);
    }

    private void refreshDerivedState(Satellite satellite) {
        Vector2D position = satellite.getPositionM();
        Vector2D velocity = satellite.getVelocityVectorMs();

        double radiusM = position.magnitude();
        double speedMs = velocity.magnitude();
        double altitudeKm = radiusM / 1_000.0 - earth.getRadius();

        satellite.setAltitude(Math.max(0.0, altitudeKm));
        satellite.setVelocity(speedMs / 1_000.0);
        satellite.setAngle(normalizeAngle(Math.atan2(position.getY(), position.getX())));

        double specificEnergy = 0.5 * speedMs * speedMs - physicsEngine.getMu() / radiusM;
        satellite.setSpecificEnergy(specificEnergy);

        satellite.setOrbitalPeriod(orbitalMechanics.estimatePeriodFromEnergy(earth, specificEnergy));

        // Stop below surface to avoid unstable exploding states.
        if (altitudeKm <= 0.0) {
            Vector2D surfacePosition = position.normalized().multiply(earth.getRadius() * 1_000.0);
            satellite.setPositionM(surfacePosition);
            satellite.setVelocityVectorMs(Vector2D.ZERO);
            satellite.setAccelerationVectorMs2(Vector2D.ZERO);
        }
    }

    /**
     * Updates the satellite's altitude and immediately recalculates derived
     * orbital properties.
     *
     * @param altitude new altitude above Earth's surface in kilometres
     */
    public void setAltitude(Satellite satellite, double altitude) {
        satellite.getOrbit().setAltitudeKm(altitude);
        satellite.setAltitude(altitude);
        initializePhysicalState(satellite);
    }

    /**
     * Updates launch speed and resets orbit state so the new trajectory starts
     * immediately from the same launch point.
     *
     * @param initialSpeedKmS launch speed in km/s
     */
    public void setInitialSpeed(Satellite satellite, double initialSpeedKmS) {
        satellite.setInitialSpeedKmS(initialSpeedKmS);
        initializePhysicalState(satellite);
    }

    public void setInclination(Satellite satellite, double inclinationDeg) {
        satellite.getOrbit().setInclinationDeg(inclinationDeg);
        initializePhysicalState(satellite);
    }

    public void setMass(Satellite satellite, double massKg) {
        satellite.setMassKg(massKg);
        initializePhysicalState(satellite);
    }

    public void setOrbitType(Satellite satellite, Orbit.OrbitType type) {
        satellite.getOrbit().setType(type);
        if (type != Orbit.OrbitType.CUSTOM) {
            satellite.getOrbit().setAltitudeKm(orbitalMechanics.getPresetAltitudeKm(type));
            satellite.setInitialSpeedKmS(orbitalMechanics.getTypicalVelocityKmS(type));
        }
        initializePhysicalState(satellite);
    }

    public void addSatellite(Satellite satellite) {
        satelliteManager.addSatellite(satellite);
        initializePhysicalState(satellite);
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    /**
     * Adds a satellite that has already been physically positioned and given a
     * velocity by the launch engine.  Unlike {@link #addSatellite}, this method
     * does <em>not</em> reset position/velocity — it only refreshes the derived
     * display fields (altitude, speed, period, energy).
     *
     * @param satellite satellite delivered by the rocket's deploy callback
     */
    public void addDeployedSatellite(Satellite satellite) {
        satelliteManager.addSatellite(satellite);
        refreshDerivedState(satellite);
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    public void removeSatellite(Satellite satellite) {
        launchSimulationEngine.abortLaunchForSatellite(satellite);
        transferSimulationController.clearSatelliteState(satellite);
        satelliteManager.removeSatellite(satellite);
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    public List<Satellite> getSatellites() {
        return satelliteManager.getSatellites();
    }

    public Satellite getSelectedSatellite() {
        return satelliteManager.getSelectedSatellite();
    }

    public void setSelectedSatellite(Satellite satellite) {
        satelliteManager.setSelectedSatellite(satellite);
    }

    public LaunchSimulationEngine getLaunchSimulationEngine() {
        return launchSimulationEngine;
    }

    public OrbitalTransfer executeHohmannTransfer(Satellite satellite,
                                                  Orbit.OrbitType current,
                                                  Orbit.OrbitType target) {
        if (launchSimulationEngine.isSatelliteUnderLaunch(satellite)) {
            return null;
        }
        return transferSimulationController.executeTransfer(satellite, current, target);
    }

    public boolean cancelHohmannTransfer(Satellite satellite) {
        return transferSimulationController.cancelTransfer(satellite);
    }

    public OrbitalTransfer getActiveTransfer(Satellite satellite) {
        return transferSimulationController.getTransfer(satellite);
    }

    public TransferTelemetry getTransferTelemetry(Satellite satellite) {
        return transferSimulationController.getTelemetry(satellite);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Sets the simulation speed multiplier (matches the UI slider value).
     * The effective time-acceleration factor is {@code speed × 100}.
     *
     * @param speed speed multiplier (positive value)
     */
    public void setSimulationSpeed(double speed) {
        this.simulationSpeed = speed;
    }

    /**
     * Registers a callback that is invoked after every position update.
     * The callback runs on the JavaFX Application Thread.
     *
     * @param onUpdate callback Runnable (may be {@code null} to clear)
     */
    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    /** @return {@code true} if the animation loop is currently active */
    public boolean isRunning() {
        return running;
    }

    private double normalizeAngle(double angleRad) {
        double twoPi = 2.0 * Math.PI;
        double value = angleRad % twoPi;
        return (value < 0.0) ? value + twoPi : value;
    }
}
