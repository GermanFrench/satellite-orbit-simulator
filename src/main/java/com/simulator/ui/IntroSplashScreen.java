package com.simulator.ui;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Animated intro splash shown before the main simulator scene appears.
 */
public class IntroSplashScreen {

    private static final String SOFTWARE_TITLE = "Satellite Orbit Simulator";
    private static final int STAR_COUNT = 260;
    private static final int TRAIL_LIMIT = 36;
    private static final Duration FADE_IN_DURATION = Duration.millis(1100);
    private static final Duration HOLD_DURATION = Duration.millis(2500);
    private static final Duration FADE_OUT_DURATION = Duration.millis(900);

    private final List<Star> stars = new ArrayList<>(STAR_COUNT);
    private final Deque<double[]> trailPoints = new ArrayDeque<>();
    private final Random random = new Random(24680L);

    public void play(Stage stage, Scene nextScene, double width, double height) {
        precomputeStars(width, height);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #050814, #0a1030 60%, #060912);");
        root.setOpacity(0.0);

        Canvas canvas = new Canvas(width, height);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        VBox content = buildOverlayContent();
        root.getChildren().addAll(canvas, content);
        StackPane.setAlignment(content, Pos.CENTER);

        Scene splashScene = new Scene(root, width, height);
        AnimationTimer timer = createRenderer(canvas);

        stage.setScene(splashScene);
        stage.show();
        timer.start();

        FadeTransition fadeIn = new FadeTransition(FADE_IN_DURATION, root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition hold = new PauseTransition(HOLD_DURATION);

        FadeTransition fadeOut = new FadeTransition(FADE_OUT_DURATION, root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, hold, fadeOut);
        sequence.setOnFinished(event -> {
            timer.stop();
            nextScene.getRoot().setOpacity(0.0);
            stage.setScene(nextScene);
            FadeTransition mainFade = new FadeTransition(Duration.millis(550), nextScene.getRoot());
            mainFade.setFromValue(0.0);
            mainFade.setToValue(1.0);
            mainFade.play();
        });
        sequence.play();

        stage.setOnHidden(event -> {
            timer.stop();
            sequence.stop();
        });
    }

    private VBox buildOverlayContent() {
        Label eyebrow = new Label("PLANETA TIERRA");
        eyebrow.setTextFill(Color.web("#78a9ff"));
        eyebrow.setFont(Font.font("System", FontWeight.SEMI_BOLD, 15));

        Label title = new Label(SOFTWARE_TITLE);
        title.setTextFill(Color.web("#eef5ff"));
        title.setFont(Font.font(resolveTitleFontFamily(), FontWeight.BOLD, 40));
        title.setWrapText(true);
        title.setMaxWidth(880);
        title.setEffect(new DropShadow(20, Color.web("#4c8dff", 0.35)));

        Label description = new Label(
                "La Tierra, nuestro planeta azul, está rodeada por una fina atmósfera y gobernada por la gravedad. " +
                "Este simulador visualiza lanzamientos, transferencias y movimiento orbital alrededor de ese mundo vivo."
        );
        description.setTextFill(Color.web("#bfd4ff"));
        description.setFont(Font.font("System", 17));
        description.setWrapText(true);
        description.setMaxWidth(720);
        description.setAlignment(Pos.CENTER);

        Label hint = new Label("Inicializando control de misión...");
        hint.setTextFill(Color.web("#7fd9ff"));
        hint.setFont(Font.font("Consolas", FontWeight.NORMAL, 14));
        hint.setEffect(new Glow(0.35));

        VBox content = new VBox(14, eyebrow, title, description, hint);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(900);
        content.setMouseTransparent(true);
        return content;
    }

    private AnimationTimer createRenderer(Canvas canvas) {
        final long[] startedAt = { -1L };

        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (startedAt[0] < 0) {
                    startedAt[0] = now;
                }

                double elapsedSeconds = (now - startedAt[0]) / 1_000_000_000.0;
                drawFrame(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight(), elapsedSeconds);
            }
        };
    }

    private void drawFrame(GraphicsContext gc, double width, double height, double timeSeconds) {
        gc.setFill(Color.web("#050814"));
        gc.fillRect(0, 0, width, height);

        drawStars(gc, timeSeconds);

        double cx = width * 0.5;
        double cy = height * 0.56;
        double earthRadius = Math.max(68.0, Math.min(width, height) * 0.11);

        drawEarthGlow(gc, cx, cy, earthRadius);
        drawOrbitGuide(gc, cx, cy, earthRadius, timeSeconds);
        drawSatelliteAndTrail(gc, cx, cy, earthRadius, timeSeconds);
    }

    private void drawStars(GraphicsContext gc, double timeSeconds) {
        for (int i = 0; i < stars.size(); i++) {
            Star star = stars.get(i);
            double twinkle = 0.65 + 0.35 * Math.sin(timeSeconds * 0.85 + i * 0.37);
            gc.setFill(Color.rgb(255, 255, 255, Math.max(0.15, star.getBrightness() * twinkle)));
            gc.fillOval(star.getX(), star.getY(), star.getSize(), star.getSize());
        }
    }

    private void drawEarthGlow(GraphicsContext gc, double cx, double cy, double radius) {
        RadialGradient atmosphere = new RadialGradient(
                0, 0,
                cx, cy,
                radius * 2.3,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#8fd2ff", 0.16)),
                new Stop(0.45, Color.web("#58a9ff", 0.10)),
                new Stop(0.78, Color.web("#256fe8", 0.05)),
                new Stop(1.00, Color.web("#1a49c8", 0.0))
        );
        gc.setFill(atmosphere);
        gc.fillOval(cx - radius * 2.3, cy - radius * 2.3, radius * 4.6, radius * 4.6);

        RadialGradient earthGradient = new RadialGradient(
                0, 0,
                cx - radius * 0.24, cy - radius * 0.24,
                radius * 1.35,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#70b6ff")),
                new Stop(0.38, Color.web("#236ddf")),
                new Stop(0.78, Color.web("#103a92")),
                new Stop(1.00, Color.web("#04152f"))
        );
        gc.setFill(earthGradient);
        gc.fillOval(cx - radius, cy - radius, radius * 2.0, radius * 2.0);

        gc.setStroke(Color.web("#b7e0ff", 0.3));
        gc.setLineWidth(1.4);
        gc.strokeOval(cx - radius, cy - radius, radius * 2.0, radius * 2.0);
    }

    private void drawOrbitGuide(GraphicsContext gc, double cx, double cy, double earthRadius, double timeSeconds) {
        double orbitRadiusX = earthRadius * 2.15;
        double orbitRadiusY = earthRadius * 1.55;
        gc.setStroke(Color.web("#88c8ff", 0.18 + 0.05 * Math.sin(timeSeconds * 0.8)));
        gc.setLineWidth(1.2);
        gc.strokeOval(cx - orbitRadiusX, cy - orbitRadiusY, orbitRadiusX * 2.0, orbitRadiusY * 2.0);
    }

    private void drawSatelliteAndTrail(GraphicsContext gc, double cx, double cy, double earthRadius, double timeSeconds) {
        double orbitRadiusX = earthRadius * 2.15;
        double orbitRadiusY = earthRadius * 1.55;
        double angle = timeSeconds * 1.12 - Math.PI / 5.0;

        double satX = cx + Math.cos(angle) * orbitRadiusX;
        double satY = cy + Math.sin(angle) * orbitRadiusY;

        trailPoints.addLast(new double[] { satX, satY });
        while (trailPoints.size() > TRAIL_LIMIT) {
            trailPoints.removeFirst();
        }

        double[][] points = trailPoints.toArray(new double[0][]);
        for (int i = 1; i < points.length; i++) {
            double alpha = (double) i / points.length;
            gc.setStroke(Color.web("#a8dcff", 0.05 + alpha * 0.32));
            gc.setLineWidth(0.8 + alpha * 1.4);
            gc.strokeLine(points[i - 1][0], points[i - 1][1], points[i][0], points[i][1]);
        }

        gc.setFill(Color.web("#8ed9ff", 0.22));
        gc.fillOval(satX - 10, satY - 10, 20, 20);

        gc.setFill(Color.web("#f2fbff"));
        gc.fillRoundRect(satX - 4.5, satY - 3.0, 9.0, 6.0, 2.0, 2.0);
        gc.setStroke(Color.web("#a5d9ff"));
        gc.setLineWidth(1.2);
        gc.strokeLine(satX - 9.5, satY, satX - 4.5, satY);
        gc.strokeLine(satX + 4.5, satY, satX + 9.5, satY);
    }

    private void precomputeStars(double width, double height) {
        stars.clear();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star(
                    random.nextDouble() * width,
                    random.nextDouble() * height,
                    0.28 + random.nextDouble() * 0.72,
                    0.7 + random.nextDouble() * 2.4
            ));
        }
    }

    private String resolveTitleFontFamily() {
        List<String> availableFamilies = Font.getFamilies();
        List<String> preferredFamilies = List.of(
                "OCR A Extend",
                "OCR A Extended",
                "OCR A Std",
                "OCR A",
                "Consolas",
                "Monospaced"
        );

        for (String preferred : preferredFamilies) {
            for (String family : availableFamilies) {
                if (family.equalsIgnoreCase(preferred)) {
                    return family;
                }
            }
        }

        for (String family : availableFamilies) {
            String normalized = family.toLowerCase();
            if (normalized.contains("ocr") && normalized.contains("extend")) {
                return family;
            }
        }

        return Font.getDefault().getFamily();
    }
}
