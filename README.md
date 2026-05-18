# 📡 Techno Radar — Sprint 1 & Sprint 2 (Publisher/Subscriber)

**Techno Radar** es un proyecto desarrollado en **Java 21** (Maven multimódulo) para la captura periódica de datos desde fuentes externas y su incorporación a una arquitectura orientada a eventos mediante el patrón **Publisher/Subscriber**.

- En **Sprint 1**, cada *feeder* capturaba datos y los persistía localmente de forma incremental.
- En **Sprint 2**, los *feeders* publican eventos en un **broker (ActiveMQ)** y un módulo **Event Store Builder** los consume mediante **suscripciones durables** para construir un **event store** persistente y ordenado temporalmente.

---

## 👥 Integrantes del equipo

- **Lucas Rodríguez Hernández**  
- **Javier Bolívar García-Izquierdo** (GitHub: @Javi05x)

**Institución:** Universidad de Las Palmas de Gran Canaria (EUII)  
**Asignatura:** Desarrollo de Aplicaciones para Ciencia de Datos

---

## 💡 Propuesta de valor (Unidad de negocio)

El sistema permite **monitorizar señales tecnológicas** a partir de múltiples fuentes (p. ej. repositorios/proyectos y preguntas técnicas), creando un histórico de eventos:
- **Reproducible**: los eventos quedan almacenados y pueden re-procesarse.
- **Trazable**: cada evento incorpora `ts` (UTC) y `ss` (source system).
- **Escalable por módulos**: es sencillo añadir nuevos feeders y nuevos consumidores.

Esto habilita futuros casos de uso (Sprint 3) como agregaciones, dashboards, métricas y un **datamart** alimentado desde el event store.

---

## 🧱 Estructura del repositorio

Proyecto Maven multimódulo:

- `github-feeder/` → Feeder (publisher) de datos de GitHub
- `stackexchange-feeder/` → Feeder (publisher) de datos de StackExchange
- `event-store-builder/` → Subscriber que consume mensajes de ActiveMQ y construye el event store
- `common/` → Código compartido (utilidades, dependencias comunes, integración JMS/JSON)

El `pom.xml` raíz declara estos módulos y fija **Java 21**.

---

## 🏗️ Arquitectura (Sprint 2)

### Arquitectura de sistema (Publisher/Subscriber)

1. **Feeders (Publishers)**:
   - Capturan datos con periodicidad definida.
   - Transforman la captura a un **evento JSON** con estructura mínima:
     - `ts`: timestamp en UTC
     - `ss`: identificador del feeder (fuente)
     - `payload`: atributos específicos de la fuente
   - Publican el evento en un **topic** de ActiveMQ (ej. `GitHub`, `StackExchange`, etc.).

2. **Broker (ActiveMQ)**:
   - Servicio externo (no es un módulo Java del repo).
   - Ejecutándose típicamente en `tcp://localhost:61616`.

3. **Event Store Builder (Subscriber)**:
   - Se suscribe **de forma durable** a los topics.
   - Consume eventos y los almacena en un **event store** con estructura temporal:

```
eventstore/{topic}/{ss}/{YYYYMMDD}.events
```

- Un archivo `.events` contiene eventos de un solo día.
- Formato de escritura: **NDJSON (JSON Lines)**, un evento por línea, en modo *append*.

---

## ✅ Justificación de APIs y (pre)estructura del datamart

### APIs / Fuentes externas
- Se utilizan fuentes externas (GitHub y StackExchange) por su relevancia para **tendencias tecnológicas**:
  - actividad de repositorios / metadatos
  - preguntas, tags, volumen de actividad técnica, etc.

### Event Store como base del datamart (Sprint 3)
En Sprint 2 aún no se implementa el datamart final, pero se deja preparada la arquitectura:
- El **event store** actúa como fuente de verdad (histórico de eventos).
- En Sprint 3, un módulo `business-unit` podrá:
  - leer eventos por rango temporal / por topic
  - transformarlos a tablas agregadas (datamart)
  - exponer consultas o endpoints REST

---

## 🛠️ Requisitos técnicos

- **Java:** 21
- **Build:** Maven (multimódulo)
- **IDE recomendada:** IntelliJ IDEA
- **Broker:** ActiveMQ (servicio local)
- **Serialización JSON:** Gson/Jackson (según módulo)
- **Mensajería:** JMS (ActiveMQ Client)

Dependencia de mensajería usada: `org.apache.activemq:activemq-client:5.15.12`.

---

## ▶️ Compilación

Desde la raíz del repositorio:

```bash
mvn clean package
```

---

## ⚙️ Ejecución (Sprint 2)

### 1) Arrancar ActiveMQ
Requisito: tener ActiveMQ instalado y corriendo.  
Comprobar que está disponible en el puerto **61616**.

> Nota: en la defensa es útil enseñar también la consola web de ActiveMQ (si la tenéis habilitada) para ver colas/topics y mensajes en tránsito.

### 2) Ejecutar Event Store Builder (Subscriber)
Desde IntelliJ, ejecutar el `Main` del módulo:

- `event-store-builder/src/main/java/es/ulpgc/dacd/Main.java`

Este módulo:
- se suscribe a los topics de forma durable
- consume eventos
- escribe en `eventstore/` siguiendo el esquema definido

### 3) Ejecutar los Feeders (Publishers)
Ejecutar cada feeder (desde IntelliJ) para que publique eventos:

- `github-feeder/`
- `stackexchange-feeder/`

Cada ejecución periódica (o iteración) publica un evento JSON en su topic correspondiente.

---

## 📌 Ejemplos de uso / evidencias

### Estructura esperada de salida (event store)
Tras ejecutar feeders + event-store-builder, se generarán rutas como:

- `eventstore/<topic>/<ss>/20260518.events`

Ejemplo (conceptual) de contenido NDJSON:
```json
{"ts":"2026-05-18T10:15:00Z","ss":"github-feeder", ...}
{"ts":"2026-05-18T10:20:00Z","ss":"stackexchange-feeder", ...}
```

---

## 🧼 Principios y patrones aplicados

- **Publisher/Subscriber** (Sprint 2): feeders publican, event-store-builder consume.
- **Single Responsibility**: módulos separados por responsabilidad (captura vs almacenamiento).
- **Separación por capas**: captura → transformación → publicación/consumo → persistencia.
- **Persistencia incremental**: no se sobrescriben datos; se añaden eventos y se conserva histórico.
- **Clean Code**:
  - nombres significativos
  - modularidad
  - logging para trazabilidad

---

## 📅 Estado del proyecto (hasta Sprint 2)

- [x] Proyecto multimódulo Maven (Java 21)
- [x] Feeders implementados (GitHub y StackExchange)
- [x] Publicación de eventos a ActiveMQ (topics)
- [x] Event Store Builder (suscriptor durable)
- [x] Persistencia de eventos en `eventstore/{topic}/{ss}/{YYYYMMDD}.events` en formato NDJSON

---

## 🧩 Próximo sprint (Sprint 3 — previsto)
En Sprint 3 se completará la entrega final incorporando:
- `business-unit/` (capa de explotación)
- datamart (modelo y justificación final)
- documentación ampliada (diagramas de arquitectura + clases)
- ejemplos de consultas / endpoints REST y demo funcional

---
