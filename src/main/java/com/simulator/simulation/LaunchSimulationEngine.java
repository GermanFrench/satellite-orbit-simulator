package com.simulator.simulation;

import com.simulator.model.Earth;
import com.simulator.model.LaunchSite;
import com.simulator.model.LaunchTelemetry;
import com.simulator.model.Rocket;
import com.simulator.model.Satellite;
import com.simulator.physics.RocketPhysics;
import com.simulator.physics.ThrustModel;
import com.simulator.physics.Vector2D;

import java.util.function.Consumer;

/**
 * Handles launch ascent and satellite deployment.
 */
public class LaunchSimulationEngine {

    private static final int MAX_EXHAUST_POINTS = 280;

    private final Earth earth;
    private final RocketPhysics rocketPhysics;
    private final ThrustModel thrustModel;

    private Rocket activeRocket;
    private Satellite launchedSatellite;
    private LaunchSite launchSite;

    private double targetAltitudeKm;
    private double targetVelocityKmS;

    private LaunchTelemetry telemetry = LaunchTelemetry.idle();
    private Consumer<LaunchTelemetry> onTelemetry;
    private Consumer<Satellite> onDeploy;

    public LaunchSimulationEngine(Earth earth) {
        this.earth = earth;
        this.rocketPhysics = new RocketPhysics(earth);
        this.thrustModel = new ThrustModel();
    }

    public boolean startLaunch(Satellite satellite,
                               LaunchSite site,
                               double targetAltitudeKm,
                               double targetVelocityKmS) {
        if (satellite == null || isActive()) {
            return false;
        }

        this.launchedSatellite = satellite;
        this.launchSite = site;
        this.targetAltitudeKm = targetAltitudeKm;
        this.targetVelocityKmS = targetVelocityKmS;

        activeRocket = new Rocket("LV-1", 18_500.0, 115_000.0, 2_100_000.0);
        activeRocket.setState(Rocket.RocketState.ASCENT);

        double radiusM = earth.getRadius() * 1_000.0;
        double theta = Math.toRadians(site.getLatitudeDeg());
        Vector2D startPos = new Vector2D(radiusM * Math.cos(theta), radiusM * Math.sin(theta));
        activeRocket.setPositionM(startPos);
        activeRocket.setVelocityMs(Vector2D.ZERO);

        launchedSatellite.setPositionM(startPos);
        launchedSatellite.setVelocityVectorMs(Vector2D.ZERO);

        pushTelemetry("Ascent");
        return true;
    }

    public void update(double dtSeconds) {
        if (!isActive()) {
            telemetry = LaunchTelemetry.idle();
            emitTelemetry();
            return;
        }

        activeRocket.addElapsedTime(dtSeconds);

        double thrustN = thrustModel.getThrustN(activeRocket);
        double theta = Math.toRadians(launchSite.getLaunchAzimuthDeg());
        Vector2D radial = activeRocket.getPositionM().normalized();
        Vector2D tangential = new Vector2D(-radial.getY(), radial.getX());

        // Gravity turn approximation: mostly vertical first, then gain tangential speed.
        double blend = Math.min(1.0, activeRocket.getElapsedTimeS() / 80.0);
        Vector2D direction = radial.multiply(1.0 - blend).add(tangential.multiply(blend));
        direction = rotate(direction, theta * 0.02);

        Vector2D acceleration = rocketPhysics.calculateTotalAcceleration(activeRocket, thrustN, direction);
        Vector2D velocity = activeRocket.getVelocityMs().add(acceleration.multiply(dtSeconds));
        Vector2D position = activeRocket.getPositionM().add(velocity.multiply(dtSeconds));

        activeRocket.setAccelerationMs2(acceleration);
        activeRocket.setVelocityMs(velocity);
        activeRocket.setPositionM(position);

        double burnRate = thrustModel.getFuelBurnRateKgS(activeRocket, thrustN);
        activeRocket.setFuelMassKg(activeRocket.getFuelMassKg() - burnRate * dtSeconds);

        activeRocket.getExhaustTrail().addLast(position);
        while (activeRocket.getExhaustTrail().size() > MAX_EXHAUST_POINTS) {
            activeRocket.getExhaustTrail().removeFirst();
        }

        launchedSatellite.setPositionM(position);
        launchedSatellite.setVelocityVectorMs(velocity);

        if (shouldDeploy(position, velocity)) {
            activeRocket.setState(Rocket.RocketState.DEPLOYED);
            pushTelemetry("Satellite deployed");
            if (onDeploy != null) {
                onDeploy.accept(launchedSatellite);
            }
            activeRocket = null;
            launchedSatellite = null;
            emitTelemetry();
            return;
        }

        pushTelemetry("Ascent");
    }

    private boolean shouldDeploy(Vector2D position, Vector2D velocity) {
        double altitudeKm = position.magnitude() / 1_000.0 - earth.getRadius();
        double speedKmS = velocity.magnitude() / 1_000.0;
        return altitudeKm >= targetAltitudeKm && speedKmS >= targetVelocityKmS;
    }

    private void pushTelemetry(String status) {
        double altitudeKm = activeRocket.getPositionM().magnitude() / 1_000.0 - earth.getRadius();
        telemetry = new LaunchTelemetry(
                true,
                status,
                Math.max(0.0, altitudeKm),
                activeRocket.getVelocityMs().magnitude() / 1_000.0,
                thrustModel.getThrustN(activeRocket) / 1_000.0
        );
        emitTelemetry();
    }

    private void emitTelemetry() {
        if (onTelemetry != null) {
            onTelemetry.accept(telemetry);
        }
    }

    private Vector2D rotate(Vector2D v, double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        return new Vector2D(
                v.getX() * cos - v.getY() * sin,
                v.getX() * sin + v.getY() * cos
        );
    }

    public boolean isActive() {
        return activeRocket != null && activeRocket.getState() == Rocket.RocketState.ASCENT;
    }

    public boolean isSatelliteUnderLaunch(Satellite satellite) {
        return launchedSatellite != null
                && satellite != null
                && launchedSatellite.getSatelliteId().equals(satellite.getSatelliteId());
    }

    public Rocket getActiveRocket() {
        return activeRocket;
    }

    public LaunchTelemetry getTelemetry() {
        return telemetry;
    }

    public void setOnTelemetry(Consumer<LaunchTelemetry> onTelemetry) {
        this.onTelemetry = onTelemetry;
    }

    public void setOnDeploy(Consumer<Satellite> onDeploy) {
        this.onDeploy = onDeploy;
    }

    /**
     * Aborts current launch and clears rocket visual state.
     *
     * @return {@code true} if a launch was active and got aborted
     */
    public boolean abortLaunch() {
        if (!isActive()) {
            return false;
        }

        activeRocket.setState(Rocket.RocketState.ABORTED);
        telemetry = new LaunchTelemetry(
                false,
                "Aborted",
                Math.max(0.0, activeRocket.getPositionM().magnitude() / 1_000.0 - earth.getRadius()),
                activeRocket.getVelocityMs().magnitude() / 1_000.0,
                0.0
        );
        emitTelemetry();

        activeRocket = null;
        launchedSatellite = null;
        launchSite = null;
        return true;
    }

    /**
     * Aborts launch only if the given satellite is currently under launch.
     *
     * @param satellite satellite that may be attached to the active rocket
     * @return {@code true} if the active launch was aborted
     */
    public boolean abortLaunchForSatellite(Satellite satellite) {
        if (!isSatelliteUnderLaunch(satellite)) {
            return false;
        }
        return abortLaunch();
    }
}


