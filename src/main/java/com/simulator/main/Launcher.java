package com.simulator.main;

/**
 * Bootstrap launcher that does NOT extend {@code javafx.application.Application}.
 *
 * <p>This class exists because some JVM / module-path configurations refuse to
 * run a class that extends {@code Application} unless the JavaFX modules are
 * on the module-path.  Delegating to {@link MainApp} from a plain main class
 * avoids that constraint when using the Maven javafx-maven-plugin.</p>
 */
public class Launcher {

    /**
     * Application entry point – delegates to {@link MainApp#main(String[])}.
     *
     * @param args command-line arguments (passed through to JavaFX)
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
