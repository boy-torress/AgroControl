# 🌱 AgroControl - Aplicación Móvil Android

**Gestión agrícola inteligente para pequeños y medianos agricultores de Chile.**

> Proyecto académico - ICI-513 Gestión de Proyectos Informáticos  
> Universidad de Valparaíso · 2025  
> Integrantes: Brandon Torres · Bruno González · Joaquín Aguilera

---

## 📋 Requisitos previos

| Herramienta | Versión mínima |
|---|---|
| Android Studio | Hedgehog (2023.1.1) o superior |
| JDK | 17 |
| Android SDK | API 26 (Android 8.0) |
| Gradle | 8.5+ |

---

## 🚀 Cómo abrir el proyecto en Android Studio

1. **Clona o descomprime** el proyecto en tu computador.
2. Abre **Android Studio**.
3. Selecciona **"Open"** y navega hasta la carpeta `AgroControl/`.
4. Espera que Gradle sincronice (puede tardar 2–5 minutos la primera vez).
5. Conecta un dispositivo Android o crea un **emulador** (API 26+).
6. Presiona ▶️ **Run** (`Shift+F10`).

---

## 🏗️ Arquitectura del proyecto

```
AgroControl/
├── app/src/main/java/com/agrocontrol/
│   ├── data/
│   │   ├── local/
│   │   │   ├── AgroControlDatabase.kt   ← Room DB
│   │   │   ├── dao/Daos.kt              ← DAOs de Room
│   │   │   └── entities/Entities.kt     ← Entidades Room
│   │   └── repository/
│   │       ├── Repositories.kt          ← Auth, Cultivo, Inventario, Alertas
│   │       ├── ClimaService.kt          ← Servicio clima + IA (simulado)
│   │       └── SessionManager.kt        ← Sesión con DataStore
│   ├── di/AppModule.kt                  ← Inyección de dependencias (Hilt)
│   ├── domain/model/Models.kt           ← Modelos de dominio
│   ├── presentation/
│   │   ├── navigation/NavGraph.kt       ← Navegación entre pantallas
│   │   ├── theme/Theme.kt               ← Colores y tipografía
│   │   └── ui/
│   │       ├── auth/      ← Login + Registro (HU-06, HU-07)
│   │       ├── dashboard/ ← Dashboard principal (HU-08)
│   │       ├── cultivo/   ← Registro y ciclo de cultivo (HU-01, HU-02, HU-03)
│   │       ├── clima/     ← Clima y pronóstico + recomendaciones (HU-09, HU-10)
│   │       ├── inventario/← Gestión de insumos (HU-11)
│   │       ├── alertas/   ← Alertas climáticas automáticas (HU-12)
│   │       ├── agronomo/  ← Panel agrónomo (HU-04)
│   │       └── admin/     ← Panel administrador (HU-05)
│   ├── AgroControlApp.kt                ← Application class (Hilt)
│   └── MainActivity.kt                  ← Entry point
└── app/build.gradle.kts                 ← Dependencias
```

**Patrón:** MVVM (Model-View-ViewModel) + Clean Architecture  
**UI:** Jetpack Compose  
**Navegación:** Navigation Component  
**Base de datos:** Room (local)  
**DI:** Hilt  
**Sesión:** DataStore Preferences  

---

## 👤 Roles de usuario

| Rol | Pantalla inicial | Funcionalidades |
|---|---|---|
| **AGRICULTOR** | Dashboard | Todo el flujo de cultivo, clima, inventario, alertas |
| **AGRONOMO** | Panel Agrónomo | Ver cultivos y historial de agricultores asignados (solo lectura) |
| **ADMINISTRADOR** | Panel Admin | Ver todos los cultivos y usuarios, filtros, exportar CSV |

### Cuentas de prueba (crea en Registro):
- Para crear un **Agrónomo** o **Admin**, debes modificar el rol directamente en el código o en la base de datos durante desarrollo (el registro público solo crea AGRICULTORES por seguridad).

---

## 🗺️ Historias de usuario implementadas

| ID | Historia | Sprint | Pantalla |
|---|---|---|---|
| HU-01 | Predicción de rendimiento (IA) | 2 | CultivoScreen |
| HU-02 | Registro de cultivo activo | 1 | RegistroCultivoScreen |
| HU-03 | Seguimiento de etapa del ciclo | 2 | CultivoScreen |
| HU-04 | Seguimiento remoto como agrónomo | 3 | AgronomoScreen |
| HU-05 | Panel de administración | 4 | AdminPanelScreen |
| HU-06 | Registro de cuenta de agricultor | 1 | RegisterScreen |
| HU-07 | Inicio de sesión con rol | 1 | LoginScreen |
| HU-08 | Dashboard con resumen de estado | 1 | DashboardScreen |
| HU-09 | Consulta de clima y pronóstico | 2 | ClimaScreen |
| HU-10 | Recomendación de cultivos IA | 2 | ClimaScreen |
| HU-11 | Gestión de inventario de insumos | 3 | InventarioScreen |
| HU-12 | Alertas climáticas automáticas | 3 | AlertasScreen |

---

## 🌐 API Climática — Open-Meteo (ACTIVA ✅)

La app usa **[Open-Meteo](https://open-meteo.com/)** — gratuita, sin API key, con datos reales.

**Endpoint activo:**
```
GET https://api.open-meteo.com/v1/forecast
    ?latitude=-33.04&longitude=-71.62
    &current=temperature_2m,relative_humidity_2m,wind_speed_10m,precipitation
    &daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max
    &timezone=America/Santiago
```

**Características de la integración:**
- Coordenadas automáticas según la región ingresada (20+ ciudades de Chile)
- Caché en memoria de 1 hora para no repetir llamadas innecesarias
- Descripción e ícono del clima inferidos automáticamente desde los datos
- Pronóstico de 7 días con temperatura máx/mín y probabilidad de lluvia

---

## 📦 Dependencias principales

- **Jetpack Compose BOM 2024.06.00**
- **Room 2.6.1** — Base de datos local
- **Hilt 2.51** — Inyección de dependencias
- **Navigation Compose 2.7.7** — Navegación
- **DataStore Preferences 1.0.0** — Sesión de usuario
- **Retrofit 2.9.0** — HTTP client (listo para API climática)
- **Coroutines + Flow** — Programación asíncrona

---

## ⚠️ Notas importantes

- La **sesión expira automáticamente a los 30 minutos** de inactividad (HU-07 CA3).
- Los **datos climáticos son simulados** y se cachean por 1 hora en producción.
- La **IA de predicción** está simulada; en producción conectar a un endpoint ML real.
- El **inventario** persiste localmente en Room entre sesiones.
- Las **alertas climáticas** se generan automáticamente al registrar un cultivo.
