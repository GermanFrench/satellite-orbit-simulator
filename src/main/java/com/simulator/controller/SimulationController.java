package com.simulator.controller;

import com.simulator.model.Earth;
import com.simulator.model.Satellite;
import com.simulator.model.TelemetryData;
import com.simulator.simulation.SimulationEngine;
import com.simulator.ui.SimulationView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * JavaFX FXML controller that connects the UI with the simulation engine.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Handle the Start, Pause and Reset buttons</li>
 *   <li>React to altitude and simulation-speed slider changes</li>
 *   <li>Update the telemetry panel on every animation frame</li>
 * </ul>
 * </p>
 */
public class SimulationController implements Initializable {

    // -------------------------------------------------------------------------
    // FXML-injected fields
    // -------------------------------------------------------------------------

    /** Canvas in the centre panel where the orbit is drawn. */
    @FXML private Canvas orbitCanvas;

    /** Slider that adjusts the satellite's altitude (km). */
    @FXML private Slider altitudeSlider;

    /** Slider that adjusts the simulation playback speed. */
    @FXML private Slider speedSlider;

    /** Telemetry label – altitude. */
    @FXML private Label altitudeLabel;

    /** Telemetry label – orbital velocity. */
    @FXML private Label velocityLabel;

    /** Telemetry label – orbital period. */
    @FXML private Label periodLabel;

    // -------------------------------------------------------------------------
    // Simulation objects (shared single satellite instance)
    // -------------------------------------------------------------------------

    private Satellite        satellite;
    private SimulationEngine engine;
    private SimulationView   view;

    // -------------------------------------------------------------------------
    // Initializable
    // -------------------------------------------------------------------------

    /**
     * Initialises models, simulation engine and UI bindings.
     * Called automatically by the FXMLLoader after all @FXML fields are
     * injected.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Earth earth = new Earth();
        satellite = new Satellite(400.0); // default altitude 400 km (similar to the ISS)

        engine = new SimulationEngine(satellite, earth);
        view   = new SimulationView(orbitCanvas, satellite);

        configureAltitudeSlider();
        configureSpeedSlider();
        wireEngineCallback();

        // Draw the initial (static) frame and populate telemetry
        view.render();
        refreshTelemetry();
    }

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    /** Handles the Start button – begins or resumes the simulation. */
    @FXML
    private void handleStart() {
        engine.startSimulation();
    }

    /** Handles the Pause button – freezes the satellite in place. */
    @FXML
    private void handlePause() {
        engine.pauseSimulation();
    }

    /**
     * Handles the Reset button – stops the simulation, resets the satellite
     * angle to zero and redraws the canvas.
     */
    @FXML
    private void handleReset() {
        engine.resetSimulation();
    }

    // -------------------------------------------------------------------------
    // Slider configuration
    // -------------------------------------------------------------------------

    private void configureAltitudeSlider() {
        altitudeSlider.setMin(200);
        altitudeSlider.setMax(2000);
        altitudeSlider.setValue(400);
        altitudeSlider.setMajorTickUnit(450);
        altitudeSlider.setMinorTickCount(4);
        altitudeSlider.setShowTickLabels(true);
        altitudeSlider.setShowTickMarks(true);

        altitudeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            engine.setAltitude(newVal.doubleValue());
            // Provide immediate visual feedback even while the simulation is paused
            view.render();
            refreshTelemetry();
        });
    }

    private void configureSpeedSlider() {
        speedSlider.setMin(0.5);
        speedSlider.setMax(10.0);
        speedSlider.setValue(1.0);
        speedSlider.setMajorTickUnit(2.375);
        speedSlider.setMinorTickCount(3);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                engine.setSimulationSpeed(newVal.doubleValue()));
    }

    // -------------------------------------------------------------------------
    // Engine callback
    // -------------------------------------------------------------------------

    /**
     * Registers the per-frame callback so that every position update triggers
     * a canvas redraw and a telemetry refresh.
     */
    private void wireEngineCallback() {
        engine.setOnUpdate(() -> {
            view.render();
            refreshTelemetry();
        });
    }

    // -------------------------------------------------------------------------
    // Telemetry display
    // -------------------------------------------------------------------------

    /**
     * Reads the satellite's current properties, wraps them in a
     * {@link TelemetryData} snapshot and updates the right-panel labels.
     */
    private void refreshTelemetry() {
        TelemetryData data = new TelemetryData(
                satellite.getAltitude(),
                satellite.getVelocity(),
                satellite.getOrbitalPeriod()
        );

        altitudeLabel.setText(String.format("Altitude:  %.1f km",   data.getAltitude()));
        velocityLabel.setText(String.format("Velocity:  %.2f km/s", data.getVelocity()));

        double periodMin = data.getOrbitalPeriod() / 60.0;
        periodLabel.setText(String.format("Period:    %.1f min",    periodMin));
    }
}
