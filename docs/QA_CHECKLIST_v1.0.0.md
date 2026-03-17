# QA Checklist — Satellite Orbit Simulator v1.0.0

Objetivo: validar rapidamente que la app esta lista para exhibicion publica y uso inicial en Windows.

Resultado esperado final: todos los items criticos en verde.

---

## 1. Instalacion

- [ ] El archivo `SatelliteOrbitSimulator.exe` existe.
- [ ] El instalador inicia con doble clic.
- [ ] El icono de la aplicacion se muestra correctamente.
- [ ] La instalacion finaliza sin errores.
- [ ] Se crea acceso directo si fue seleccionado.
- [ ] La app abre correctamente tras instalarse.

---

## 2. Arranque y presentacion

- [ ] Aparece la pantalla/splash de introduccion.
- [ ] El titulo `Satellite Orbit Simulator` se ve correctamente.
- [ ] La ventana principal abre centrada.
- [ ] La UI no queda escondida por la barra de tareas.
- [ ] Los paneles laterales pueden recorrerse sin perder controles.

---

## 3. Simulacion base

- [ ] `Add Satellite` agrega un nuevo satelite a la lista.
- [ ] Se puede seleccionar un satelite distinto.
- [ ] `Start Simulation` anima la orbita.
- [ ] `Pause Simulation` detiene el movimiento.
- [ ] `Reset Simulation` reinicia la escena sin errores.
- [ ] El satelite deja trail orbital visible.
- [ ] El fondo estelar y el glow terrestre se renderizan correctamente.

---

## 4. Controles orbitales

- [ ] Cambiar `Orbit Type` a `LEO` ajusta altitud/velocidad.
- [ ] Cambiar `Orbit Type` a `MEO` ajusta altitud/velocidad.
- [ ] Cambiar `Orbit Type` a `GEO` ajusta altitud/velocidad.
- [ ] Cambiar sliders de altitud y velocidad actualiza la simulacion.
- [ ] La telemetria refleja altitud, velocidad y periodo.
- [ ] La energia orbital se muestra sin valores corruptos.

---

## 5. Lanzamiento

- [ ] `Launch Satellite` inicia la animacion del cohete.
- [ ] La telemetria de lanzamiento muestra altitud, velocidad y thrust.
- [ ] El cohete se dibuja correctamente.
- [ ] La estela del cohete se dibuja correctamente.
- [ ] `Cancel Launch` detiene el lanzamiento.
- [ ] Tras cancelar, la animacion se restaura de forma coherente.
- [ ] Tras cancelar, no quedan restos visuales inconsistentes.
- [ ] Un nuevo lanzamiento puede iniciarse luego de cancelar.

---

## 6. Transferencia Hohmann

- [ ] Puede elegirse orbita actual y objetivo.
- [ ] `Execute Transfer` inicia la maniobra.
- [ ] Se visualiza la elipse de transferencia.
- [ ] Se muestran `delta-v1`, `delta-v2` y tiempo.
- [ ] La fase de transferencia cambia en telemetria.
- [ ] `Cancel Transfer` aborta sin bloquear la UI.

---

## 7. Robustez visual y UX

- [ ] No se solapan labels importantes.
- [ ] No aparecen controles inaccesibles al fondo del panel.
- [ ] Los botones se habilitan/deshabilitan segun el estado real.
- [ ] La app no parpadea ni consume memoria de forma evidente en 3-5 minutos.
- [ ] El trail no crece sin limite visible.

---

## 8. Cierre y reinstalacion

- [ ] La app puede cerrarse sin colgarse.
- [ ] Puede abrirse nuevamente sin problemas.
- [ ] La desinstalacion funciona.
- [ ] Una reinstalacion posterior funciona.

---

## 9. Go / No-Go

### Go
- Todos los bloques 1 a 6 completados.
- Sin errores bloqueantes.
- Sin fallos visuales graves en demo.

### No-Go
- La app no abre.
- El lanzamiento deja la simulacion rota.
- La UI queda parcialmente inaccesible.
- La transferencia rompe la escena o la telemetria.

---

## 10. Evidencia recomendada

Guardar al menos:
- 3 capturas de pantalla,
- 1 captura del instalador,
- 1 captura de telemetria de transferencia,
- hash SHA-256 del `.exe`,
- breve nota con SO y resolucion usada en la validacion.

