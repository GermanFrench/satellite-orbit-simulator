package com.simulator.ui;

/**
 * Immutable star sprite used by the background renderer.
 */
public class Star {

    private final double x;
    private final double y;
    private final double brightness;
    private final double size;

    public Star(double x, double y, double brightness, double size) {
        this.x = x;
        this.y = y;
        this.brightness = brightness;
        this.size = size;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getBrightness() {
        return brightness;
    }

    public double getSize() {
        return size;
    }
}

