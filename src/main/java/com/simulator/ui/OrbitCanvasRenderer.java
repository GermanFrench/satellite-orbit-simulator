package com.simulator.ui;

import com.simulator.model.Earth;
import com.simulator.model.Rocket;
import com.simulator.model.Satellite;
import com.simulator.physics.OrbitalTransfer;
import com.simulator.physics.Vector2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Renders Earth, multiple satellites and their trails on the orbit canvas.
 */
public class OrbitCanvasRenderer {

    private static final int STAR_COUNT = 240;
    private static final int MAX_TRAIL_POINTS = 300;
    private static final double FIT_MARGIN = 24.0;
    private static final double MIN_EARTH_RADIUS_PX = 42.0;

    private static final Color[] SAT_COLORS = new Color[] {
            Color.web("#ffe066"),
            Color.web("#66ddff"),
            Color.web("#ff9f9f"),
            Color.web("#9effb4"),
            Color.web("#d3a4ff"),
            Color.web("#f4b37a")
    };

    private final Canvas canvas;
    private final Earth earth;
    private List<Star> stars;

    private final Map<String, Deque<Vector2D>> trailsBySatellite = new HashMap<>();

    public OrbitCanvasRenderer(Canvas canvas, Earth earth) {
        this.canvas = canvas;
        this.earth = earth;
        precomputeStars(canvas.getWidth(), canvas.getHeight());
    }

    public void render(List<Satellite> satellites,
                       Satellite selectedSatellite,
                       Rocket activeRocket,
                       OrbitalTransfer activeTransfer) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double cx = width / 2.0;
        double cy = height / 2.0;

        drawBackground(gc, width, height);

        double earthRadiusM = earth.getRadius() * 1_000.0;
        double maxRadiusM = earthRadiusM;

        for (Satellite satellite : satellites) {
            appendTrailPoint(satellite);
            maxRadiusM = Math.max(maxRadiusM, satellite.getPositionM().magnitude());
            Deque<Vector2D> trail = trailsBySatellite.get(satellite.getSatelliteId());
            if (trail != null) {
                for (Vector2D point : trail) {
                    maxRadiusM = Math.max(maxRadiusM, point.magnitude());
                }
            }
        }

        if (activeRocket != null) {
            maxRadiusM = Math.max(maxRadiusM, activeRocket.getPositionM().magnitude());
            for (Vector2D point : activeRocket.getExhaustTrail()) {
                maxRadiusM = Math.max(maxRadiusM, point.magnitude());
            }
        }

        if (activeTransfer != null) {
            maxRadiusM = Math.max(maxRadiusM, Math.max(activeTransfer.getRadiusInitialM(), activeTransfer.getRadiusTargetM()));
        }

        double availableRadiusPx = Math.max(1.0, Math.min(width, height) / 2.0 - FIT_MARGIN);
        double scale = availableRadiusPx / Math.max(maxRadiusM, 1.0);
        double earthRadiusPx = Math.max(MIN_EARTH_RADIUS_PX, earthRadiusM * scale);

        drawTrails(gc, satellites, cx, cy, scale);
        drawEarth(gc, cx, cy, earthRadiusPx);
        drawTransferOrbit(gc, activeTransfer, cx, cy, scale);
        drawSatellites(gc, satellites, selectedSatellite, cx, cy, scale);
        drawRocket(gc, activeRocket, cx, cy, scale);
    }

    public void clearAllTrails() {
        trailsBySatellite.clear();
    }

    public void clearTrailFor(String satelliteId) {
        trailsBySatellite.remove(satelliteId);
    }

    private void drawBackground(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.web("#080818"));
        gc.fillRect(0, 0, width, height);

        for (Star star : stars) {
            gc.setFill(Color.rgb(255, 255, 255, star.getBrightness()));
            gc.fillOval(star.getX(), star.getY(), star.getSize(), star.getSize());
        }
    }

    private void drawTrails(GraphicsContext gc, List<Satellite> satellites, double cx, double cy, double scale) {
        for (int satIndex = 0; satIndex < satellites.size(); satIndex++) {
            Satellite satellite = satellites.get(satIndex);
            Deque<Vector2D> trailDeque = trailsBySatellite.get(satellite.getSatelliteId());
            if (trailDeque == null || trailDeque.size() < 2) {
                continue;
            }

            Color baseColor = SAT_COLORS[satIndex % SAT_COLORS.length];
            Color trailColor = baseColor.brighter();

            Vector2D[] points = trailDeque.toArray(new Vector2D[0]);
            for (int i = 1; i < points.length; i++) {
                double alpha = (double) i / points.length;
                gc.setStroke(trailColor.deriveColor(0, 1, 1, 0.05 + alpha * 0.45));
                gc.setLineWidth(0.8 + alpha * 1.4);
                gc.strokeLine(
                        cx + points[i - 1].getX() * scale,
                        cy + points[i - 1].getY() * scale,
                        cx + points[i].getX() * scale,
                        cy + points[i].getY() * scale
                );
            }
        }
    }

    private void drawEarth(GraphicsContext gc, double cx, double cy, double r) {
        // Atmosphere glow with outward fade to simulate scattering.
        RadialGradient outerGlow = new RadialGradient(
                0, 0,
                cx, cy,
                r * 2.15,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#8fc7ff", 0.18)),
                new Stop(0.40, Color.web("#5aa5ff", 0.12)),
                new Stop(0.72, Color.web("#2c78e8", 0.06)),
                new Stop(1.00, Color.web("#1a4bd0", 0.00))
        );
        gc.setFill(outerGlow);
        gc.fillOval(cx - r * 2.15, cy - r * 2.15, r * 4.3, r * 4.3);

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
        gc.fillOval(cx - r, cy - r, r * 2.0, r * 2.0);
    }

    private void drawSatellites(GraphicsContext gc,
                                List<Satellite> satellites,
                                Satellite selectedSatellite,
                                double cx,
                                double cy,
                                double scale) {
        for (int i = 0; i < satellites.size(); i++) {
            Satellite satellite = satellites.get(i);
            Color baseColor = SAT_COLORS[i % SAT_COLORS.length];
            boolean selected = selectedSatellite != null
                    && selectedSatellite.getSatelliteId().equals(satellite.getSatelliteId());

            double sx = cx + satellite.getPositionM().getX() * scale;
            double sy = cy + satellite.getPositionM().getY() * scale;
            satellite.setScreenPosition(sx, sy);

            gc.setFill(baseColor.deriveColor(0, 1, 1, selected ? 0.50 : 0.28));
            gc.fillOval(sx - 8, sy - 8, 16, 16);

            gc.setFill(selected ? Color.WHITE : baseColor.brighter());
            gc.fillOval(sx - (selected ? 3.8 : 3.0), sy - (selected ? 3.8 : 3.0), selected ? 7.6 : 6.0, selected ? 7.6 : 6.0);
        }
    }

    private void appendTrailPoint(Satellite satellite) {
        Deque<Vector2D> trail = trailsBySatellite.computeIfAbsent(
                satellite.getSatelliteId(),
                key -> new ArrayDeque<>()
        );

        Vector2D point = satellite.getPositionM();
        if (!trail.isEmpty()) {
            Vector2D last = trail.peekLast();
            double dx = point.getX() - last.getX();
            double dy = point.getY() - last.getY();
            if ((dx * dx + dy * dy) < 2_500_000.0) {
                return;
            }
        }

        trail.addLast(point);
        while (trail.size() > MAX_TRAIL_POINTS) {
            trail.removeFirst();
        }
    }

    private void drawRocket(GraphicsContext gc, Rocket rocket, double cx, double cy, double scale) {
        if (rocket == null) {
            return;
        }

        Vector2D[] exhaust = rocket.getExhaustTrail().toArray(new Vector2D[0]);
        for (int i = 1; i < exhaust.length; i++) {
            double alpha = (double) i / exhaust.length;
            gc.setStroke(Color.web("#ffb347", 0.08 + alpha * 0.45));
            gc.setLineWidth(1.0 + alpha * 1.1);
            gc.strokeLine(
                    cx + exhaust[i - 1].getX() * scale,
                    cy + exhaust[i - 1].getY() * scale,
                    cx + exhaust[i].getX() * scale,
                    cy + exhaust[i].getY() * scale
            );
        }

        double rx = cx + rocket.getPositionM().getX() * scale;
        double ry = cy + rocket.getPositionM().getY() * scale;

        gc.setFill(Color.web("#ff8844", 0.35));
        gc.fillOval(rx - 8, ry - 8, 16, 16);
        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(rx - 3.2, ry - 3.2, 6.4, 6.4);
    }

    private void drawTransferOrbit(GraphicsContext gc,
                                   OrbitalTransfer transfer,
                                   double cx,
                                   double cy,
                                   double scale) {
        if (transfer == null) {
            return;
        }

        double a = transfer.getSemiMajorAxisTransferM();
        double e = transfer.getEccentricity();
        Vector2D p = transfer.getPeriapsisDirection();
        Vector2D q = new Vector2D(-p.getY(), p.getX());

        gc.setStroke(Color.web("#ffbb66", 0.70));
        gc.setLineWidth(1.6);

        Vector2D previous = null;
        int samples = 220;
        for (int i = 0; i <= samples; i++) {
            double nu = 2.0 * Math.PI * i / samples;
            double r = a * (1.0 - e * e) / (1.0 + e * Math.cos(nu));
            Vector2D point = p.multiply(r * Math.cos(nu)).add(q.multiply(r * Math.sin(nu)));

            if (previous != null) {
                gc.strokeLine(
                        cx + previous.getX() * scale,
                        cy + previous.getY() * scale,
                        cx + point.getX() * scale,
                        cy + point.getY() * scale
                );
            }
            previous = point;
        }

        Vector2D burn1 = p.multiply(transfer.getRadiusInitialM());
        Vector2D burn2 = p.multiply(-transfer.getRadiusTargetM());

        drawBurnMarker(gc, cx + burn1.getX() * scale, cy + burn1.getY() * scale, Color.web("#ffd166"));
        drawBurnMarker(gc, cx + burn2.getX() * scale, cy + burn2.getY() * scale, Color.web("#ff8fab"));
    }

    private void drawBurnMarker(GraphicsContext gc, double x, double y, Color color) {
        gc.setFill(color.deriveColor(0, 1, 1, 0.35));
        gc.fillOval(x - 7, y - 7, 14, 14);
        gc.setFill(color.brighter());
        gc.fillOval(x - 3, y - 3, 6, 6);
    }

    private void precomputeStars(double width, double height) {
        Random rng = new Random(12345L);
        java.util.ArrayList<Star> generated = new java.util.ArrayList<>(STAR_COUNT);
        for (int i = 0; i < STAR_COUNT; i++) {
            double x = rng.nextDouble() * width;
            double y = rng.nextDouble() * height;
            double brightness = 0.35 + rng.nextDouble() * 0.65;
            double size = 0.6 + rng.nextDouble() * 2.2;
            generated.add(new Star(x, y, brightness, size));
        }
        stars = generated;
    }
}





