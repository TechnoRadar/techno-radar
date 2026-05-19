🚀 Techno Radar
Plataforma Avanzada de Análisis de Tendencias Tecnológicas
Java Maven Javalin SQLite

Empoderando a desarrolladores e innovadores con insights impulsados por análisis integral de repositorios GitHub e integración de datos de Stack Exchange en tiempo real.

📋 Tabla de Contenidos
- 📜 Descripción
- ✨ Características
- 🔌 APIs Utilizadas
- 🏗️ Arquitectura
- 💾 Estructura del Datamart
- 📐 Diagramas de Clases
- 🔧 Requisitos
- 🚀 Instalación y Uso
- 👥 Autores
- 📄 Licencia

📜 Descripción
Techno Radar es una plataforma de análisis de tendencias tecnológicas que estudia distintos parámetros de las tecnologías más populares basándose en su actividad en GitHub y en Stack Exchange. El objetivo es proporcionar a desarrolladores, innovadores y empresas información detallada y relevante que les permita identificar qué tecnologías están en auge, cuáles están consolidadas y cuáles están en declive.

Además de los datos de GitHub (estrellas, actividad), el sistema utiliza información proveniente de Stack Exchange (cantidad de preguntas), permitiendo obtener datos de calidad de cada tecnología. El resultado es un datamart consolidado y actualizado que permite visualizar tendencias en tiempo real a través de un dashboard interactivo.

✨ Características
Techno Radar ofrece un conjunto completo de características diseñadas para proporcionar análisis profundos y precisos de tendencias tecnológicas:

- 🔍 Recolección de Datos en Tiempo Real: Extracción automatizada de datos desde las APIs de GitHub y Stack Exchange
- 📊 Análisis Combinado: Integración de métricas de GitHub (estrellas) y Stack Exchange (preguntas)
- 🎯 Score Combinado: Algoritmo que pondera ambas fuentes para obtener una puntuación 0-100
- 📈 Análisis de Tendencias: Detección automática de tecnologías en crecimiento, decrecimiento o estables
- ❤️ Índice de Salud Tecnológica: Métrica que evalúa la salud general de cada tecnología
- 🖥️ Dashboard Interactivo: Visualización en tiempo real con 5 gráficos diferentes
- 🔄 API REST: Endpoints para consumir datos de tendencias
- ⏱️ Actualizaciones Periódicas cada 10 segundos para mantener datos recientes
- 📋 Pruebas Exhaustivas: Arquitectura modular y bien testeable

🔌 APIs Utilizadas
Hemos escogido la API de GitHub y la de Stack Exchange por su relevancia en la comunidad tecnológica global, siendo fuentes confiables de actividad y popularidad de tecnologías.

- 📊 GitHub API: Nos permite analizar la popularidad de proyectos relacionados con cada tecnología, medida en estrellas
- 💬 Stack Exchange API: Proporciona datos sobre la cantidad de preguntas realizadas, indicando el nivel de soporte comunitario y problemas enfrentados

La combinación de ambas fuentes permite correlacionar la popularidad con la actividad comunitaria, proporcionando una visión holística de cada tecnología.

🏗️ Arquitectura

```mermaid
graph LR
    A["GitHub API"] -->|Extrae estrellas| B["GitHub Feeder"]
    C["Stack Exchange API"] -->|Extrae preguntas| D["Stack Exchange Feeder"]
    B -->|Publica eventos| E["ActiveMQ"]
    D -->|Publica eventos| E
    E -->|Consume eventos| F["Event Store Builder"]
    F -->|Almacena eventos| G["Event Store"]
    G -->|Lee eventos| H["Business Unit"]
    H -->|Procesa datos| I["SQLite Datamart"]
    I -->|Consulta datos| J["REST API"]
    J -->|Visualiza| K["Dashboard"]
