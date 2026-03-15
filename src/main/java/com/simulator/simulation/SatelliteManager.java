package com.simulator.simulation;

import com.simulator.model.Satellite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps track of active satellites and current UI selection.
 */
public class SatelliteManager {

    private final List<Satellite> satellites = new ArrayList<>();
    private Satellite selectedSatellite;

    public void addSatellite(Satellite satellite) {
        satellites.add(satellite);
        if (selectedSatellite == null) {
            selectedSatellite = satellite;
        }
    }

    public void removeSatellite(Satellite satellite) {
        satellites.remove(satellite);
        if (satellite == selectedSatellite) {
            selectedSatellite = satellites.isEmpty() ? null : satellites.get(0);
        }
    }

    public List<Satellite> getSatellites() {
        return Collections.unmodifiableList(satellites);
    }

    public Satellite getSelectedSatellite() {
        return selectedSatellite;
    }

    public void setSelectedSatellite(Satellite selectedSatellite) {
        this.selectedSatellite = selectedSatellite;
    }
}

