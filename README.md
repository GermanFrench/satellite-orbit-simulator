# Satellite Orbit Simulator

A Java desktop application that simulates a satellite orbiting the Earth in real time using basic orbital mechanics and displays the orbit visually with a JavaFX GUI.

---

## Features

- 🌍 **Animated orbit** – watch the satellite circle Earth on a canvas with a space background
- ⚙️ **Orbital mechanics** – velocity and period calculated from `v = √(GM/r)` and `T = 2π√(r³/GM)`
- 🎚️ **Altitude slider** – change the satellite's altitude (200 – 2 000 km) and see the orbit update instantly
- ⏩ **Speed slider** – adjust the simulation playback speed (0.5 × – 10 × the default 100× time-acceleration)
- 📡 **Telemetry panel** – live display of altitude, orbital velocity and orbital period

---

## Tech Stack

| Concern        | Technology          |
|----------------|---------------------|
| Language       | Java 17             |
| Build tool     | Maven 3.9+          |
| UI framework   | JavaFX 21           |
| Layout format  | FXML                |

> **Note:** The project targets Java 17 (the LTS release available in this environment). JavaFX 21 is fully compatible with Java 17+. Updating `<maven.compiler.source>` / `<maven.compiler.target>` to `21` in `pom.xml` will work on JDK 21 without any other changes.

---

## Project Structure

```
satellite-orbit-simulator/
├── pom.xml
└── src/
    └── main/
        ├── java/com/simulator/
        │   ├── main/
        │   │   ├── MainApp.java          # JavaFX Application entry point
        │   │   └── Launcher.java         # Plain main() bootstrap launcher
        │   ├── model/
        │   │   ├── Earth.java            # Earth physical constants
        │   │   ├── Satellite.java        # Satellite state & position
        │   │   └── TelemetryData.java    # Telemetry snapshot
        │   ├── simulation/
        │   │   ├── OrbitCalculator.java  # v = √(GM/r), T = 2π√(r³/GM)
        │   │   └── SimulationEngine.java # AnimationTimer-based loop
        │   ├── controller/
        │   │   └── SimulationController.java  # FXML controller
        │   └── ui/
        │       └── SimulationView.java   # Canvas renderer
        └── resources/com/simulator/ui/
            └── simulation.fxml           # Main window layout
```

---

## Running the Application

### Prerequisites

- JDK 17 or later
- Maven 3.9 or later (or use the `./mvnw` wrapper if provided)

### Start

```bash
mvn clean javafx:run
```

### Package (optional)

```bash
mvn clean package
```

The shaded / executable JAR (if configured) will be in `target/`.

---

## Orbital Mechanics

The simulation uses two standard equations from Newtonian gravity:

**Orbital velocity**

```
v = √(GM / r)
```

**Orbital period**

```
T = 2π × √(r³ / GM)
```

Where:
- `G` = 6.674 × 10⁻¹¹ m³ kg⁻¹ s⁻²  (gravitational constant)
- `M` = 5.972 × 10²⁴ kg              (mass of Earth)
- `r` = R_Earth + altitude            (orbital radius in metres)

---

## License

This project is provided for educational purposes.

