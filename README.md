# chess-software

Sistema de salas para jugar al ajedrez. Proyecto individual de **Ingeniería del Software II** (Escuela de Ingeniería Informática, ULPGC).

> 🚧 En desarrollo. Este README se va completando a medida que avanza el proyecto.

## Estado actual

| Funcionalidad | Estado |
|---|---|
| Reglas de movimiento de todas las piezas | ✅ |
| Jaque y movimientos que dejan el rey en jaque | ✅ |
| En passant | ✅ |
| Enroque (corto y largo) | ✅ |
| Promoción con elección de pieza | ✅ |
| Jaque mate y ahogado | ✅ |
| Tablas por material insuficiente y regla de los 50 movimientos | ✅ |
| Salas para dos jugadores: crear, unirse con código, un color por jugador | ✅ |
| Sincronización en tiempo real entre jugadores (WebSocket) | ✅ |
| Persistencia de partidas, salas e historial de movimientos (SQLite) | ✅ |
| Partida local (dos jugadores en la misma pantalla) | ✅ |
| Frontend web: inicio, tablero orientado por color, movimientos legales resaltados, selector de promoción | ✅ |
| Diseño final de la interfaz | ⏳ |
| Aportación propia | ⏳ |
| Tests automáticos | ⏳ |

## Estructura del repositorio

```
chess-software/
├── backend/     Java 27, Maven, Javalin, SQLite
└── frontend/    HTML + CSS + JavaScript (módulos ES, sin dependencias ni compilación)
```

## Arquitectura

Backend y frontend siguen una **arquitectura hexagonal** (puertos y adaptadores): el núcleo no depende de ningún framework ni de la tecnología de comunicación, y cada detalle técnico es un adaptador sustituible.

### Backend (`software.ulpgc.chess`)

```
domain/           Reglas del ajedrez y de las salas. Java puro, sin dependencias externas.
application/      Casos de uso y los puertos (interfaces) que necesitan.
infrastructure/   Adaptadores: HTTP y WebSocket (Javalin), persistencia (SQLite y memoria) y punto de entrada (Main).
```

- **`domain`**: modelo inmutable.
  - `Board` (piezas), `Position` (tablero + turno + derechos de enroque + casilla de en passant + contador de medios movimientos, el mismo contenido que una cadena FEN) y `Game` (identificador + posición + historial de `Move`).
  - Cada regla vive en una clase pequeña:
    - `KnightMovementRule`, `PawnMovementRule`… implementan `MovementRule`, y `MovementRules` despacha según el tipo de pieza.
    - `AttackDetector` sabe qué casillas están atacadas. `CheckDetector` lo usa para el jaque y `CastlingRule` para que el rey no enroque pasando por casillas atacadas.
    - `LegalMoveValidator` combina las reglas de movimiento, el enroque y la comprobación de que el propio rey no queda en jaque.
    - `LegalMoveGenerator` lista los movimientos legales. `GameStatusEvaluator` decide si la partida sigue, es mate, ahogado o tablas.
  - `Room`: código de sala, partida asociada y token secreto de cada jugador. Contiene las reglas de la sala: esperar al rival, solo juegan sus dos jugadores, cada uno en su turno.
- **`application`**:
  - Partidas: `CreateGameUseCase`, `GetGameUseCase`, `MovePieceUseCase`, `GetLegalMovesUseCase`.
  - Salas: `CreateRoomUseCase`, `JoinRoomUseCase`, `GetRoomUseCase`, `MoveInRoomUseCase`.
  - Puertos de salida: `GameRepository`, `RoomRepository` (persistencia) y `RoomNotifier` (avisar a los jugadores de que la sala ha cambiado).
- **`infrastructure`**:
  - `http/`: `GameController` y `RoomController` traducen HTTP ⇄ casos de uso, con DTOs (`GameResponse`, `RoomResponse`, `SessionResponse`, `MoveRequest`, `ErrorResponse`).
  - `websocket/WebSocketRoomNotifier` implementa `RoomNotifier`: mantiene las conexiones abiertas de cada sala y les envía el estado nuevo.
  - `persistence/`: `SqliteGameRepository` y `SqliteRoomRepository` (por defecto), e `InMemoryGameRepository` e `InMemoryRoomRepository`.
  - `Main` monta las piezas. Es el único sitio que conoce todas las implementaciones.

Los casos de uso no saben nada de HTTP, WebSocket, Javalin ni SQLite. **Cambiar la persistencia de SQLite a memoria es una variable de entorno (`STORAGE=memory`)** y no toca ni el dominio ni la aplicación.

### Frontend (`frontend/js`)

```
application/      Puertos (GameService, RoomService) y casos de uso (LocalGame, OnlineGame).
infrastructure/   Adaptadores: HTTP/WebSocket, mocks, almacenamiento de sesión y vistas del DOM.
main.js           Raíz de composición: elige los adaptadores y los conecta.
```

- **Puertos**:
  - `GameService` (`createGame`, `getGame`, `move`, `legalMoves`) para la partida local.
  - `RoomService` (`createRoom`, `joinRoom`, `getRoom`, `move`, `legalMoves`, `subscribe`) para jugar en sala.
- **Casos de uso**: `LocalGame` y `OnlineGame` orquestan cada modo de juego. No tocan el DOM: reciben el puerto y las vistas y solo llaman a sus métodos.
- **Adaptadores**:
  - Servicios reales: `HttpGameService` y `HttpRoomService`. Este último usa `fetch` y `WebSocket`, y se reconecta solo si la conexión se cae.
  - `MockGameService` y `MockRoomService`: en memoria y sin reglas, para trabajar en la interfaz sin backend.
  - Vistas: `BoardView`, `StatusView`, `LobbyView`, `RoomInfoView` y `PromotionDialog`.
  - `BrowserSessionStore`: guarda la sesión del jugador.
- **Para pasar a los mocks basta con poner `USE_MOCK = true` en `main.js`**.
- Las reglas del ajedrez **no se duplican en el frontend**. Los movimientos legales los calcula el backend, y lo que hace el frontend (seleccionar solo tus piezas en tu turno) es comodidad visual: el backend vuelve a validarlo todo.

## Cómo ejecutarlo

Requisitos: JDK 27 y Maven, o IntelliJ IDEA, que trae Maven integrado.

**1. Backend** (puerto 7070):

Ejecuta `software.ulpgc.chess.infrastructure.Main` desde IntelliJ, o bien:

```
cd backend
mvn compile exec:java -Dexec.mainClass=software.ulpgc.chess.infrastructure.Main
```

Variables de entorno opcionales:

| Variable | Por defecto | Descripción |
|---|---|---|
| `PORT` | `7070` | Puerto del servidor |
| `STORAGE` | (SQLite) | `memory` para no guardar nada en disco |
| `DATABASE` | `chess.db` | Ruta del archivo SQLite (relativa al directorio de trabajo) |

**2. Frontend** (puerto 8000). Los módulos ES no funcionan abriendo el HTML con doble clic (`file://`), así que hay que servirlo por HTTP. El JDK incluye `jwebserver`:

```
cd frontend
jwebserver
```

Abre http://localhost:8000.

**Para probar una sala tú solo**, abre dos pestañas. En una pulsa *Crear sala* y en la otra escribe el código y pulsa *Unirse*. La sesión se guarda por pestaña (`sessionStorage`), así que cada pestaña es un jugador distinto y recargar no te saca de la sala.

## API

### Partidas (modo local)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `POST` | `/games` | Crea una partida | 201 |
| `GET` | `/games/{id}` | Estado de una partida | 200, 404 |
| `GET` | `/games/{id}/moves?from=e2` | Casillas a las que puede moverse la pieza de `from` | 200, 400, 404 |
| `POST` | `/games/{id}/moves` | Realiza un movimiento | 200, 400, 404, 409, 422 |

### Salas

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `POST` | `/rooms` | Crea una sala. El creador juega con blancas | 201 |
| `POST` | `/rooms/{code}/players` | Se une a la sala como negras | 201, 404, 409 |
| `GET` | `/rooms/{code}` | Estado de la sala y su partida | 200, 404 |
| `POST` | `/rooms/{code}/moves` | Mueve. Requiere la cabecera `X-Player-Token` | 200, 400, 403, 404, 409, 422 |
| `WS` | `/rooms/{code}/events` | Recibe el estado de la sala cada vez que cambia | — |

Crear o unirse a una sala devuelve el token del jugador. **Es la única vez que se envía**: ninguna otra respuesta incluye tokens.

```json
{ "color": "White", "playerToken": "…", "room": { "…": "…" } }
```

Estado de una sala, que es también lo que se recibe por el WebSocket:

```json
{ "code": "K7Q2MX", "whiteJoined": true, "blackJoined": true, "game": { "…": "…" } }
```

### Formatos comunes

Cuerpo de un movimiento. `promotion` es opcional (`q`, `r`, `b` o `n`); si se omite, la promoción es a dama:

```json
{ "from": "e7", "to": "e8", "promotion": "n" }
```

Estado de una partida:

```json
{
  "id": "…",
  "sideToMove": "White",
  "pieces": { "e1": "K", "e8": "k", "…": "…" },
  "status": "InProgress",
  "winner": null,
  "moves": ["e2e4", "e7e5"]
}
```

- `pieces` usa notación FEN: mayúsculas para blancas y minúsculas para negras.
- `status` puede ser `InProgress`, `Checkmate`, `Stalemate`, `InsufficientMaterial` o `FiftyMoveRule`.
- `winner` solo tiene valor (`White` o `Black`) en caso de jaque mate.
- `moves` es el historial en notación UCI (origen + destino + pieza de promoción, por ejemplo `e7e8q`).

Códigos de error:

| Código | Significado |
|---|---|
| 400 | Datos mal formados (casilla o pieza de promoción inválidas) |
| 403 | No es tu turno, o el token no corresponde a ningún jugador de la sala |
| 404 | La partida o la sala no existen |
| 409 | La partida ha terminado, la sala está llena o todavía falta el rival |
| 422 | Movimiento ilegal |

## Persistencia

SQLite, en un único archivo (`chess.db`), con tres tablas:

| Tabla | Contenido |
|---|---|
| `games` | Una fila por partida, con su fecha de creación |
| `moves` | Una fila por movimiento: partida, número de medio movimiento, origen, destino, promoción y fecha |
| `rooms` | Una fila por sala: código, partida y tokens de cada jugador |

**El tablero no se guarda: se guarda la lista de movimientos** y la partida se reconstruye repitiéndolos desde la posición inicial (`Game.replay`). Esto tiene varias ventajas:
- el historial completo, con fecha de cada jugada, queda registrado (trazabilidad),
- no hace falta inventar un formato para guardar el tablero,
- y cada movimiento se vuelve a validar al cargar.

## Decisiones de diseño

- **Dominio inmutable**: mover devuelve una `Position` nueva en lugar de modificar la actual. Eso permite simular un movimiento para comprobar si deja el rey en jaque sin tener que deshacerlo.
- **El enroque no es una `MovementRule`**: `AttackDetector` reutiliza las reglas de movimiento para saber qué casillas están atacadas. Si el enroque fuera una regla más, comprobar ataques podría acabar comprobando enroques, que a su vez comprueban ataques. Además, un enroque nunca ataca. Por eso solo lo consulta `LegalMoveValidator`.
- **El estado de la partida se calcula, no se guarda**: `Game.status()` lo deduce de la posición, así que nunca puede quedar desincronizado.
- **Jugadores identificados por token**: al crear o unirse a una sala, el servidor genera un UUID aleatorio que solo conoce ese jugador. Así no hace falta un sistema de cuentas y nadie puede mover por otro.
- **WebSocket solo de bajada**: los movimientos se envían por HTTP, que ya valida, responde errores con su código y es fácil de probar. El WebSocket solo avisa de los cambios. El cliente, al conectarse o reconectarse, pide además el estado actual, por si se perdió algún aviso.
- **Casos de uso de sala `synchronized`**: dos peticiones simultáneas (por ejemplo, dos personas uniéndose a la vez) se procesan una detrás de otra.
- **Regla de los 50 movimientos automática**: en el reglamento FIDE es una tabla que se reclama. Aquí se aplica automáticamente para simplificar.
- **No implementado (por ahora)**: tablas por triple repetición.

## Próximos pasos

- Diseño final de la interfaz: indicador de turno, historial de movimientos visible, estilo.
- Aportación propia.
- Tests.
