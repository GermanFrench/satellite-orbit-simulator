# Satellite Orbit Simulator

Aplicacion de escritorio en JavaFX para simulacion orbital educativa y de ingenieria, con soporte para:
- dinamica multi-satelite,
- lanzamiento de cohetes,
- transferencias orbitales Hohmann,
- telemetria en tiempo real,
- y visualizacion avanzada (starfield, trails, glow atmosferico y orbitas de transferencia).

Este proyecto esta pensado para aprendizaje, demostraciones tecnicas y portfolio profesional.

## Descarga rapida

- Windows installer: `installer/dist/SatelliteOrbitSimulator.exe`
- Version actual: `v1.0.0`
- Runtime Java incluido: **no necesitas instalar Java manualmente**
- SHA-256: `E973517C6541EEFC491AE20EF70FE9B9E34B80F315F950DF6AC3A8EDC7491A6A`

### Resumen ejecutivo

`Satellite Orbit Simulator` es un simulador educativo de mecanica orbital desarrollado en `Java 17 + JavaFX`, orientado a visualizacion interactiva, portfolio tecnico y demostraciones publicas. Incluye orbitas LEO/MEO/GEO, trails orbitales, lanzamiento de satelites, transferencia Hohmann y telemetria en tiempo real.

---

## 1. Objetivos del proyecto

- Explicar mecanica orbital con visualizacion interactiva.
- Implementar un motor de simulacion con fisica Newtoniana.
- Mostrar practicas de arquitectura limpia en Java desktop (`model`, `physics`, `simulation`, `ui`).
- Permitir escenarios de mision reales: inyeccion, transferencia y circularizacion.

---

## 2. Stack tecnologico

| Area | Tecnologia |
|---|---|
| Lenguaje | Java 17 |
| UI | JavaFX (FXML + Canvas) |
| Build | Maven |
| Arquitectura | MVC por capas |

Notas:
- JavaFX 21 es compatible con Java 17 en este proyecto.
- El layout principal esta en `src/main/resources/com/simulator/ui/simulation.fxml`.

---

## 3. Arquitectura tecnica

```text
src/main/java/com/simulator/
  main/
    MainApp.java
    Launcher.java

  model/
    Earth.java
    Orbit.java
    Satellite.java
    TelemetryData.java
    Rocket.java
    LaunchSite.java
    LaunchTelemetry.java
    TransferTelemetry.java

  physics/
    Vector2D.java
    PhysicsEngine.java
    OrbitIntegrator.java
    OrbitalMechanics.java
    RocketPhysics.java
    ThrustModel.java
    HohmannTransferCalculator.java
    OrbitalTransfer.java

  simulation/
    SatelliteManager.java
    SimulationEngine.java
    LaunchSimulationEngine.java
    TransferSimulationController.java

  controller/
    SimulationController.java

  ui/
    OrbitCanvasRenderer.java
    TelemetryPanel.java
    LaunchController.java
    Star.java

src/main/resources/com/simulator/ui/
  simulation.fxml
```

### Responsabilidades

- `model`: entidades y snapshots de telemetria.
- `physics`: ecuaciones y calculos puros, sin UI.
- `simulation`: evolucion temporal de estados y eventos de mision.
- `controller/ui`: interaccion de usuario, render y sincronizacion visual.

---

## 4. Fundamentos fisicos y matematicos

### 4.1 Gravitacion universal

```text
F = G * M * m / r^2
a = F / m
```

Donde:
- `G = 6.674e-11 m^3/(kg*s^2)`
- `M` = masa de la Tierra
- `m` = masa del satelite/cohete
- `r` = distancia al centro terrestre

### 4.2 Integracion numerica (Semi-Implicit Euler)

```text
v(t+dt) = v(t) + a(t)*dt
x(t+dt) = x(t) + v(t+dt)*dt
```

Este integrador ofrece buena estabilidad para simulacion en tiempo real.

### 4.3 Energia orbital especifica

```text
epsilon = v^2/2 - mu/r
mu = G*M
```

- `epsilon < 0` -> orbita cerrada.
- `epsilon >= 0` -> trayectoria abierta.

### 4.4 Velocidad circular

```text
v_circ = sqrt(mu/r)
```

### 4.5 Transferencia Hohmann (dos impulsos)

Para radios circulares `r1` y `r2`:

```text
a_t = (r1 + r2)/2
v1 = sqrt(mu/r1)
v2 = sqrt(mu/r2)
v_p = sqrt(mu*(2/r1 - 1/a_t))
v_a = sqrt(mu*(2/r2 - 1/a_t))
delta-v1 = v_p - v1
delta-v2 = v2 - v_a
t_transfer = pi * sqrt(a_t^3 / mu)
```

---

## 5. Regimenes orbitales implementados

`OrbitType` incluye parametros de referencia:

- `LEO`: ~400 km, ~7.80 km/s, ~90 min.
- `MEO`: ~20,200 km, ~3.87 km/s, ~11.97 h.
- `GEO`: 35,786 km, ~3.07 km/s, 24 h.
- `CUSTOM`: editable por controles.

Al seleccionar `LEO/MEO/GEO`, la UI ajusta automaticamente altitud y velocidad inicial tipica.

---

## 6. Sistema de lanzamiento

### Flujo

1. `LaunchSimulationEngine` crea un cohete en superficie terrestre.
2. Integra ascenso con thrust + gravedad + drag simplificado.
3. Reporta `LaunchTelemetry` (altitud, velocidad, thrust).
4. Al alcanzar umbral de despliegue, desacopla satelite.

### Controles relacionados

- `Launch Satellite`
- `Cancel Launch` (aborta solo el lanzamiento)
- `Reset Simulation` (reinicio general)

---

## 7. Transferencias Hohmann

### Flujo operativo

1. Seleccionar orbita actual y objetivo (`LEO/MEO/GEO`).
2. `Execute Transfer` aplica burn #1.
3. Fase de coast en orbita eliptica.
4. Deteccion de apoapsis (cruce de velocidad radial + fallback temporal).
5. Burn #2 y circularizacion en orbita objetivo.

### Telemetria de transferencia

- Orbita inicial
- Orbita objetivo
- `delta-v1`
- `delta-v2`
- tiempo de transferencia
- fase de la maniobra

### Robustez

- `Cancel Transfer` para abortar maniobra activa.
- bloqueo de transferencias si el satelite esta en fase de lanzamiento.

---

## 8. Visualizacion

- Starfield precomputado (`Star`) reutilizado por frame.
- Tierra con gradientes y halo atmosferico.
- Trails por satelite (limitados a 300 puntos para controlar memoria).
- Elipse de transferencia + marcadores de burns.
- Cohete y estela durante lanzamientos.

---

## 9. UX y comportamiento de la UI

- Ventana redimensionable con tamano inicial adaptado a pantalla.
- Paneles laterales con `ScrollPane` para evitar contenido oculto.
- Botones con habilitacion por estado real:
  - `Launch / Cancel Launch`
  - `Execute Transfer / Cancel Transfer`
- Etiqueta de estado de acciones para explicar por que algo esta bloqueado.

---

## 10. Como ejecutar

### Requisitos

- JDK 17+
- Maven 3.9+

### Ejecutar la aplicacion

```powershell
Set-Location "C:\Users\germa\OneDrive\Desktop\PROYECTOS\satellite-orbit-simulator"
mvn clean javafx:run
```

### Compilar y empaquetar

```powershell
Set-Location "C:\Users\germa\OneDrive\Desktop\PROYECTOS\satellite-orbit-simulator"
mvn clean package
```

---

## 11. Guia de uso rapido

1. Click en `Add Satellite`.
2. Selecciona satelite en la lista.
3. Ajusta parametros orbitales (`Orbit Type`, altitud, velocidad, inclinacion, masa).
4. Ejecuta `Start Simulation`.
5. Para lanzamiento: `Launch Satellite` y, si hace falta, `Cancel Launch`.
6. Para transferencia:
   - configura orbita actual y objetivo,
   - usa `Execute Transfer`,
   - revisa delta-v, tiempo y fase,
   - aborta con `Cancel Transfer` si es necesario.

---

## 12. Guia de implementacion y extension

### Donde agregar cambios

- Nuevas ecuaciones: `physics/`.
- Nuevos flujos de mision: `simulation/`.
- Nuevas vistas/controles: `simulation.fxml` + `SimulationController`.
- Nuevo render: `OrbitCanvasRenderer`.

### Buenas practicas del proyecto

- Usar SI internamente (m, s, kg).
- Convertir a km/km-s solo para UI.
- Mantener la fisica fuera de `controller`.
- Limitar historiales (trails/telemetria) para rendimiento.
- Validar build despues de cambios grandes.

### Extensiones sugeridas

- Perturbaciones avanzadas (`J2`, drag realista por capas).
- Integradores avanzados (RK4, Verlet adaptativo).
- Plane change y transferencias bi-elipticas.
- Export de telemetria a CSV/JSON.
- Tests de regresion fisica con JUnit 5.

---

## 13. Validacion recomendada

```powershell
Set-Location "C:\Users\germa\OneDrive\Desktop\PROYECTOS\satellite-orbit-simulator"
mvn -q -DskipTests package
```

---

## 14. Licencia

Proyecto orientado a fines educativos y portfolio tecnico.

---

## 15. Preparacion para lanzamiento publico (go-live)

Para considerar la app lista para publico masivo, usa este criterio:

- Sin errores de compilacion (`mvn clean test package`).
- Flujos criticos validados manualmente:
  - agregar satelite,
  - iniciar/pausar/reset,
  - lanzamiento y cancelacion,
  - transferencia Hohmann y cancelacion.
- Instalador `.exe` generado y probado en una PC limpia.
- Documentacion de uso y troubleshooting disponible.

### Checklist de release

- [ ] Version actualizada en `pom.xml`.
- [ ] `mvn clean test` en verde.
- [ ] `mvn clean package -Ppackage-win` en verde.
- [ ] `installer/build-win.ps1` genera `.exe`.
- [ ] `installer/smoke-test.ps1` genera hash SHA-256.
- [ ] Prueba de instalacion/desinstalacion completada.
- [ ] Telemetria visible y sin labels rotos en resoluciones comunes.

---

## 16. Empaquetado Windows (.exe) con runtime embebido

Se incluye automatizacion en `installer/` para empaquetar con `jpackage`.

### Build automatizado

```powershell
Set-Location "C:\Users\germa\OneDrive\Desktop\PROYECTOS\satellite-orbit-simulator"
.\installer\build-win.ps1 -Version "1.0.0"
```

### Validacion rapida del artefacto

```powershell
Set-Location "C:\Users\germa\OneDrive\Desktop\PROYECTOS\satellite-orbit-simulator"
.\installer\smoke-test.ps1
```

### Resultado

- Instalador en `installer/dist/`.
- Runtime Java incluido (usuarios finales no necesitan instalar Java).
- Icono de app desde `src/main/resources/icons/app-icon.ico`.

---

## 17. Operacion y soporte tecnico

### Problemas comunes

1. **No aparece jpackage**
   - Verifica JDK 17+ y `JAVA_HOME`.

2. **La UI queda fuera de pantalla**
   - La app usa `Screen.getPrimary().getVisualBounds()` para respetar barra de tareas.
   - Si usas varios monitores con escalado distinto, valida en cada escenario.

3. **Cancel launch deja estado visual inconsistente**
   - Al cancelar, el satelite vuelve a su estado orbital configurado y se limpia su trail.

### Recomendacion de distribucion

- Publicar cada release con:
  - `SatelliteOrbitSimulator*.exe`,
  - hash SHA-256,
  - notas de version,
  - capturas de pantalla de features clave.

---

## 18. Kit de lanzamiento publico

Documentacion lista para publicar y presentar la app:

- `docs/RELEASE_NOTES_v1.0.0.md`
- `docs/QA_CHECKLIST_v1.0.0.md`
- `docs/DEMO_SCRIPT_3_MIN.md`
- `docs/GITHUB_RELEASE_TEMPLATE_v1.0.0.md`
- `docs/PUBLICATION_COPY.md`

Artefacto de Windows generado y validado:

- `installer/dist/SatelliteOrbitSimulator.exe`
- SHA-256: `E973517C6541EEFC491AE20EF70FE9B9E34B80F315F950DF6AC3A8EDC7491A6A`

