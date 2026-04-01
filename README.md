# TechnoRadar – Sprint 1 (Memoria corta)

## 🎯 Objetivo del Sprint
Desarrollar un proyecto **multimódulo en Java** que consuma **dos fuentes externas** y persista los datos en **SQLite** de forma **incremental**, añadiendo una marca temporal de captura. En este sprint, cada fuente funciona de manera independiente.

---

## 🧩 Estructura del proyecto
Proyecto Maven multimódulo con dos módulos:

- `github-feeder`
- `stackexchange-feeder`

Ambos módulos siguen la misma arquitectura basada en **Clean Code** y principios **SOLID**, separando responsabilidades en:

- **Control**: coordinación y consumo de datos  
- **Model**: representación interna  
- **Persistence**: almacenamiento en SQLite  

---

## 🚀 Módulo GitHub (API)
**Fuente:** GitHub API  
**Ejemplo:** repositorios con más estrellas

### Clases principales:
- `GitHubClient` → consume la API  
- `GitHubFeeder` → interfaz de consumo  
- `GitHubTrend` → modelo interno  
- `GitHubStore` → interfaz de persistencia  
- `SqliteDatabaseManager` → guarda en SQLite  
- `Controller` → coordina flujo  
- `Main` → entrada principal + ejecución periódica  

✅ Datos almacenados en **github_trends.db**, tabla `github_trends` con timestamp.

---

## 🌍 Módulo StackExchange (API)
**Fuente:** StackExchange API  
**Endpoint:**  
`https://api.stackexchange.com/2.3/tags?order=desc&sort=popular&site=stackoverflow`

### Clases principales:
- `StackExchangeClient`
- `StackExchangeFeeder`
- `StackExchangeTrend`
- `StackExchangeStore`
- `SqliteDatabaseManager`
- `Controller`
- `Main`

✅ Datos almacenados en **stackexchange_trends.db**, tabla `stackexchange_trends` con timestamp.

---

## 💾 Persistencia
En ambos módulos:
- Se crea la tabla si no existe  
- Se insertan nuevas capturas sin borrar histórico  
- Cada registro incluye `captured_at`  

---

## ⏱️ Periodicidad
Cada módulo realiza capturas periódicas (cada **1 hora**) mediante `Timer`.

---

## ✅ Resultado Sprint 1
- Dos módulos independientes  
- Consumo de fuentes externas dinámicas  
- Persistencia incremental en SQLite  
- Arquitectura preparada para integrar datos en próximos sprints  
