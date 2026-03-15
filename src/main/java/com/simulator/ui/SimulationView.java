package com.simulator.ui;

import com.simulator.model.Satellite;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.Random;

/**
 * Renders the orbital simulation onto a JavaFX {@link Canvas}.
 *
 * <p>Each call to {@link #render()} redraws the full canvas in three layers:
 * <ol>
 *   <li>Space background with static stars</li>
 *   <li>Dashed orbit path circle</li>
 *   <li>Earth (with radial gradient and atmosphere glow)</li>
 *   <li>Satellite dot with a soft glow halo</li>
 * </ol>
 * </p>
 *
 * <p>The orbit is drawn as a schematic (not to scale) so that the satellite
 * remains clearly visible for all supported altitudes.</p>
 */
public class SimulationView {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Fixed screen radius of the Earth regardless of actual scale (pixels). */
    private static final double EARTH_SCREEN_RADIUS = 80.0;

    /** Minimum gap between the Earth's edge and the orbit path (pixels). */
    private static final double ORBIT_MIN_PADDING = 50.0;

    /** Scale factor mapping real altitude (km) to extra screen pixels. */
    private static final double ALTITUDE_SCALE = 0.075;

    /** Number of randomly placed background stars. */
    private static final int STAR_COUNT = 120;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final Canvas canvas;
    private final Satellite satellite;

    /**
     * Pre-computed star positions so the background is constant across frames.
     * Layout: [x0, y0, s0,  x1, y1, s1, …]
     */
    private double[] stars;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a SimulationView that draws onto the supplied canvas.
     *
     * @param canvas    the JavaFX canvas to draw on
     * @param satellite the satellite whose position is rendered
     */
    public SimulationView(Canvas canvas, Satellite satellite) {
        this.canvas = canvas;
        this.satellite = satellite;
        precomputeStars(canvas.getWidth(), canvas.getHeight());
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Redraws the entire canvas for the current simulation state.
     * Must be called on the JavaFX Application Thread.
     */
    public void render() {
        double width  = canvas.getWidth();
        double height = canvas.getHeight();
        double cx = width  / 2.0;
        double cy = height / 2.0;

        double orbitRadius = computeScreenOrbitRadius();

        // Update satellite's screen position for the new orbit radius
        satellite.updatePosition(cx, cy, orbitRadius);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Background
        drawBackground(gc, width, height);

        // 2. Orbit path
        drawOrbitPath(gc, cx, cy, orbitRadius);

        // 3. Earth
        drawEarth(gc, cx, cy);

        // 4. Satellite
        drawSatellite(gc, satellite.getX(), satellite.getY());
    }

    /**
     * Computes the screen-space orbital radius for the satellite's current
     * altitude using a schematic (non-linear) mapping.
     *
     * @return orbital radius in pixels
     */
    public double computeScreenOrbitRadius() {
        return EARTH_SCREEN_RADIUS + ORBIT_MIN_PADDING + satellite.getAltitude() * ALTITUDE_SCALE;
    }

    // -------------------------------------------------------------------------
    // Drawing helpers
    // -------------------------------------------------------------------------

    private void drawBackground(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.web("#080818"));
        gc.fillRect(0, 0, width, height);

        // Draw pre-computed stars
        gc.setFill(Color.WHITE);
        for (int i = 0; i < stars.length; i += 3) {
            double sx   = stars[i];
            double sy   = stars[i + 1];
            double size = stars[i + 2];
            gc.fillOval(sx, sy, size, size);
        }
    }

    private void drawOrbitPath(GraphicsContext gc, double cx, double cy, double r) {
        gc.setStroke(Color.web("#4488aa", 0.55));
        gc.setLineWidth(1.2);
        gc.setLineDashes(6, 5);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        gc.setLineDashes(); // reset dash
    }

    private void drawEarth(GraphicsContext gc, double cx, double cy) {
        double r = EARTH_SCREEN_RADIUS;

        // Atmosphere halo (faint blue ring)
        gc.setFill(Color.web("#3366ff", 0.08));
        gc.fillOval(cx - r - 10, cy - r - 10, (r + 10) * 2, (r + 10) * 2);

        // Earth body with a radial gradient to simulate lighting
        RadialGradient earthGrad = new RadialGradient(
                0, 0,
                cx - r * 0.25, cy - r * 0.25,
                r * 1.4,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#5599ff")),
                new Stop(0.55, Color.web("#1155cc")),
                new Stop(1.0, Color.web("#001133"))
        );
        gc.setFill(earthGrad);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Subtle continent suggestion (dark patches)
        gc.setFill(Color.web("#338833", 0.35));
        gc.fillOval(cx - 30, cy - 20, 40, 28);
        gc.fillOval(cx + 10, cy + 15, 30, 20);
        gc.fillOval(cx - 50, cy + 10, 25, 18);
    }

    private void drawSatellite(GraphicsContext gc, double sx, double sy) {
        // Outer glow
        gc.setFill(Color.web("#ffff44", 0.25));
        gc.fillOval(sx - 9, sy - 9, 18, 18);

        // Inner glow
        gc.setFill(Color.web("#ffff88", 0.55));
        gc.fillOval(sx - 6, sy - 6, 12, 12);

        // Core dot
        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(sx - 3, sy - 3, 6, 6);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Pre-generates star positions so they stay fixed across frames. */
    private void precomputeStars(double width, double height) {
        Random rng = new Random(12345L); // fixed seed → deterministic stars
        stars = new double[STAR_COUNT * 3];
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i * 3]     = rng.nextDouble() * width;
            stars[i * 3 + 1] = rng.nextDouble() * height;
            stars[i * 3 + 2] = rng.nextDouble() * 2.0 + 0.5; // size 0.5–2.5 px
        }
    }
}
