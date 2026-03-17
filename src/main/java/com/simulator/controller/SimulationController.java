package com.simulator.controller;

import com.simulator.model.Earth;
import com.simulator.model.LaunchTelemetry;
import com.simulator.model.Orbit;
import com.simulator.model.Satellite;
import com.simulator.model.TelemetryData;
import com.simulator.model.TransferTelemetry;
import com.simulator.physics.OrbitalTransfer;
import com.simulator.simulation.SimulationEngine;
import com.simulator.simulation.SatelliteManager;
import com.simulator.ui.LaunchController;
import com.simulator.ui.OrbitCanvasRenderer;
import com.simulator.ui.TelemetryPanel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.net.URL;
import java.util.Optional;
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

    /** Slider that adjusts initial tangential speed at launch (km/s). */
    @FXML private Slider initialSpeedSlider;

    /** Slider for orbital inclination (deg). */
    @FXML private Slider inclinationSlider;

    /** Slider for satellite mass (kg). */
    @FXML private Slider massSlider;

    /** Preset selector (LEO/MEO/GEO). */
    @FXML private ComboBox<Orbit.OrbitType> orbitTypeComboBox;

    /** Transfer selectors. */
    @FXML private ComboBox<Orbit.OrbitType> transferCurrentOrbitComboBox;
    @FXML private ComboBox<Orbit.OrbitType> transferTargetOrbitComboBox;

    /** Transfer action buttons. */
    @FXML private Button executeTransferButton;
    @FXML private Button cancelTransferButton;

    /** List of active satellites. */
    @FXML private ListView<Satellite> satelliteListView;
    @FXML private Button removeSatelliteButton;

    /** Launch action buttons. */
    @FXML private Button launchSatelliteButton;
    @FXML private Button cancelLaunchButton;

    /** Launch telemetry labels. */
    @FXML private Label launchStatusLabel;
    @FXML private Label launchAltitudeLabel;
    @FXML private Label launchVelocityLabel;
    @FXML private Label launchThrustLabel;

    /** Telemetry label – altitude. */
    @FXML private Label altitudeLabel;

    /** Telemetry label – orbital velocity. */
    @FXML private Label velocityLabel;

    /** Telemetry label – orbital period. */
    @FXML private Label periodLabel;

    /** Telemetry label – launch speed configured by the user. */
    @FXML private Label initialSpeedLabel;

    /** Telemetry label – specific orbital energy. */
    @FXML private Label energyLabel;

    /** Telemetry label - selected satellite name. */
    @FXML private Label selectedSatelliteLabel;

    /** Telemetry label - selected satellite orbit regime. */
    @FXML private Label orbitTypeLabel;

    /** Telemetry label - selected satellite inclination. */
    @FXML private Label inclinationLabel;

    /** Telemetry label - selected satellite mass. */
    @FXML private Label massLabel;

    /** Transfer telemetry labels. */
    @FXML private Label transferInitialOrbitLabel;
    @FXML private Label transferTargetOrbitLabel;
    @FXML private Label transferDeltaV1Label;
    @FXML private Label transferDeltaV2Label;
    @FXML private Label transferTimeLabel;
    @FXML private Label transferPhaseLabel;

    /** Global action status label explaining disabled actions. */
    @FXML private Label actionStatusLabel;

    // -------------------------------------------------------------------------
    // Simulation objects (shared single satellite instance)
    // -------------------------------------------------------------------------

    private Earth earth;
    private SatelliteManager satelliteManager;
    private SimulationEngine engine;
    private OrbitCanvasRenderer renderer;
    private TelemetryPanel telemetryPanel;
    private LaunchController launchController;
    private final ObservableList<Satellite> satelliteItems = FXCollections.observableArrayList();
    private boolean syncingControls;

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
        earth = new Earth();
        satelliteManager = new SatelliteManager();
        engine = new SimulationEngine(earth, satelliteManager);
        renderer = new OrbitCanvasRenderer(orbitCanvas, earth);

        telemetryPanel = new TelemetryPanel(
                selectedSatelliteLabel,
                orbitTypeLabel,
                altitudeLabel,
                velocityLabel,
                periodLabel,
                initialSpeedLabel,
                inclinationLabel,
                massLabel,
                energyLabel
        );

        launchController = new LaunchController(
                engine.getLaunchSimulationEngine(),
                launchStatusLabel,
                launchAltitudeLabel,
                launchVelocityLabel,
                launchThrustLabel
        );
        engine.getLaunchSimulationEngine().setOnTelemetry(launchController::showTelemetry);

        configureSatelliteList();
        configureOrbitSelector();
        configureTransferSelectors();
        configureAltitudeSlider();
        configureInitialSpeedSlider();
        configureInclinationSlider();
        configureMassSlider();
        configureSpeedSlider();
        wireEngineCallback();

        addSatellite();
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
        renderer.clearAllTrails();
        engine.resetSimulation();
    }

    @FXML
    private void handleAddSatellite() {
        addSatellite();
    }

    @FXML
    private void handleRemoveSatellite() {
        Satellite selected = engine.getSelectedSatellite();
        if (selected == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Satellite");
        confirm.setHeaderText("Delete selected satellite?");
        confirm.setContentText("This will remove " + selected.getDisplayName() + " and clear its trail/active actions.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        renderer.clearTrailFor(selected.getSatelliteId());
        engine.removeSatellite(selected);

        satelliteItems.setAll(engine.getSatellites());
        Satellite fallback = engine.getSelectedSatellite();
        if (fallback != null) {
            satelliteListView.getSelectionModel().select(fallback);
            syncControlsFromSelection(fallback);
        } else {
            satelliteListView.getSelectionModel().clearSelection();
        }

        refreshTransferActionButtons();
        refreshLaunchActionButtons();
        refreshActionStatusMessage();
        refreshTelemetry();
        renderFrame();
    }

    @FXML
    private void handleLaunchSatellite() {
        Satellite selected = engine.getSelectedSatellite();
        if (selected == null) {
            return;
        }
        boolean launched = launchController.launchSelectedSatellite(selected);
        if (launched && !engine.isRunning()) {
            engine.startSimulation();
        }
        refreshLaunchActionButtons();
        refreshTransferActionButtons();
    }

    @FXML
    private void handleCancelLaunch() {
        Satellite selected = engine.getSelectedSatellite();
        boolean aborted = engine.getLaunchSimulationEngine().abortLaunch();
        if (aborted && selected != null) {
            // Restore the selected satellite to its configured orbital state.
            engine.setOrbitType(selected, selected.getOrbit().getType());
            renderer.clearTrailFor(selected.getSatelliteId());
            renderFrame();
            refreshTelemetry();
        }
        refreshLaunchActionButtons();
        refreshTransferActionButtons();
    }

    @FXML
    private void handleExecuteTransfer() {
        Satellite selected = engine.getSelectedSatellite();
        if (selected == null) {
            return;
        }

        Orbit.OrbitType current = transferCurrentOrbitComboBox.getValue();
        Orbit.OrbitType target = transferTargetOrbitComboBox.getValue();
        if (current == null || target == null || current == target) {
            return;
        }

        // Align selected satellite with the chosen starting circular regime.
        engine.setOrbitType(selected, current);
        renderer.clearTrailFor(selected.getSatelliteId());

        OrbitalTransfer transfer = engine.executeHohmannTransfer(selected, current, target);
        if (transfer != null && !engine.isRunning()) {
            engine.startSimulation();
        }

        refreshTelemetry();
        renderFrame();
    }

    @FXML
    private void handleCancelTransfer() {
        Satellite selected = engine.getSelectedSatellite();
        if (selected == null) {
            return;
        }

        boolean cancelled = engine.cancelHohmannTransfer(selected);
        if (cancelled) {
            refreshTelemetry();
            renderFrame();
        }
    }

    // -------------------------------------------------------------------------
    // Slider configuration
    // -------------------------------------------------------------------------

    private void configureSatelliteList() {
        satelliteListView.setItems(satelliteItems);
        satelliteListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSat, newSat) -> {
            engine.setSelectedSatellite(newSat);
            syncControlsFromSelection(newSat);
            refreshTelemetry();
            renderFrame();
            refreshTransferActionButtons();
            refreshLaunchActionButtons();
        });
    }

    private void configureOrbitSelector() {
        orbitTypeComboBox.setItems(FXCollections.observableArrayList(
                Orbit.OrbitType.LEO,
                Orbit.OrbitType.MEO,
                Orbit.OrbitType.GEO,
                Orbit.OrbitType.CUSTOM
        ));
        orbitTypeComboBox.setValue(Orbit.OrbitType.LEO);
        orbitTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (syncingControls) {
                return;
            }
            Satellite selected = engine.getSelectedSatellite();
            if (selected == null || newVal == null) {
                return;
            }
            if (newVal == Orbit.OrbitType.CUSTOM) {
                selected.getOrbit().setType(Orbit.OrbitType.CUSTOM);
                refreshTelemetry();
                return;
            }
            engine.setOrbitType(selected, newVal);
            syncingControls = true;
            altitudeSlider.setValue(selected.getOrbit().getAltitudeKm());
            initialSpeedSlider.setValue(newVal.getTypicalVelocityKmS());
            syncingControls = false;
            renderer.clearTrailFor(selected.getSatelliteId());
            refreshTelemetry();
            renderFrame();
        });
    }

    private void configureTransferSelectors() {
        ObservableList<Orbit.OrbitType> transferItems = FXCollections.observableArrayList(
                Orbit.OrbitType.LEO,
                Orbit.OrbitType.MEO,
                Orbit.OrbitType.GEO
        );
        transferCurrentOrbitComboBox.setItems(transferItems);
        transferTargetOrbitComboBox.setItems(FXCollections.observableArrayList(transferItems));
        transferCurrentOrbitComboBox.setValue(Orbit.OrbitType.LEO);
        transferTargetOrbitComboBox.setValue(Orbit.OrbitType.GEO);

        transferCurrentOrbitComboBox.valueProperty().addListener((obs, oldVal, newVal) -> refreshTransferActionButtons());
        transferTargetOrbitComboBox.valueProperty().addListener((obs, oldVal, newVal) -> refreshTransferActionButtons());
    }

    private void configureAltitudeSlider() {
        altitudeSlider.setMin(180);
        altitudeSlider.setMax(42000);
        altitudeSlider.setValue(400);
        altitudeSlider.setMajorTickUnit(10000);
        altitudeSlider.setMinorTickCount(4);
        altitudeSlider.setShowTickLabels(true);
        altitudeSlider.setShowTickMarks(true);

        altitudeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (syncingControls) {
                return;
            }
            Satellite selected = engine.getSelectedSatellite();
            if (selected == null) {
                return;
            }
            selected.getOrbit().setType(Orbit.OrbitType.CUSTOM);
            syncingControls = true;
            orbitTypeComboBox.setValue(selected.getOrbit().getType());
            syncingControls = false;
            engine.setAltitude(selected, newVal.doubleValue());
            renderer.clearTrailFor(selected.getSatelliteId());
            renderFrame();
            refreshTelemetry();
        });
    }

    private void configureInitialSpeedSlider() {
        initialSpeedSlider.setMin(2.5);
        initialSpeedSlider.setMax(12.0);
        initialSpeedSlider.setValue(7.7);
        initialSpeedSlider.setMajorTickUnit(1.0);
        initialSpeedSlider.setMinorTickCount(4);
        initialSpeedSlider.setShowTickLabels(true);
        initialSpeedSlider.setShowTickMarks(true);

        initialSpeedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (syncingControls) {
                return;
            }
            Satellite selected = engine.getSelectedSatellite();
            if (selected == null) {
                return;
            }
            engine.setInitialSpeed(selected, newVal.doubleValue());
            renderer.clearTrailFor(selected.getSatelliteId());
            renderFrame();
            refreshTelemetry();
        });
    }

    private void configureInclinationSlider() {
        inclinationSlider.setMin(0.0);
        inclinationSlider.setMax(180.0);
        inclinationSlider.setValue(15.0);
        inclinationSlider.setMajorTickUnit(45.0);
        inclinationSlider.setMinorTickCount(4);
        inclinationSlider.setShowTickLabels(true);
        inclinationSlider.setShowTickMarks(true);

        inclinationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            Satellite selected = engine.getSelectedSatellite();
            if (selected == null) {
                return;
            }
            engine.setInclination(selected, newVal.doubleValue());
            renderer.clearTrailFor(selected.getSatelliteId());
            renderFrame();
            refreshTelemetry();
        });
    }

    private void configureMassSlider() {
        massSlider.setMin(100.0);
        massSlider.setMax(3000.0);
        massSlider.setValue(Satellite.DEFAULT_MASS_KG);
        massSlider.setMajorTickUnit(500.0);
        massSlider.setMinorTickCount(4);
        massSlider.setShowTickLabels(true);
        massSlider.setShowTickMarks(true);

        massSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            Satellite selected = engine.getSelectedSatellite();
            if (selected == null) {
                return;
            }
            engine.setMass(selected, newVal.doubleValue());
            renderer.clearTrailFor(selected.getSatelliteId());
            renderFrame();
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
            renderFrame();
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
        telemetryPanel.showSatellite(engine.getSelectedSatellite());
        refreshLaunchTelemetry();
        refreshTransferTelemetry();
        refreshLaunchActionButtons();
        refreshActionStatusMessage();
    }

    private void refreshLaunchTelemetry() {
        LaunchTelemetry launchTelemetry = engine.getLaunchSimulationEngine().getTelemetry();
        launchController.showTelemetry(launchTelemetry);
    }

    private void refreshTransferTelemetry() {
        Satellite selected = engine.getSelectedSatellite();
        TransferTelemetry telemetry = engine.getTransferTelemetry(selected);
        transferInitialOrbitLabel.setText("Transfer from: " + telemetry.getInitialOrbit());
        transferTargetOrbitLabel.setText("Transfer to:   " + telemetry.getTargetOrbit());
        transferDeltaV1Label.setText(String.format("Delta-v1: %.3f km/s", telemetry.getDeltaV1KmS()));
        transferDeltaV2Label.setText(String.format("Delta-v2: %.3f km/s", telemetry.getDeltaV2KmS()));

        OrbitalTransfer activeTransfer = engine.getActiveTransfer(selected);
        if (activeTransfer != null) {
            double elapsedMin = activeTransfer.getElapsedSeconds() / 60.0;
            double totalMin = Math.max(telemetry.getTransferTimeMinutes(), 0.0001);
            double progress = Math.min(100.0, (elapsedMin / totalMin) * 100.0);

            transferTimeLabel.setText(String.format("Transfer time: %.1f / %.1f min", elapsedMin, totalMin));
            transferPhaseLabel.setText(String.format("Transfer phase: %s (%.0f%%)", telemetry.getPhase(), progress));
        } else {
            transferTimeLabel.setText(String.format("Transfer time: %.1f min", telemetry.getTransferTimeMinutes()));
            transferPhaseLabel.setText("Transfer phase: " + telemetry.getPhase());
        }

        refreshTransferActionButtons();
    }

    private void refreshTransferActionButtons() {
        Satellite selected = engine.getSelectedSatellite();
        boolean hasSelected = selected != null;
        boolean hasActiveTransfer = hasSelected && engine.getActiveTransfer(selected) != null;
        boolean underLaunch = hasSelected && engine.getLaunchSimulationEngine().isSatelliteUnderLaunch(selected);

        Orbit.OrbitType current = transferCurrentOrbitComboBox.getValue();
        Orbit.OrbitType target = transferTargetOrbitComboBox.getValue();
        boolean validTransferRequest = hasSelected
                && current != null
                && target != null
                && current != target
                && !underLaunch;

        executeTransferButton.setDisable(!validTransferRequest || hasActiveTransfer);
        cancelTransferButton.setDisable(!hasActiveTransfer);
    }

    private void refreshLaunchActionButtons() {
        Satellite selected = engine.getSelectedSatellite();
        boolean hasSelected = selected != null;
        boolean launchActive = engine.getLaunchSimulationEngine().isActive();
        boolean hasActiveTransfer = hasSelected && engine.getActiveTransfer(selected) != null;

        removeSatelliteButton.setDisable(!hasSelected);
        launchSatelliteButton.setDisable(!hasSelected || launchActive || hasActiveTransfer);
        cancelLaunchButton.setDisable(!launchActive);
    }

    private void refreshActionStatusMessage() {
        Satellite selected = engine.getSelectedSatellite();
        if (selected == null) {
            actionStatusLabel.setText("State: Select a satellite to enable actions.");
            return;
        }

        boolean launchActive = engine.getLaunchSimulationEngine().isActive();
        boolean hasActiveTransfer = engine.getActiveTransfer(selected) != null;

        if (launchActive) {
            actionStatusLabel.setText("State: Launch in progress. Transfer is blocked.");
            return;
        }

        if (hasActiveTransfer) {
            actionStatusLabel.setText("State: Transfer in progress. Launch is blocked.");
            return;
        }

        Orbit.OrbitType current = transferCurrentOrbitComboBox.getValue();
        Orbit.OrbitType target = transferTargetOrbitComboBox.getValue();
        if (current == null || target == null || current == target) {
            actionStatusLabel.setText("State: Select two distinct transfer orbits.");
            return;
        }

        actionStatusLabel.setText("State: Ready. Launch and transfer actions are available.");
    }

    private void renderFrame() {
        renderer.render(
                engine.getSatellites(),
                engine.getSelectedSatellite(),
                engine.getLaunchSimulationEngine().getActiveRocket(),
                engine.getActiveTransfer(engine.getSelectedSatellite())
        );
    }

    private void addSatellite() {
        Orbit.OrbitType preset = orbitTypeComboBox.getValue() == null ? Orbit.OrbitType.LEO : orbitTypeComboBox.getValue();
        double initialSpeed = Double.isFinite(preset.getTypicalVelocityKmS())
                ? preset.getTypicalVelocityKmS()
                : initialSpeedSlider.getValue();
        Orbit orbit = new Orbit(
                preset,
                Double.isFinite(preset.getDefaultAltitudeKm()) ? preset.getDefaultAltitudeKm() : altitudeSlider.getValue(),
                0.0,
                inclinationSlider.getValue()
        );

        String name = "Satellite " + (satelliteItems.size() + 1);
        Satellite satellite = new Satellite(name, massSlider.getValue(), orbit, initialSpeed);
        engine.addSatellite(satellite);
        satelliteItems.setAll(engine.getSatellites());
        satelliteListView.getSelectionModel().select(satellite);
        renderFrame();
        refreshTelemetry();
    }

    private void syncControlsFromSelection(Satellite satellite) {
        if (satellite == null) {
            return;
        }
        syncingControls = true;
        altitudeSlider.setValue(satellite.getOrbit().getAltitudeKm());
        initialSpeedSlider.setValue(satellite.getInitialSpeedKmS());
        inclinationSlider.setValue(satellite.getOrbit().getInclinationDeg());
        massSlider.setValue(satellite.getMassKg());
        orbitTypeComboBox.setValue(satellite.getOrbit().getType());
        if (satellite.getOrbit().getType() != Orbit.OrbitType.CUSTOM) {
            transferCurrentOrbitComboBox.setValue(satellite.getOrbit().getType());
        }
        syncingControls = false;
    }
}
