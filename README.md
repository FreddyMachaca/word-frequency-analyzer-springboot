# Contador de Palabras

Sistema de análisis optimizado para el procesamiento de archivos de texto grandes, desarrollado con Spring Boot y una interfaz web.

## Objetivo del Proyecto

Este proyecto implementa un sistema de análisis  especializado en el conteo y análisis de frecuencia de palabras en archivos de texto de gran tamaño. Está diseñado para procesar eficientemente el archivo `es-wiki-abstracts.txt` (~336 MB) que contiene abstracts de Wikipedia en español.

## Tecnologías Utilizadas

- **Backend**: Spring Boot 3.x, Java 17+
- **Frontend**: HTML5, CSS3, JavaScript, Bootstrap 5
- **Visualizaciones**: Chart.js
- **Build Tool**: Maven
- **Template Engine**: Thymeleaf

## Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- **Java 17 o superior**
- **Maven 3.6 o superior**

## Instalación y Configuración


### 1. Verificar el Archivo de Datos

Asegúrate de que el archivo `es-wiki-abstracts.txt` esté presente en el directorio raíz del proyecto. 
- Enlace de descarga de `es-wiki-abstracts.txt`: https://drive.google.com/file/d/1junMIikr0Bu3_mxGElfJLm7cM2lW775M/view?usp=sharing

### 2. Compilar el Proyecto

```bash
# Limpiar y compilar
mvn clean compile

# O compilar y ejecutar tests
mvn clean package
```

## Ejecución del Proyecto

### Opción 1: Usando Maven (Recomendado)

```bash
# Ejecutar directamente con Maven
mvn spring-boot:run
```

### Opción 2: Usando JAR Compilado

```bash
# Compilar primero
mvn clean package

# Ejecutar el JAR
java -jar target/reton1-0.0.1-SNAPSHOT.jar
```

### Opción 3: Desde IDE

1. Importar el proyecto en tu IDE favorito (IntelliJ IDEA, Eclipse, VS Code)
2. Localizar la clase principal con `@SpringBootApplication`
3. Ejecutar como aplicación Java

## 🌐 Acceso a la Aplicación

Una vez iniciada la aplicación, accede a:

```
http://localhost:8080
```

### Endpoints Disponibles

- **`GET /`** - Interfaz principal de usuario
- **`GET /api/status`** - Estado del sistema
- **`POST /api/analyze`** - Iniciar análisis de palabras

##  Uso de la Aplicación

### 1. Verificar Estado del Sistema
- Al cargar la página, verás el estado del sistema
- Información sobre memoria disponible y procesadores

### 2. Iniciar Análisis
- Hacer clic en "Iniciar Análisis de Palabras"
- El sistema procesará el archivo automáticamente
- Se mostrará un indicador de progreso

### 3. Visualizar Resultados
- **Métricas Generales**: Palabras totales, únicas, tiempo de procesamiento
- **Gráfico de Barras**: Top 20 palabras más frecuentes
- **Gráfico de Rendimiento**: Distribución del tiempo de procesamiento
- **Tabla Detallada**: Lista completa de palabras con frecuencias

## Estructura del Proyecto

```
reton1/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/ejemplo/reton1/
│       │       ├── RetonApplication.java
│       │       ├── controller/
│       │       ├── service/
│       │       └── model/
│       └── resources/
│           ├── templates/
│           │   └── index.html
│           └── application.properties
├── target/
├── es-wiki-abstracts.txt
├── pom.xml
├── .gitignore
└── README.md
```

## Optimizaciones de Rendimiento

El sistema incluye varias optimizaciones para el procesamiento de Big Data:

- **Streaming de Archivos**: Lectura eficiente sin cargar todo en memoria
- **Procesamiento Paralelo**: Utilización de múltiples cores del CPU
- **Estructuras de Datos Optimizadas**: HashMap y algoritmos eficientes
- **Gestión de Memoria**: Control de heap y garbage collection

##  Configuración

### Configuración en application.properties

El archivo `src/main/resources/application.properties` contiene configuraciones importantes:

```properties
# Puerto del servidor
server.port=8080

# Configuración para archivos grandes
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# Optimizaciones de memoria
server.tomcat.max-swallow-size=-1

# JVM optimizations
spring.jpa.open-in-view=false

# Configuración de charset UTF-8
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

### Ajustar Memoria JVM

Para archivos muy grandes, puedes ajustar la memoria:

```bash
# Aumentar heap size
java -Xmx4g -jar target/reton1-0.0.1-SNAPSHOT.jar

# O con Maven
export MAVEN_OPTS="-Xmx4g"
mvn spring-boot:run
```

### Configurar Puerto

```bash
# Cambiar puerto por defecto
java -jar target/reton1-0.0.1-SNAPSHOT.jar --server.port=8081
```

## Solución de Problemas

### Error de Memoria Insuficiente
```bash
# Síntoma: OutOfMemoryError
# Solución: Aumentar heap size
java -Xmx8g -jar target/reton1-0.0.1-SNAPSHOT.jar
```

### Puerto en Uso
```bash
# Síntoma: Port 8080 already in use
# Solución: Cambiar puerto
java -jar target/reton1-0.0.1-SNAPSHOT.jar --server.port=8081
```

### Archivo no Encontrado
```bash
# Síntoma: FileNotFoundException: es-wiki-abstracts.txt
# Solución: Verificar que el archivo esté en el directorio raíz
ls -la es-wiki-abstracts.txt
```