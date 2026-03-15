package com.simulator.ui;

import com.simulator.model.Satellite;
import com.simulator.model.TelemetryData;
import javafx.scene.control.Label;

/**
 * Small presenter class that maps telemetry data to JavaFX labels.
 */
public class TelemetryPanel {

    private final Label selectedSatelliteLabel;
    private final Label orbitTypeLabel;
    private final Label altitudeLabel;
    private final Label velocityLabel;
    private final Label periodLabel;
    private final Label initialSpeedLabel;
    private final Label inclinationLabel;
    private final Label massLabel;
    private final Label energyLabel;

    public TelemetryPanel(Label selectedSatelliteLabel,
                          Label orbitTypeLabel,
                          Label altitudeLabel,
                          Label velocityLabel,
                          Label periodLabel,
                          Label initialSpeedLabel,
                          Label inclinationLabel,
                          Label massLabel,
                          Label energyLabel) {
        this.selectedSatelliteLabel = selectedSatelliteLabel;
        this.orbitTypeLabel = orbitTypeLabel;
        this.altitudeLabel = altitudeLabel;
        this.velocityLabel = velocityLabel;
        this.periodLabel = periodLabel;
        this.initialSpeedLabel = initialSpeedLabel;
        this.inclinationLabel = inclinationLabel;
        this.massLabel = massLabel;
        this.energyLabel = energyLabel;
    }

    public void showSatellite(Satellite satellite) {
        if (satellite == null) {
            selectedSatelliteLabel.setText("Satellite: none");
            orbitTypeLabel.setText("Orbit Type: --");
            altitudeLabel.setText("Altitude:  -- km");
            velocityLabel.setText("Velocity:  -- km/s");
            periodLabel.setText("Period:    -- min");
            initialSpeedLabel.setText("Launch v0: -- km/s");
            inclinationLabel.setText("Inclination: -- deg");
            massLabel.setText("Mass: -- kg");
            energyLabel.setText("Energy:   -- MJ/kg");
            return;
        }

        TelemetryData data = TelemetryData.fromSatellite(satellite);
        selectedSatelliteLabel.setText("Satellite: " + data.getSatelliteName());
        orbitTypeLabel.setText("Orbit Type: " + satellite.getOrbit().getType().name());
        altitudeLabel.setText(String.format("Altitude:  %.1f km", data.getAltitude()));
        velocityLabel.setText(String.format("Velocity:  %.2f km/s", data.getVelocity()));
        initialSpeedLabel.setText(String.format("Launch v0: %.2f km/s", data.getInitialSpeed()));
        inclinationLabel.setText(String.format("Inclination: %.1f deg", data.getInclination()));
        massLabel.setText(String.format("Mass: %.0f kg", data.getMassKg()));

        if (Double.isFinite(data.getOrbitalPeriod())) {
            periodLabel.setText(String.format("Period:    %.1f min", data.getOrbitalPeriod() / 60.0));
        } else {
            periodLabel.setText("Period:    Open trajectory");
        }

        energyLabel.setText(String.format("Energy:   %.2f MJ/kg", data.getSpecificEnergy() / 1_000_000.0));
    }
}


