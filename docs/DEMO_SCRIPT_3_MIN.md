# Demo Script — Satellite Orbit Simulator (3 minutos)

Objetivo: presentar la app de forma clara, visual e impactante en una demo corta.

---

## 0:00 - 0:20 | Apertura

**Narrativa sugerida**

> Satellite Orbit Simulator es una aplicacion de escritorio hecha en Java y JavaFX para visualizar mecanica orbital de forma interactiva. Permite simular orbitas reales, lanzamientos y transferencias orbitales en una interfaz educativa y visual.

**Que mostrar**
- splash de inicio,
- entrada a la pantalla principal,
- vista general de la Tierra, glow atmosferico y estrellas.

---

## 0:20 - 1:00 | Orbita base

**Acciones**
1. Seleccionar un satelite.
2. Elegir `LEO` en `Orbit Type`.
3. Pulsar `Start Simulation`.
4. Señalar el trail orbital.

**Narrativa sugerida**

> Aqui vemos un satelite en orbita baja terrestre. La simulacion usa gravedad newtoniana e integracion numerica para actualizar la trayectoria en tiempo real. El rastro orbital permite ver claramente la evolucion de la orbita.

---

## 1:00 - 1:40 | Multiples satelites

**Acciones**
1. Pulsar `Add Satellite`.
2. Seleccionar el nuevo satelite.
3. Cambiarlo a `GEO` o `MEO`.
4. Mostrar que ambos quedan activos.

**Narrativa sugerida**

> El sistema soporta multiples satelites simultaneamente. Cada uno tiene su propia masa, velocidad, altitud e inclinacion, junto con telemetria individual en tiempo real.

---

## 1:40 - 2:15 | Lanzamiento

**Acciones**
1. Seleccionar un satelite.
2. Pulsar `Launch Satellite`.
3. Mostrar telemetria de lanzamiento.
4. Si quieres remarcar robustez, usar `Cancel Launch`.

**Narrativa sugerida**

> Ademas de las orbitas, la app incluye una simulacion de lanzamiento. El cohete despega desde la superficie terrestre, gana altitud con thrust y luego despliega el satelite cuando alcanza condiciones orbitales.

---

## 2:15 - 2:45 | Transferencia Hohmann

**Acciones**
1. Seleccionar un satelite en `LEO`.
2. Configurar transferencia `LEO -> GEO`.
3. Pulsar `Execute Transfer`.
4. Mostrar elipse de transferencia y `delta-v`.

**Narrativa sugerida**

> Tambien se pueden visualizar maniobras orbitales reales, como la transferencia de Hohmann. La aplicacion calcula los dos impulsos necesarios y muestra la orbita de transferencia, el tiempo de vuelo y el costo en delta-v.

---

## 2:45 - 3:00 | Cierre

**Narrativa sugerida**

> El objetivo de este proyecto es unir ingenieria, simulacion cientifica y visualizacion interactiva en una herramienta compacta y presentable, ideal para aprendizaje, portfolio tecnico y demostraciones publicas.

---

## Tips para una demo limpia

- Inicia con un satelite ya visible.
- Mantén preparada una trayectoria estable en `LEO`.
- No cambies demasiados sliders en vivo si el tiempo es corto.
- Si haces lanzamiento, decide antes si vas a mostrar despliegue o cancelacion.
- Ten abierto el panel de telemetria para reforzar el valor tecnico.

---

## Setup recomendado antes de presentar

- Resolucion: 1920x1080 si es posible.
- App abierta y centrada.
- Paneles visibles.
- Un satelite en `LEO`, otro en `GEO`.
- Instalador y README listos por si te preguntan por distribucion.

