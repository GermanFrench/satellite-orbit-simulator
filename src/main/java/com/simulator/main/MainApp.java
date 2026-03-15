package com.simulator.main;

import com.simulator.ui.IntroSplashScreen;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Satellite Orbit Simulator.
 *
 * <p>Loads the main FXML layout and displays the primary application window.</p>
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/simulator/ui/simulation.fxml"));
        Scene scene = new Scene(loader.load());

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double targetWidth = Math.min(1600, visualBounds.getWidth() * 0.94);
        double targetHeight = Math.min(900, visualBounds.getHeight() * 0.94);

        primaryStage.setTitle("Satellite Orbit Simulator");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1220);
        primaryStage.setMinHeight(720);
        primaryStage.setWidth(targetWidth);
        primaryStage.setHeight(targetHeight);
        primaryStage.centerOnScreen();

        IntroSplashScreen splashScreen = new IntroSplashScreen();
        splashScreen.play(primaryStage, scene, targetWidth, targetHeight);
    }

    /**
     * Application entry point.  Delegates to {@link Launcher} in normal
     * execution; kept here so IDE run configurations targeting MainApp work.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
