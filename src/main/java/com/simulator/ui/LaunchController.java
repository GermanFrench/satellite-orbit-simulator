package com.simulator.ui;

import com.simulator.model.LaunchSite;
import com.simulator.model.LaunchTelemetry;
import com.simulator.model.Satellite;
import com.simulator.simulation.LaunchSimulationEngine;
import javafx.scene.control.Label;

/**
 * Coordinates launch actions from the UI and maps launch telemetry labels.
 */
public class LaunchController {

    private final LaunchSimulationEngine launchEngine;
    private final Label launchStatusLabel;
    private final Label launchAltitudeLabel;
    private final Label launchVelocityLabel;
    private final Label launchThrustLabel;

    private final LaunchSite defaultSite = new LaunchSite("Cape", 28.5, 90.0);

    public LaunchController(LaunchSimulationEngine launchEngine,
                            Label launchStatusLabel,
                            Label launchAltitudeLabel,
                            Label launchVelocityLabel,
                            Label launchThrustLabel) {
        this.launchEngine = launchEngine;
        this.launchStatusLabel = launchStatusLabel;
        this.launchAltitudeLabel = launchAltitudeLabel;
        this.launchVelocityLabel = launchVelocityLabel;
        this.launchThrustLabel = launchThrustLabel;
    }

    public boolean launchSelectedSatellite(Satellite satellite) {
        boolean started = launchEngine.startLaunch(satellite, defaultSite, 180.0, 7.60);
        if (!started) {
            launchStatusLabel.setText("Launch: busy or no satellite");
        }
        return started;
    }

    public void showTelemetry(LaunchTelemetry telemetry) {
        launchStatusLabel.setText("Launch: " + telemetry.getStatus());
        launchAltitudeLabel.setText(String.format("L-Altitude: %.1f km", telemetry.getAltitudeKm()));
        launchVelocityLabel.setText(String.format("L-Velocity: %.2f km/s", telemetry.getVelocityKmS()));
        launchThrustLabel.setText(String.format("L-Thrust:   %.0f kN", telemetry.getThrustKN()));
    }
}

