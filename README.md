# OdontoHistoria IA — App Android (paciente)

App móvil del paciente de la clínica dental **Fassiara**. Forma parte del proyecto OdontoHistoria IA (SENATI, Ingeniería de Software con IA).

| Parte | Repositorio |
|---|---|
| App Android (este repositorio) | `odontohistoria-app` |
| Backend (API REST, Node.js + Express) | https://github.com/wilfredoalced-byte/odontohistoria-backend |
| Panel web de la clínica (React + Vite) | https://github.com/wilfredoalced-byte/odontohistoria-frontend |

## Qué hace la app

- Inicio de sesión (pantalla de acceso; en esta versión solo valida que los campos no estén vacíos).
- Pantalla principal con la próxima cita del paciente (se sincroniza con el servidor).
- **Odontograma**: muestra las piezas dentales (Sano / Caries / Tratado) desde el servidor. Al tocar una pieza cambia su estado y se guarda en el servidor (`PUT`/`DELETE /pacientes/:id/odontograma/:pieza`).
- **Historial de tratamientos**: lista los tratamientos reales del paciente (`GET /tratamientos?pacienteId=`).
- Datos locales con Room (SQLite) y sincronización con el backend mediante Retrofit.

## Tecnología

Java · Android (Room 2.6, Retrofit 2.9, Gson, OkHttp) · ViewModel/LiveData · Gradle (Kotlin DSL).

## Requisitos

- Android Studio reciente: el proyecto usa Android Gradle Plugin 9.3, Gradle 9.5 y compileSdk 37 (Android Studio ofrece descargar el SDK Android 37 al abrirlo).
- Un emulador o celular con Android 7.0 (API 24) o superior.
- Node.js 20.19 o superior para el backend y el panel (se probó con la 22).

## Cómo probarla

1. Encender el backend:
   ```
   cd odontohistoria-backend
   npm install
   node server.js        # http://localhost:3000
   ```
2. Con el emulador abierto, crear el túnel hacia el backend:
   ```
   adb reverse tcp:3000 tcp:3000
   ```
3. Abrir este proyecto en Android Studio, esperar el Gradle Sync y pulsar **Run**.
4. Entrar con cualquier usuario y contraseña (versión de demostración).

El panel web (`odontohistoria-frontend`, `npm run dev`) muestra el mismo odontograma: un cambio hecho en la app aparece en el panel.

## Evidencias de prueba

Capturas reales tomadas el 22/11/2026 con la app corriendo en un emulador Pixel 8 Pro (Android 17, API 37.1) contra el backend local:

| Captura | Qué muestra |
|---|---|
| `docs/capturas/00_app_pantalla_principal.jpg` | Pantalla principal de la app |
| `docs/capturas/01_app_historial_tratamientos.jpg` | Historial con los 4 tratamientos del servidor (incluye Endodoncia, En curso) |
| `docs/capturas/02_app_odontograma_cargado.jpg` | Odontograma cargado desde el servidor (16 Caries, 13 Tratado) |
| `docs/capturas/03_app_odontograma_pieza17_caries.jpg` | Pieza 17 cambiada a Caries desde la app |
| `docs/capturas/05_app_odontograma_estado_final.jpg` | Estado final en la app (11, 13, 16 y 17 en Caries) |
| `docs/capturas/06_panel_web_odontograma.png` | El panel web muestra las mismas piezas en Caries (4) |
| `docs/capturas/07_app_odontograma_en_android_studio.png` | App en el emulador dentro de Android Studio |

## Limitaciones conocidas (versión de desarrollo)

- La comunicación con el backend usa **HTTP** (permitido solo para desarrollo en `network_security_config.xml`). Para producción se necesita HTTPS.
- El inicio de sesión es de demostración; falta autenticación real.
- Los textos de consentimiento (en el backend) son un **borrador** y requieren revisión legal antes de publicar la app.
- Los datos de salud son datos sensibles: antes de Google Play faltan la política de privacidad y el formulario de Seguridad de los datos.
