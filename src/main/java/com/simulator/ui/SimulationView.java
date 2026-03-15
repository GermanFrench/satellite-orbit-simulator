package com.simulator.ui;

import com.simulator.model.Earth;
import com.simulator.model.Satellite;
import com.simulator.physics.Vector2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayDeque;
import java.util.Deque;
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

    /** Minimum rendered Earth radius for visual clarity (pixels). */
    private static final double MIN_EARTH_SCREEN_RADIUS = 45.0;

    /** Margin used when fitting physical coordinates into the canvas (pixels). */
    private static final double FIT_MARGIN = 26.0;

    /** Number of randomly placed background stars. */
    private static final int STAR_COUNT = 120;

    /** Max number of points used for the trajectory trail. */
    private static final int MAX_TRAIL_POINTS = 700;

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

    /** Earth model (physical radius/mass constants). */
    private final Earth earth;

    /** Ring buffer holding recent world positions in metres for the trail. */
    private final Deque<Vector2D> trailPoints = new ArrayDeque<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a SimulationView that draws onto the supplied canvas.
     *
     * @param canvas    the JavaFX canvas to draw on
     * @param satellite the satellite whose position is rendered
     */
    public SimulationView(Canvas canvas, Satellite satellite, Earth earth) {
        this.canvas = canvas;
        this.satellite = satellite;
        this.earth = earth;
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

        Vector2D currentPosition = satellite.getPositionM();
        appendTrailPoint(currentPosition);

        double earthRadiusM = earth.getRadius() * 1_000.0;
        double maxRadiusM = earthRadiusM;
        for (Vector2D trailPoint : trailPoints) {
            maxRadiusM = Math.max(maxRadiusM, trailPoint.magnitude());
        }
        maxRadiusM = Math.max(maxRadiusM, currentPosition.magnitude());

        double availableRadiusPx = Math.max(1.0, Math.min(width, height) / 2.0 - FIT_MARGIN);
        double scale = availableRadiusPx / Math.max(maxRadiusM, 1.0);
        double earthRadiusPx = Math.max(MIN_EARTH_SCREEN_RADIUS, earthRadiusM * scale);

        double satX = cx + currentPosition.getX() * scale;
        double satY = cy + currentPosition.getY() * scale;
        satellite.setScreenPosition(satX, satY);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Background
        drawBackground(gc, width, height);

        // 2. Real trajectory trail
        drawTrajectoryTrail(gc);

        // 3. Earth
        drawEarth(gc, cx, cy, earthRadiusPx);

        // 4. Satellite
        drawSatellite(gc, satX, satY);
    }

    /** Clears the orbital trail, useful after reset or orbit parameter changes. */
    public void clearTrail() {
        trailPoints.clear();
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

    private void drawTrajectoryTrail(GraphicsContext gc) {
        if (trailPoints.size() < 2) {
            return;
        }

        double width  = canvas.getWidth();
        double height = canvas.getHeight();
        double cx = width  / 2.0;
        double cy = height / 2.0;

        double earthRadiusM = earth.getRadius() * 1_000.0;
        double maxRadiusM = earthRadiusM;
        for (Vector2D trailPoint : trailPoints) {
            maxRadiusM = Math.max(maxRadiusM, trailPoint.magnitude());
        }
        double availableRadiusPx = Math.max(1.0, Math.min(width, height) / 2.0 - FIT_MARGIN);
        double scale = availableRadiusPx / Math.max(maxRadiusM, 1.0);

        Vector2D[] points = trailPoints.toArray(new Vector2D[0]);
        for (int i = 1; i < points.length; i++) {
            double alpha = (double) i / points.length;
            gc.setStroke(Color.web("#66ddff", 0.10 + alpha * 0.55));
            gc.setLineWidth(1.0 + alpha * 1.1);
            double x1 = cx + points[i - 1].getX() * scale;
            double y1 = cy + points[i - 1].getY() * scale;
            double x2 = cx + points[i].getX() * scale;
            double y2 = cy + points[i].getY() * scale;
            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    private void drawEarth(GraphicsContext gc, double cx, double cy, double r) {

        // Multi-layer atmosphere glow.
        RadialGradient outerGlow = new RadialGradient(
                0, 0,
                cx, cy,
                r * 1.9,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#66b3ff", 0.14)),
                new Stop(0.50, Color.web("#3d8dff", 0.09)),
                new Stop(1.00, Color.web("#1847c8", 0.00))
        );
        gc.setFill(outerGlow);
        gc.fillOval(cx - r * 1.9, cy - r * 1.9, r * 3.8, r * 3.8);

        RadialGradient innerGlow = new RadialGradient(
                0, 0,
                cx, cy,
                r * 1.25,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#9bc8ff", 0.20)),
                new Stop(0.8, Color.web("#3f7cff", 0.08)),
                new Stop(1.0, Color.web("#2050d8", 0.0))
        );
        gc.setFill(innerGlow);
        gc.fillOval(cx - r * 1.25, cy - r * 1.25, r * 2.5, r * 2.5);

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

    private void appendTrailPoint(Vector2D pointM) {
        if (!trailPoints.isEmpty()) {
            Vector2D last = trailPoints.peekLast();
            double dx = pointM.getX() - last.getX();
            double dy = pointM.getY() - last.getY();
            if ((dx * dx + dy * dy) < 2_500_000.0) {
                return;
            }
        }

        trailPoints.addLast(pointM);
        while (trailPoints.size() > MAX_TRAIL_POINTS) {
            trailPoints.removeFirst();
        }
    }
}
