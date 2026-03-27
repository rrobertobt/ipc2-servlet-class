# Documentación del Proyecto — API REST Todo con JWT

## Índice
1. [Resumen de cambios](#1-resumen-de-cambios)
2. [Estructura del proyecto](#2-estructura-del-proyecto)
3. [Base de datos](#3-base-de-datos)
4. [¿Qué es JWT y cómo funciona?](#4-qué-es-jwt-y-cómo-funciona)
5. [Implementación JWT paso a paso](#5-implementación-jwt-paso-a-paso)
6. [Endpoints de la API](#6-endpoints-de-la-api)
7. [Flujo completo de una petición autenticada](#7-flujo-completo-de-una-petición-autenticada)
8. [Cómo probar la API](#8-cómo-probar-la-api)

---

## 1. Resumen de cambios

| Área | Cambio |
|------|--------|
| `pom.xml` | Se agregaron las dependencias de la librería **JJWT** (io.jsonwebtoken) |
| `db.sql` | Se creó la tabla `usuarios` y se agregó la columna `usuario_id` en `todos` con FK |
| `models/Usuario.java` | Nuevo modelo que representa a un usuario del sistema |
| `utils/HashUtil.java` | Utilidad para hashear contraseñas con SHA-256 |
| `utils/JwtUtil.java` | Utilidad central para **crear y validar tokens JWT** |
| `data/UsuarioDB.java` | DAO para consultar usuarios en la base de datos |
| `services/UsuarioServicio.java` | Servicio que valida credenciales y genera el JWT |
| `controllers/AuthController.java` | Nuevo endpoint `POST /api/auth/login` |
| `filters/AuthFilter.java` | Filtro que intercepta `/api/*` y verifica el token JWT |
| `models/Tarea.java` | Se agregó el campo `usuarioId` |
| `data/TareaDB.java` | Todas las operaciones ahora filtran por `usuario_id` |
| `services/TareaServicio.java` | Los métodos reciben `username` y resuelven el `usuarioId` |
| `controllers/TareaController.java` | Se movió a `/api/tareas/*` y lee el `username` del request |
| `web.xml` | Se registró el `AuthFilter` para la ruta `/api/*` |

---

## 2. Estructura del proyecto

```
src/main/java/edu/rb/ejemploservlets/
├── controllers/
│   ├── AuthController.java      ← POST /api/auth/login
│   └── TareaController.java     ← CRUD /api/tareas/*  (protegido)
├── data/
│   ├── TareaDB.java             ← SQL de tareas (filtrado por usuario)
│   └── UsuarioDB.java           ← SQL de usuarios
├── db/
│   └── DatabaseConnection.java  ← Sin cambios
├── filters/
│   ├── AuthFilter.java          ← Verifica JWT en /api/*
│   └── CORSFilter.java          ← Sin cambios
├── models/
│   ├── Tarea.java               ← Ahora incluye usuarioId
│   └── Usuario.java             ← Nuevo
├── services/
│   ├── TareaServicio.java       ← Métodos reciben username
│   └── UsuarioServicio.java     ← Nuevo: login y generación JWT
└── utils/
    ├── DBException.java         ← Sin cambios
    ├── HashUtil.java            ← Nuevo: hash SHA-256
    └── JwtUtil.java             ← Nuevo: crear/validar JWT
```

---

## 3. Base de datos

### Tabla `usuarios`

```sql
CREATE TABLE IF NOT EXISTS usuarios (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,   -- hash SHA-256 de la contraseña
    creado_en TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Importante:** la contraseña **nunca se guarda en texto plano**. Se almacena el hash SHA-256. Si bien en producción se recomienda BCrypt o Argon2 (algoritmos lentos diseñados para contraseñas), SHA-256 se usa aquí por simplicidad didáctica.

### Tabla `todos` (modificada)

```sql
CREATE TABLE IF NOT EXISTS todos (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT          NOT NULL,           -- nueva columna
    titulo     VARCHAR(255) NOT NULL,
    completado TINYINT(1)   NOT NULL DEFAULT 0,
    creado_en  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_todos_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
```

La `FOREIGN KEY` garantiza integridad referencial: si se elimina un usuario, sus tareas también se eliminan automáticamente (`ON DELETE CASCADE`).

---

## 4. ¿Qué es JWT y cómo funciona?

**JWT (JSON Web Token)** es un estándar abierto (RFC 7519) que define una forma compacta y segura de transmitir información entre dos partes como un objeto JSON. Se usa principalmente para **autenticación sin estado** (_stateless_).

### Estructura de un JWT

Un JWT es un string con **tres partes** separadas por puntos (`.`):

```
HEADER.PAYLOAD.SIGNATURE
```

Ejemplo real:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMDAwMDAwMCwiZXhwIjoxNzEwMDA3MjAwfQ
.x4a9dGz7b3KqPQsR2mT8vN1cJfYeW6hLuDoXpAiBqZE
```

Cada parte está codificada en **Base64URL** (no cifrada — solo codificada).

#### Parte 1: HEADER

Indica el tipo de token y el algoritmo de firma:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

- `alg: HS256` → HMAC con SHA-256 (algoritmo de firma simétrico)
- `typ: JWT` → tipo de token

#### Parte 2: PAYLOAD (Claims)

Contiene los datos del usuario. Estos datos son públicos (cualquiera puede decodificarlos), por eso **nunca incluyas contraseñas u otros datos sensibles**:

```json
{
  "sub": "admin",
  "iat": 1710000000,
  "exp": 1710007200
}
```

| Claim | Significado |
|-------|-------------|
| `sub` | Subject — quién es el dueño del token (el username) |
| `iat` | Issued At — cuándo fue emitido (timestamp Unix) |
| `exp` | Expiration — cuándo expira (timestamp Unix) |

#### Parte 3: SIGNATURE

La firma garantiza que el token **no fue modificado**. Se calcula así:

```
HMACSHA256(
  Base64URL(header) + "." + Base64URL(payload),
  clave_secreta
)
```

Si alguien modifica el payload (por ejemplo, cambia el username), la firma ya no coincidirá y el servidor rechazará el token.

### ¿Por qué JWT?

| Enfoque tradicional (sesiones) | JWT |
|-------------------------------|-----|
| El servidor guarda el estado de la sesión en memoria o BD | El servidor no guarda nada — el token se valida matemáticamente |
| Difícil de escalar horizontalmente | Escala perfectamente (cualquier servidor puede validar el token) |
| Requiere cookies de sesión | Funciona con el header `Authorization` |
| Ideal para apps web monolíticas | Ideal para APIs REST y SPA (Angular, React, etc.) |

---

## 5. Implementación JWT paso a paso

### 5.1 Dependencias (pom.xml)

Se agregaron tres artefactos de la librería **JJWT**:

```xml
<!-- API pública (interfaces y clases que usamos en el código) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>

<!-- Implementación interna (solo necesaria en runtime, no en compilación) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Integración con Gson para serializar el JSON interno del JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-gson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### 5.2 JwtUtil — el corazón de la autenticación

`JwtUtil.java` encapsula toda la lógica de JWT en dos métodos:

```java
// Genera un token para el usuario
public static String generarToken(String username) {
    return Jwts.builder()
            .subject(username)          // quién es el dueño
            .issuedAt(new Date())       // cuándo se creó
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS)) // cuándo expira
            .signWith(KEY)              // firma con HS256
            .compact();                 // construye el string final
}

// Valida el token y extrae el username
public static String validarYObtenerUsername(String token) {
    return Jwts.parser()
            .verifyWith(KEY)            // clave para verificar la firma
            .build()
            .parseSignedClaims(token)   // parsea y valida en un solo paso
            .getPayload()
            .getSubject();              // extrae el username
}
```

El método `parseSignedClaims` hace tres cosas automáticamente:
1. Verifica que la **firma** sea correcta (el token no fue manipulado)
2. Verifica que el token **no haya expirado**
3. Si algo falla, lanza una `JwtException`

**Clave secreta:**
```java
private static final String SECRET = "clave-super-secreta-para-jwt-que-debe-ser-larga-256bits!!";
private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
```

> ⚠️ **En producción:** la clave secreta debe estar en una variable de entorno, nunca hardcodeada en el código fuente.

### 5.3 HashUtil — protección de contraseñas

Antes de guardar o comparar contraseñas, se aplica SHA-256:

```java
public static String sha256(String input) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
    // convertir bytes a string hexadecimal
    StringBuilder sb = new StringBuilder();
    for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
    }
    return sb.toString();
}
```

La contraseña `password123` produce:
```
ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
```

Esto se guarda en la BD. Cuando el usuario hace login, se hashea el password recibido y se compara con el hash almacenado.

### 5.4 AuthController — el endpoint de login

`POST /api/auth/login` recibe las credenciales y devuelve el token:

```
Request:
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}

Response 200 OK:
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}

Response 401 Unauthorized:
{
  "error": "Credenciales incorrectas"
}
```

Internamente llama a `UsuarioServicio.login()` que:
1. Hashea la contraseña recibida
2. Busca en la BD por `username` + `passwordHash`
3. Si existe → llama a `JwtUtil.generarToken(username)` y devuelve el token
4. Si no existe → devuelve `null`

### 5.5 AuthFilter — el guardián de la API

El filtro intercepta **cada petición** a `/api/*` antes de que llegue al servlet:

```
Petición entrante a /api/tareas
           │
           ▼
    ┌─────────────┐
    │  AuthFilter │
    └──────┬──────┘
           │
           ├─ ¿Es la ruta /api/auth/login?  ──Sí──► dejar pasar (sin token)
           │
           ├─ ¿Tiene header "Authorization: Bearer <token>"?
           │     └─ No ──► responder 401 "Token no proporcionado"
           │
           ├─ ¿Es el token válido y no expirado?
           │     └─ No ──► responder 401 "Token inválido o expirado"
           │
           └─ Token OK ──► guardar username en req.setAttribute("username", username)
                           continuar hacia el servlet
```

```java
@WebFilter(urlPatterns = "/api/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 1. Dejar pasar el login sin verificar
        if ("/api/auth/login".equals(req.getServletPath())) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Verificar que el header exista y tenga el formato correcto
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            enviarNoAutorizado(res, "Token no proporcionado.");
            return;
        }

        // 3. Extraer el token (quitar "Bearer ")
        String token = authHeader.substring(7);

        try {
            // 4. Validar el token — si falla, lanza JwtException
            String username = JwtUtil.validarYObtenerUsername(token);

            // 5. Pasar el username al servlet como atributo del request
            req.setAttribute("username", username);
            chain.doFilter(request, response);

        } catch (JwtException e) {
            // 6. Token inválido o expirado
            enviarNoAutorizado(res, "Token invalido o expirado.");
        }
    }
}
```

### 5.6 Cómo el servlet lee el username

En `TareaController`, cada método obtiene el username del atributo del request (puesto por el filtro):

```java
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse res) {
    // El AuthFilter ya validó el token y puso el username aquí
    String username = (String) req.getAttribute("username");

    // Ahora podemos usarlo para filtrar solo las tareas del usuario
    escribirJson(res, tareaServicio.obtenerTodas(username));
}
```

---

## 6. Endpoints de la API

### Autenticación

| Método | URL | Auth | Descripción |
|--------|-----|------|-------------|
| POST | `/api/auth/login` | No | Iniciar sesión, recibe token |

**Request body:**
```json
{ "username": "admin", "password": "password123" }
```

**Response 200:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### Tareas (requieren token)

Todas las peticiones a estos endpoints deben incluir el header:
```
Authorization: Bearer <token>
```

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/tareas` | Listar tareas del usuario autenticado |
| GET | `/api/tareas/{id}` | Obtener una tarea (solo si es del usuario) |
| POST | `/api/tareas` | Crear tarea para el usuario autenticado |
| PUT | `/api/tareas/{id}` | Actualizar tarea (solo si es del usuario) |
| DELETE | `/api/tareas/{id}` | Eliminar tarea (solo si es del usuario) |

**POST/PUT request body:**
```json
{ "titulo": "Nueva tarea", "completada": false }
```

---

## 7. Flujo completo de una petición autenticada

```
Cliente (Angular/Postman)
        │
        │  POST /api/auth/login
        │  { "username": "admin", "password": "password123" }
        │
        ▼
   CORSFilter  →  AuthFilter (deja pasar login)  →  AuthController
                                                          │
                                                          │ hashea password
                                                          │ consulta BD
                                                          │ genera JWT
                                                          │
                                                          ▼
                                              { "token": "eyJ..." }
        │
        │  GET /api/tareas
        │  Authorization: Bearer eyJ...
        │
        ▼
   CORSFilter  →  AuthFilter
                      │
                      │ verifica firma del token
                      │ verifica expiración
                      │ extrae username → req.setAttribute("username", "admin")
                      │
                      ▼
               TareaController.doGet()
                      │
                      │ username = req.getAttribute("username")  → "admin"
                      │ tareaServicio.obtenerTodas("admin")
                      │   └─ resuelve usuarioId de "admin" → 1
                      │   └─ SELECT ... WHERE usuario_id = 1
                      │
                      ▼
               [{"id":1,"titulo":"Estudiar...","completada":false}, ...]
```

---

## 8. Cómo probar la API

### Con Postman o similar

**Paso 1: Obtener el token**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

Copia el valor de `"token"` de la respuesta.

**Paso 2: Usar el token en peticiones protegidas**
```
GET http://localhost:8080/api/tareas
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Con curl

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. Listar tareas
curl http://localhost:8080/api/tareas \
  -H "Authorization: Bearer $TOKEN"

# 3. Crear tarea
curl -X POST http://localhost:8080/api/tareas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Aprender JWT"}'

# 4. Actualizar tarea (reemplaza 1 con el ID real)
curl -X PUT http://localhost:8080/api/tareas/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Aprender JWT","completada":true}'

# 5. Eliminar tarea
curl -X DELETE http://localhost:8080/api/tareas/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Credenciales de ejemplo

| Username | Password | Descripción |
|----------|----------|-------------|
| `admin` | `password123` | Usuario administrador |
| `estudiante` | `password123` | Usuario de prueba |
