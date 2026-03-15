package com.simulator.simulation;

import com.simulator.model.Earth;
import com.simulator.model.Satellite;
import javafx.animation.AnimationTimer;

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

    private final Satellite satellite;
    private final Earth earth;
    private final OrbitCalculator calculator;

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
     * Creates a simulation engine for the given satellite and Earth model.
     * Orbital velocity and period are calculated immediately.
     *
     * @param satellite the satellite to simulate
     * @param earth     the Earth model
     */
    public SimulationEngine(Satellite satellite, Earth earth) {
        this.satellite = satellite;
        this.earth = earth;
        this.calculator = new OrbitCalculator(earth);
        refreshSatelliteProperties();
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
        satellite.setAngle(0.0);
        lastUpdate = 0;
        refreshSatelliteProperties();
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

        // Orbital radius in metres
        double radiusM = (earth.getRadius() + satellite.getAltitude()) * 1_000.0;

        // Real-time angular velocity ω = v / r  (rad/s)
        double v = calculator.calculateOrbitalVelocity(radiusM);
        double omega = v / radiusM;

        // Advance angle by ω × Δt × speed multiplier
        double newAngle = satellite.getAngle() + omega * deltaTime * simulationSpeed * 100.0;
        satellite.setAngle(newAngle % (2.0 * Math.PI));

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
    private void refreshSatelliteProperties() {
        double radiusM = (earth.getRadius() + satellite.getAltitude()) * 1_000.0;
        double velocityMs = calculator.calculateOrbitalVelocity(radiusM);
        double periodS = calculator.calculateOrbitalPeriod(radiusM);
        satellite.setVelocity(velocityMs / 1_000.0); // m/s → km/s
        satellite.setOrbitalPeriod(periodS);
    }

    /**
     * Updates the satellite's altitude and immediately recalculates derived
     * orbital properties.
     *
     * @param altitude new altitude above Earth's surface in kilometres
     */
    public void setAltitude(double altitude) {
        satellite.setAltitude(altitude);
        refreshSatelliteProperties();
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

    /** @return the {@link OrbitCalculator} used by this engine */
    public OrbitCalculator getCalculator() {
        return calculator;
    }
}
