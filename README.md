# TiendaApp (Móvil) 🛒📱

Aplicación nativa de **Punto de Venta (POS)** y **Gestión de Inventario** para Android, desarrollada en **Kotlin** con **Jetpack Compose**. Diseñada como una solución agnóstica, moderna y de alto rendimiento para pequeños y medianos negocios (tiendas de conveniencia, ferreterías, abarrotes, etc.), destacando por su funcionamiento 100% offline (**offline-first architecture**), importación/exportación omnicanal y procesamiento inteligente de recursos.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Android SDK](https://img.shields.io/badge/Min%20SDK-33%20|%20Target-35-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Room DB](https://img.shields.io/badge/Room%20DB-2.6.1-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![App Version](https://img.shields.io/badge/Version-v1.0.4-orange?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-green?style=for-the-badge)

---

## 📱 Características Principales

### 1. Punto de Venta Universal (POS) & Carrito Dinámico
* **Soporte Múltiple de Unidades**: Maneja productos por piezas fijas (`piece`) o por peso (`kg` con soporte decimal de gramos).
* **Entrada de Cantidad Flexible**: Modal iterativo (`QuantityDialog`) para especificar kilos/gramos o unidades antes de agregar al carrito.
* **Carrito Interactivo**: Visualización clara con tarjetas por producto (`SaleItemCard`) que permiten la eliminación individual o vaciado completo del carrito.
* **Cobro & Selección de Estado**:
  * Cálculo instantáneo de total, monto recibido y cálculo automático de cambio ($).
  * Admite estados de pago: **Contado** (Efectivo/Pagado), **Pendiente** (Ventas a crédito / Fiado) y **Cancelado**.
  * Entrada opcional de nombre del cliente, esencial para dar seguimiento a cuentas pendientes.
* **Navegación Ágil**: Acceso directo al POS desde el catálogo seleccionando un producto en particular o mediante venta general.

### 2. Gestión de Catálogo & Edición Visual de Productos
* **Catálogo Vivo**: Creación, actualización y eliminación de productos en tiempo real.
* **Personalización del Negocio**: Edición del nombre de la tienda directamente desde el encabezado principal, guardado en `SharedPreferences`.
* **Menú de Acciones Rápidas**: Al mantener presionado un producto en el carrusel (`ProductCarousel`) se despliega un diálogo contextual para editar o eliminar.
* **Optimizador de Imágenes (`ImageUtils`)**:
  * Decodifica, escala y comprime fotografías dinámicamente a un máximo de **700px** con **80% de calidad JPEG**, reduciendo imágenes de varios megabytes a escasos kilobytes sin perder nitidez en pantalla.
* **Editor con Marca de Agua (`ImageEditorDialog`)**:
  * Herramienta integrada para superponer texto de forma personalizada sobre las imágenes de los productos antes de guardarlas en el almacenamiento interno.

### 3. Historial de Ventas & Control Crediticio
* **Auditoría Completa**: Registro detallado de cada transacción indicando fecha/hora, cliente, productos, total, monto recibido, cambio y estado.
* **Filtros Dinámicos**: Clasificación instantánea de historial por estado (**Todos**, **Contado**, **Pendiente**).
* **Resumen de Ventas Acumulado**: Botón flotante extendido con el monto total cobrado en efectivo. Al presionar, abre un modal con el desglose detallado de totales e ítems por estado (**Contado**, **Pendiente** y **Cancelado**).
* **Actualización de Estado**: Permite editar y modificar el estado de cualquier venta registrada (por ejemplo, liquidar ventas marcadas previamente como *Pendiente* cuando el cliente cancela su deuda).
* **Gestión de Registros**: Opción para eliminar ventas individuales o limpiar el historial completo.

### 4. Importación y Exportación Inteligente (Offline Backup)
* **Respaldo de Catálogo en JSON (con imágenes en Base64)**:
  * Exporta todo el catálogo a un archivo `.json` empaquetando las fotografías guardadas localmente en cadenas **Base64**.
  * Modal inteligente de importación con dos modalidades: **Reemplazar Actuales** (sobrescribe la base de datos local) o **Añadir** (fusiona los nuevos productos sin perder los existentes).
  * Al importar, las imágenes se decodifican e independizan en el almacenamiento interno de la app automáticamente.
* **Respaldo de Ventas en Excel (`.xlsx`)**:
  * Exportación completa a hojas de cálculo Microsoft Excel mediante **Apache POI**, generando 10 columnas estructuradas (ID, Fecha Texto, Timestamp, Cliente, Total, Recibido, Cambio, Estado, Resumen de productos y JSON Raw).
  * Importador con validación de cabeceras que lee y restaura el libro de ventas `.xlsx` ofreciendo también modalidades de **Reemplazar Actuales** o **Añadir**.
* **Integración Nativa de Sistema (Intent Filters)**:
  * La aplicación escucha archivos `.json` y `.xlsx` abiertos desde exploradores de archivos, WhatsApp, Drive o correo electrónico, abriendo automáticamente el cuadro de diálogo para importar catálogos o historial.

---

## 🛠️ Stack Tecnológico & Librerías

La aplicación sigue rigurosamente los principios de **Clean Architecture** y el patrón **MVVM (Model-View-ViewModel)**.

| Componente | Tecnología / Librería | Versión | Descripción |
| :--- | :--- | :--- | :--- |
| **Lenguaje** | Kotlin | `2.0.21` | Lenguaje oficial de desarrollo nativo en Android. |
| **Build Tool** | Android Gradle Plugin (AGP) | `8.9.0` | Herramienta de compilación y empaquetado del proyecto. |
| **UI Toolkit** | Jetpack Compose + Material3 | `2024.09.00` (BOM) | Renderizado declarativo con componentes Material Design 3. |
| **Iconografía** | Compose Material Icons Extended | `1.7.6` | Conjunto extendido de iconos nativos. |
| **Navegación** | Navigation Compose | `2.8.5` | Manejo de rutas, parámetros opcionales e Intent URIs. |
| **Persistencia** | Room Database | `2.6.1` | Abstracción de SQLite con TypeConverters para datos complejos. |
| **Asincronía** | Kotlin Coroutines & Flow | Default | Reactividad y operaciones E/S en hilos secundarios. |
| **Carga de Fotos** | Coil | `2.5.0` | Asynchronous image loader optimizado para Compose. |
| **Excel I/O** | Apache POI (poi & poi-ooxml) | `5.2.5` | Creación y lectura de libros de trabajo `.xlsx`. |
| **JSON & Base64** | Gson | `2.10.1` | Serialización/deserialización de estructuras DTO y carritos. |

---

## 🏗️ Arquitectura & Estructura del Proyecto

El código está estructurado en paquetes limpios según la responsabilidad de cada capa:

```text
com.tienditajhonyboy.tiendaapp/
├── AppContainer.kt          # Contenedor de Inyección de Dependencias manual
├── MainActivity.kt          # Activity principal con captura de Intents de archivos (.json / .xlsx)
├── TiendaApplication.kt     # Clase Application para inicializar AppContainer
├── data/
│   ├── local/              # Database Room, DAOs, Entities y TypeConverters
│   │   ├── AppDatabase.kt
│   │   ├── Converters.kt
│   │   ├── ProductDao.kt / ProductEntity.kt
│   │   └── SaleDao.kt / SaleEntity.kt
│   └── repository/         # Implementación de repositorios (ProductRepositoryImpl, SaleRepositoryImpl)
├── domain/
│   ├── model/              # Modelos de Dominio (Product, Sale, CartItem, Enums)
│   └── repository/         # Interfaces abstractas de repositorios (ProductRepository, SaleRepository)
├── ui/
│   ├── components/         # Diálogos y componentes UI (ProductCarousel, QuantityDialog, PaymentDialog, ImageEditorDialog, SaleItemCard)
│   ├── navigation/         # Grafo de navegación y enrutamiento de Uri externas (AppNavigation)
│   ├── screens/            # Pantallas (HomeScreen, POSScreen, HistoryScreen, ProductNewScreen, ProductEditScreen)
│   ├── theme/              # Tema Material3, paleta de colores y tipografía (Color.kt, Theme.kt, Type.kt)
│   └── viewmodel/          # ViewModels y Factory (HomeViewModel, POSViewModel, HistoryViewModel, ProductNewViewModel, ProductEditViewModel, AppViewModelProvider)
└── util/
    └── ImageUtils.kt       # Escala de mapas de bits, compresión JPG y conversión Base64
```

---

## 🚀 Requisitos e Instalación

### Requisitos Mínimos
* **Android Studio**: Ladybug (2024.2.1) o superior.
* **JDK**: Versión 11.
* **Dispositivo / Emulador**:
  * **Min SDK**: API 33 (Android 13).
  * **Target / Compile SDK**: API 35 (Android 15).

### Pasos de Instalación
1. Clonar el repositorio:
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   ```
2. Abrir el proyecto en **Android Studio**.
3. Dejar que **Gradle** sincronice las dependencias automáticas.
4. Conectar un dispositivo con Android 13+ o iniciar un emulador API 33+.
5. Ejecutar la aplicación (`Shift + F10` o el botón **Run**).


