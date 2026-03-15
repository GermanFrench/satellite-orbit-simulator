package com.simulator.model;

import com.simulator.physics.Vector2D;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Represents a launch vehicle state during ascent.
 */
public class Rocket {

    public enum RocketState {
        PREPARED,
        ASCENT,
        DEPLOYED,
        ABORTED
    }

    private final String name;
    private final double dryMassKg;
    private double fuelMassKg;
    private final double maxThrustN;

    private Vector2D positionM = Vector2D.ZERO;
    private Vector2D velocityMs = Vector2D.ZERO;
    private Vector2D accelerationMs2 = Vector2D.ZERO;

    private RocketState state = RocketState.PREPARED;
    private double elapsedTimeS;

    private final Deque<Vector2D> exhaustTrail = new ArrayDeque<>();

    public Rocket(String name, double dryMassKg, double fuelMassKg, double maxThrustN) {
        this.name = name;
        this.dryMassKg = dryMassKg;
        this.fuelMassKg = fuelMassKg;
        this.maxThrustN = maxThrustN;
    }

    public String getName() {
        return name;
    }

    public double getDryMassKg() {
        return dryMassKg;
    }

    public double getFuelMassKg() {
        return fuelMassKg;
    }

    public void setFuelMassKg(double fuelMassKg) {
        this.fuelMassKg = Math.max(0.0, fuelMassKg);
    }

    public double getMassKg() {
        return dryMassKg + fuelMassKg;
    }

    public double getMaxThrustN() {
        return maxThrustN;
    }

    public Vector2D getPositionM() {
        return positionM;
    }

    public void setPositionM(Vector2D positionM) {
        this.positionM = positionM;
    }

    public Vector2D getVelocityMs() {
        return velocityMs;
    }

    public void setVelocityMs(Vector2D velocityMs) {
        this.velocityMs = velocityMs;
    }

    public Vector2D getAccelerationMs2() {
        return accelerationMs2;
    }

    public void setAccelerationMs2(Vector2D accelerationMs2) {
        this.accelerationMs2 = accelerationMs2;
    }

    public RocketState getState() {
        return state;
    }

    public void setState(RocketState state) {
        this.state = state;
    }

    public double getElapsedTimeS() {
        return elapsedTimeS;
    }

    public void addElapsedTime(double dtS) {
        this.elapsedTimeS += dtS;
    }

    public Deque<Vector2D> getExhaustTrail() {
        return exhaustTrail;
    }
}

