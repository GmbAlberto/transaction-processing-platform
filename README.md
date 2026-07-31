# Transaction Processing Platform

Aplicación desarrollada como parte de una evaluación técnica.

La solución incluye:

- Frontend desarrollado con React.
- API principal desarrollada con Spring Boot.
- Servicio de procesamiento de transacciones desarrollado con Spring Boot.
- Persistencia en base de datos en memoria.
- Autenticación mediante JWT.
- Cifrado AES-256 de información sensible.
- Comunicación entre servicios mediante OpenFeign.
- Despliegue mediante Docker Compose.


## 1. Demo en línea

La demostración se encuentra desplegada en un servidor VPS mediante Docker Compose y Nginx como reverse proxy.

Frontend:
https://transactions.bethocr.dev

Documentación API / Swagger:
https://api-transactions.bethocr.dev/swagger-ui/index.html


### Credenciales de prueba

- Usuario: usuario
- Contraseña: abc123#


## 2. Arquitectura

El proyecto está compuesto por tres aplicaciones:

### transaction-frontend

Aplicación React que proporciona la interfaz de usuario.

### transaction-api

API de entrada encargada de:

- Autenticación y generación de tokens JWT.
- Validación de solicitudes.
- Descifrado AES-256.
- Comunicación con el servicio interno.
- Estandarización de respuestas y manejo de errores.

### transaction-service

Servicio responsable de:

- Procesamiento de transacciones.
- Generación de referencias.
- Persistencia en base de datos.
- Consulta, actualización y cancelación de transacciones.


## 3. Requisitos

Para ejecutar los servicios localmente:

- Java 21
- Maven
- Node.js
- npm

Para ejecutar el proyecto con contenedores:

- Docker
- Docker Compose


## 4. Variables de entorno

### Frontend sin Docker

Para ejecutar el frontend en modo desarrollo, crear el archivo:

`transaction-frontend/.env.development`
```
VITE_API_URL=http://localhost:8080
VITE_AES_SECRET_KEY=CLAVE_AES_EN_BASE64
```

### Frontend con Docker

Para la construcción mediante Docker Compose se utiliza:

`transaction-frontend/.env.production`
```
VITE_API_URL=/api
VITE_AES_SECRET_KEY=CLAVE_AES_EN_BASE64
```


### Variables de entorno de transaction-api

Las siguientes variables son utilizadas por transaction-api:
```
TRANSACTION_SERVICE_URL: http://transaction-service:8081
AES_SECRET_KEY=CLAVE_AES_EN_BASE64
JWT_SECRET_KEY=CLAVE_PARA_CONSTRUIR_JWT
```

---

## 5. Ejecución local sin Docker

### 1. Ejecutar transaction-service

```bash
cd transaction-service
mvn spring-boot:run
```

El servicio estará disponible en:

http://localhost:8081


### 2. Ejecutar transaction-api

```bash
cd transaction-api
mvn spring-boot:run
```

La API estará disponible en:

http://localhost:8080


### 3. Ejecutar el frontend

```bash
cd transaction-frontend
npm install
npm run dev
```

La aplicación estará disponible normalmente en:

http://localhost:5173

---

## 6. Ejecución con Docker Compose

Desde la raíz del proyecto:
docker compose up -d --build

La aplicación estará disponible en:
http://localhost

---

## 7. Inicialización de usuario y base de datos

Al iniciar la aplicación se crean las tablas en la base de datos H2 en memoria.

Se crea el siguiente usuario de prueba:

- Usuario: usuario
- Contraseña: abc123#

---

## 8. Documentación de la API

Con la aplicación ejecutándose localmente:

### transaction-api

http://localhost:8080/swagger-ui/index.html

### transaction-service

http://localhost:8081/swagger-ui/index.html

---

## 9. Pruebas

Para ejecutar las pruebas de `transaction-api`:

```bash
cd transaction-api
mvn test
```

Para ejecutar las pruebas de `transaction-service`:
```bash
cd transaction-service
mvn test
```

---

## 10. Decisiones técnicas

```md
- La API principal realiza autenticación, validación, descifrado y manejo de respuestas externas.
- La comunicación entre servicios se implementó mediante OpenFeign.
- La contraseña del usuario se almacena utilizando BCrypt.
- La sesión se maneja mediante JWT con una duración limitada (10 minutos).
- La información sensible enviada desde el frontend se cifra mediante AES-256.
- Docker Compose permite levantar todos los componentes de forma reproducible.
- Nginx se utiliza para servir el frontend y redirigir las solicitudes `/api`.
```

---

## 11. Estructura del repositorio

```text
transaction-processing-platform/
├── transaction-api/
├── transaction-service/
├── transaction-frontend/
├── docker-compose.yml
└── README.md
```

---

## 12. Consideraciones

```md
## Consideraciones

- El repositorio contiene archivos de entorno únicamente con valores destinados a la evaluación técnica.
- Las claves incluidas no corresponden a credenciales productivas.
- Para un entorno productivo real, los secretos deberían administrarse mediante variables protegidas, Docker Secrets o un servicio de gestión de secretos.