# 📡 Techno Radar - Sprint 1

**Techno Radar** es una aplicación desarrollada en Java para la captura, procesamiento y persistencia de datos dinámicos provenientes de fuentes externas. Este proyecto forma parte de la asignatura *Desarrollo de Aplicaciones para Ciencia de Datos* de la ULPGC.

---

## 🎯 Objetivo del Sprint 1
El objetivo principal es desarrollar un proyecto multimódulo que consuma datos de dos fuentes externas de forma independiente y los almacene en una base de datos local.

> [!IMPORTANT]
> **Persistencia Incremental**: Los datos se guardan de forma acumulativa sin borrar ni sobrescribir capturas anteriores. Cada registro incluye una marca temporal (`captured_at`) para permitir el análisis de su evolución en el tiempo.

---

## 🏗️ Arquitectura y Diseño
El sistema sigue una estructura multimódulo en **Java 21**. Cada módulo de captura (feeder) gestiona su propia lógica de obtención y persistencia:

* **Módulo de Consumo**: Implementa clientes para APIs o técnicas de Scraping.
* **Módulo de Transformación**: Normaliza los datos crudos (JSON/HTML) a un modelo interno de la aplicación.
* **Módulo de Persistencia**: Gestiona la inserción incremental en tablas de **SQLite**.

---

## 🛠️ Requisitos Técnicos
* **Lenguaje:** Java 21.
* **IDE:** IntelliJ IDEA.
* **Base de Datos:** SQLite.

> [!NOTE]
> La captura de datos se realiza de forma periódica mediante el uso de `ScheduledExecutorService`, respetando los límites de acceso de las fuentes externas.

---

## 🧼 Estándares de Código (Clean Code)
Este proyecto se adhiere a la guía de estilo de la asignatura y a los principios de *Agile Software Craftsmanship*:
* **Identificadores:** Todo el código está escrito en inglés con nombres significativos.
* **CamelCase:** Uso de `PascalCase` para clases y `camelCase` para variables y métodos.
* **Principios SOLID:** Se fomenta la modularidad y la responsabilidad única en los métodos (menos de 10 líneas por método).
* **D.R.Y:** Evitamos la duplicación de código mediante una estructura limpia y reutilizable.

---

## 👥 Autores
* **[Lucas Rodríguez Hernández](https://github.com/Luucaaass)**
* **[Javier Bolívar García-Izquierdo](https://github.com/Javi05x)** 
* **Institución:** [Universidad de Las Palmas de Gran Canaria.](https://www10.ulpgc.es/)