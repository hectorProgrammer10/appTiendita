# Tienda App (Móvil)

Aplicación nativa de Punto de Venta (POS) y Gestión de Inventario para Android, desarrollada con **Kotlin** y **Jetpack Compose**. Diseñada como una solución agnóstica para todo tipo de negocios pequeños y medianos (abarrotes, ferreterías, tiendas de conveniencia, etc.), destacando por su robusta arquitectura sin conexión (offline-first).

![Banner](https://img.shields.io/badge/Kotlin-2.0-purple) ![Banner](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue) ![Banner](https://img.shields.io/badge/Architecture-Clean%20MVVM-green)

## 📱 Características Principales

### 1. Punto de Venta Universal (POS)
*   **Carrito de Compras**: Agrega productos ágilmente desde el catálogo.
*   **Gestión Matemática Automatizada**: Suma de totales y cálculo de cambio.
*   **Ventas de Crédito**: Manejo de ventas de contado o pendientes.
*   **Cantidades Flexibles**: Adapta pesos (Kilos/Gramos) o piezas fijas sin esfuerzo.

### 2. Gestión de Inventario & Fotografías
*   **Catálogo Vivo**: Edita precios, nombres y unidades dinámicamente.
*   **Reducción Fotográfica Inteligente**: Motor de optimización que comprime y recorta tus fotografías JPEG en la memoria local limitando un máximo de 700 píxeles a 80% de calidad, reduciendo megabytes a kilobytes.
*   **Importación y Exportación Perfecta**: Transfiere tu base de datos de productos a dispositivos nuevos usando un solo archivo ultraligero (`.json`), reconstruyendo **precios y fotografías (con codificación Base64 automágicamente)**.

### 3. Historial Inquebrantable
*   **Auditoría de Ventas**: Visualiza en un toque todo lo facturado. Edita el estado de una venta si el cliente la liquidó en la tarde.
*   **Respaldo Lossless en Excel**: Crea copias totales en `.xlsx` incluyendo columnas lógicas (JSON internos, tipo de pago, total, cambios y cliente).
*   **Restauración de Libros**: Importa y lee de vuelta los documentos `.xlsx` desde tu almacenamiento para añadir o sobreescribir la memoria histórica de la caja registradora.
*   **Resumen Rápido**: Totales y cuentas agrupadas por estado (Contado, Pendiente, Cancelado).

### 4. UI/UX Premium & Semántica
*   **Diseño Homogéneo**: Sistema de diseño unificado, combinando Material3 con colores semánticos (`SuccessGreen`, `InfoBlue`, `DangerRed`) y gradientes modernos.
*   **Glassmorphism**: Transparencias sutiles para priorizar información.
*   **Animaciones Recreativas**: Feedback orgánico con botones que pulsan y transiciones.

## 🛠️ Stack Tecnológico

La aplicación sigue los principios de **Clean Architecture** y el patrón **MVVM** (Model-View-ViewModel).

*   **Lenguaje**: Kotlin.
*   **UI Toolkit**: Jetpack Compose (Material3).
*   **Persistencia de Datos**: Room Database (SQLite).
*   **Inyección de Dependencias**: Manual (AppContainer).
*   **Manipulación de Datos Externos**: Apache POI (Excel) y Gson (JSON y Base64).
*   **Carga de Imágenes**: Coil.
*   **Asincronía**: Coroutines & Flow.

## 🚀 Instalación

1.  Clonar el repositorio.
2.  Abrir en **Android Studio Ladybug** (o superior).
3.  Sincronizar proyecto con Gradle.
4.  Ejecutar en emulador o dispositivo físico (Min SDK 24).
