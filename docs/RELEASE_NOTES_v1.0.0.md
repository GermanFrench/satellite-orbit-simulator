# Satellite Orbit Simulator — Release Notes v1.0.0

Fecha de release: 2026-03-16
Version: `1.0.0`
Plataforma objetivo: Windows
Artefacto principal: `installer/dist/SatelliteOrbitSimulator.exe`

---

## Resumen

`Satellite Orbit Simulator` es una aplicacion de escritorio desarrollada en Java 17 + JavaFX orientada a visualizacion educativa de mecanica orbital.

Esta version publica incorpora simulacion orbital multiescenario, telemetria en tiempo real, lanzamiento de satelites, transferencias Hohmann y una presentacion visual mejorada para demos, portfolio y uso educativo.

---

## Novedades principales

### Simulacion orbital mas realista
- gravedad basada en la ley de gravitación universal de Newton,
- integracion numerica semi-implicita de velocidad/posicion,
- soporte para orbitas circulares y elipticas,
- velocidad orbital configurable por satelite.

### Regimenes orbitales reales
- `LEO` (Low Earth Orbit),
- `MEO` (Medium Earth Orbit),
- `GEO` (Geostationary Orbit).

Cada regimen expone altitud, velocidad tipica y periodo orbital de referencia.

### Multi-satelite
- alta dinamica de satelites,
- lista de seleccion en UI,
- telemetria individual por satelite,
- render con colores y trails independientes.

### Sistema de lanzamiento
- simulacion de cohete desde superficie terrestre,
- thrust, gravedad, velocidad y altitud,
- desacople del satelite al alcanzar condiciones orbitales,
- cancelacion de lanzamiento con restauracion visual del estado orbital.

### Transferencia Hohmann
- seleccion de orbita actual y objetivo,
- calculo de `delta-v1`, `delta-v2` y tiempo de transferencia,
- orbita de transferencia eliptica,
- segunda maniobra de circularizacion.

### Mejoras visuales
- glow atmosferico de la Tierra,
- fondo estelar reutilizable,
- trails orbitales con desvanecimiento,
- visualizacion de cohete y estela de escape,
- splash de introduccion con identidad del software.

---

## Mejoras de estabilidad y distribucion

- build Maven verificado,
- tests automatizados minimos para fisica base,
- empaquetado con `jpackage`,
- runtime Java embebido,
- instalador Windows `.exe` listo para doble clic,
- script de build reproducible en `installer/build-win.ps1`.

---

## Archivos de entrega recomendados

Para una publicacion publica o GitHub Release:

- `SatelliteOrbitSimulator.exe`
- `RELEASE_NOTES_v1.0.0.md`
- hash SHA-256 del instalador
- 3 a 5 capturas de pantalla
- breve video/gif de demostracion

Hash validado del instalador estable:

```text
E973517C6541EEFC491AE20EF70FE9B9E34B80F315F950DF6AC3A8EDC7491A6A
```

---

## Requisitos de uso

El instalador distribuido para Windows ya incluye runtime Java. El usuario final no necesita instalar Java manualmente.

---

## Problemas conocidos

- El analisis de dependencias reporta una advertencia informativa sobre `javafx-graphics:21`; conviene revisar actualizacion de stack en una version posterior.
- Para generar el `.exe` nuevamente se requiere WiX Toolset o los binarios locales configurados en `installer/tools/wix`.

---

## Recomendacion de publicacion

Estado sugerido: **Public Beta / Release 1.0.0 presentable**

Apta para:
- portfolio tecnico,
- demos publicas,
- presentaciones educativas,
- pruebas con usuarios tempranos.

Antes de una adopcion masiva sostenida, se recomienda sumar mas pruebas automatizadas y telemetria de errores de produccion.

