# Pescadería App (Móvil)

Aplicación nativa de Punto de Venta (POS) y Gestión de Inventario para Android, desarrollada con **Kotlin** y **Jetpack Compose**. Diseñada para ofrecer paridad visual y funcional con la versión Web, optimizada para tabletas y dispositivos móviles.

![Banner](https://img.shields.io/badge/Kotlin-2.0-purple) ![Banner](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue) ![Banner](https://img.shields.io/badge/Architecture-Clean%20MVVM-green)

## 📱 Características Principales

### 1. Punto de Venta (POS)
*   **Carrito de Compras**: Agrega productos rápidamente desde el Home o el buscador.
*   **Cálculo Automático**: Totales, cambio a devolver y gestión de unidades (Kg/Pzas).
*   **Tipos de Pago**: Venta de Contado o Pendiente (Crédito).
*   **Validaciones**: Control de stocks y validación de montos recibidos.

### 2. Gestión de Inventario
*   **Altas**: Creación de nuevos productos con nombre, precio y unidad.
*   **Imágenes**:
    *   **Cámara y Galería**: Toma fotos o selecciona de la galería.
    *   **Editor Integrado**: Zoom, recorte y superposición de texto (ej "OFERTA", "GRANDE") sobre la imagen del producto.
*   **Bajas**: Elimina productos manteniendo presionado el ítem (Long Press).

### 3. Historial y Reportes
*   **Registro de Ventas**: Lista detallada de todas las transacciones.
*   **Exportación Excel**: Generación de reportes `.xlsx` compatibles con Excel/Sheets usando Apache POI.
*   **Gestión**: Visualización de detalles y opción para cancelar ventas.

### 4. UI/UX Premium
*   **Diseño Moderno**: Tema Claro (Light Theme) con paleta de colores corporativa (Sky Blue / Teal).
*   **Glassmorphism**: Efectos de desenfoque y transparencias en tarjetas de productos.
*   **Animaciones**: Transiciones suaves y feedback visual (Toasts, Dialogs personalizados).
*   **Paridad Web**: Diseño sincronizado pixel-perfect con la aplicación web administrativa.

## 🛠️ Stack Tecnológico

La aplicación sigue los principios de **Clean Architecture** y el patrón **MVVM** (Model-View-ViewModel).

*   **Lenguaje**: Kotlin.
*   **UI Toolkit**: Jetpack Compose (Material3).
*   **Persistencia de Datos**: Room Database (SQLite).
*   **Inyección de Dependencias**: Manual (AppContainer).
*   **Carga de Imágenes**: Coil.
*   **Asincronía**: Coroutines & Flow.
*   **Exportación**: Apache POI (Excel).
*   **Navegación**: Navigation Compose.

## 📂 Estructura del Proyecto

```
com.example.tiendaapp
├── data            # Capa de Datos (Room, Repository Impl)
├── domain          # Capa de Dominio (Models, Repository Interface)
├── ui              # Capa de Presentación
│   ├── components  # Composables Reutilizables (Dialogs, Cards)
│   ├── navigation  # Grafo de Navegación
│   ├── screens     # Pantallas (Home, POS, History, ProductNew)
│   ├── theme       # Sistema de Diseño (Color, Type, Theme)
│   └── viewmodel   # State management
└── TiendaApplication.kt
```

## 🚀 Instalación

1.  Clonar el repositorio.
2.  Abrir en **Android Studio Ladybug** (o superior).
3.  Sincronizar proyecto con Gradle.
4.  Ejecutar en emulador o dispositivo físico (Min SDK 24).

---
**Nota**: Esta aplicación incluye manejo de permisos para Cámara y Almacenamiento (Lectura/Escritura para exportación de reportes).
