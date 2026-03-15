package com.simulator.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Satellite Orbit Simulator.
 *
 * <p>Loads the main FXML layout and displays the primary application window.</p>
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML layout from the resources directory
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/simulator/ui/simulation.fxml"));

        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("Satellite Orbit Simulator");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
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
