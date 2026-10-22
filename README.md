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
| Persistencia de partidas, salas, historial y chat (SQLite) | ✅ |
| Partida local (dos jugadores en la misma pantalla) | ✅ |
| Nombres de jugador y presencia (conectado / desconectado) | ✅ |
| Reloj por jugador controlado por el servidor (sin reloj, 3, 5, 10 o 15 minutos) | ✅ |
| Rendirse, ofrecer / aceptar / rechazar tablas, revancha con cambio de colores | ✅ |
| Historial en notación SAN y exportación a PGN | ✅ |
| Piezas capturadas y ventaja de material | ✅ |
| Chat entre los dos jugadores | ✅ |
| Interfaz final retro 16-bit: piezas pixel-art propias, sonidos 8-bit | ✅ |
| Aportación propia: jugar contra la IA (fácil, medio y difícil; con blancas o negras) | ✅ |
| Tests automáticos (JUnit 6, 182 tests) | ✅ |

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
  - `Board` (piezas), `Position` (tablero + turno + derechos de enroque + casilla de en passant + contador de medios movimientos, el mismo contenido que una cadena FEN).
  - `Game`: identificador + posición + historial de `PlayedMove` (movimiento, su notación SAN y la pieza capturada) + `Ending` (final forzado: rendición, tablas acordadas o tiempo) + `ChessClock`.
  - `ChessClock`: tiempo restante de cada jugador y momento en que empezó el turno actual. Recibe siempre el instante actual como parámetro, así que no depende del reloj del sistema.
  - `SanFormatter`: traduce un movimiento a notación algebraica estándar (`Nf3`, `exd5`, `O-O`, `e8=Q+`, desambiguación `Nbd2`…).
  - Cada regla vive en una clase pequeña:
    - `KnightMovementRule`, `PawnMovementRule`… implementan `MovementRule`, y `MovementRules` despacha según el tipo de pieza.
    - `AttackDetector` sabe qué casillas están atacadas. `CheckDetector` lo usa para el jaque y `CastlingRule` para que el rey no enroque pasando por casillas atacadas.
    - `LegalMoveValidator` combina las reglas de movimiento, el enroque y la comprobación de que el propio rey no queda en jaque.
    - `LegalMoveGenerator` lista los movimientos legales. `GameStatusEvaluator` decide si la partida sigue, es mate, ahogado o tablas.
  - `ai/Engine`: un motor de IA recibe una `Position` y elige un `Move`. Cada nivel es una implementación (patrón Strategy). `LegalMoveGenerator.allLegalMoves` le da todos los movimientos legales, promociones incluidas.
    - `RandomEngine` (fácil): un movimiento legal al azar.
    - `GreedyEngine` (medio): la captura de más valor; si no hay ninguna, al azar.
    - `MinimaxEngine` (difícil): minimax con poda alfa-beta a profundidad 3. `PositionEvaluator` puntúa las posiciones por material (`PieceValues`, en centésimas de peón), el mate vale más cuanto antes llegue y las tablas valen 0.
  - `Room`: código de sala, partida asociada, los dos `Seat` (token secreto + nombre) y las ofertas pendientes de tablas y de revancha. Contiene las reglas de la sala: esperar al rival, solo juegan sus dos jugadores, cada uno en su turno, y la revancha intercambia los colores.
  - `ChatMessage`: mensaje de un jugador (máximo 200 caracteres, nunca vacío).
- **`application`**:
  - Partidas: `CreateGameUseCase`, `GetGameUseCase`, `MovePieceUseCase`, `GetLegalMovesUseCase`.
  - Salas: `CreateRoomUseCase`, `JoinRoomUseCase`, `GetRoomUseCase`, `MoveInRoomUseCase`, `ResignUseCase`, `OfferDrawUseCase` (si el rival ya las había ofrecido, las acepta), `DeclineDrawUseCase`, `RematchUseCase`.
  - Chat: `SendMessageUseCase`, `GetMessagesUseCase`.
  - IA: `PlayAiMoveUseCase`, `GetAiLevelsUseCase`. Los motores se registran en `Main` con su nombre de nivel.
  - Puertos de salida: `GameRepository`, `RoomRepository`, `ChatRepository` (persistencia) y `RoomNotifier` (avisar a los jugadores de cambios en la sala y de mensajes nuevos).
  - Los casos de uso que dependen de la hora reciben un `java.time.Clock`, para poder usar un reloj fijo en los tests.
- **`infrastructure`**:
  - `http/`: `GameController` y `RoomController` traducen HTTP ⇄ casos de uso, con DTOs (`GameResponse`, `RoomResponse`, `ClockResponse`, `PlayerResponse`, `SessionResponse`, `ChatMessageResponse`…).
  - `websocket/WebSocketRoomNotifier` implementa `RoomNotifier`: mantiene las conexiones abiertas de cada sala, sabe de qué jugador es cada una (por su token) y envía tres tipos de evento: sala, presencia y chat.
  - `persistence/`: `SqliteGameRepository`, `SqliteRoomRepository` y `SqliteChatRepository` (por defecto), y sus equivalentes en memoria.
  - `Main` monta las piezas. Es el único sitio que conoce todas las implementaciones.

Los casos de uso no saben nada de HTTP, WebSocket, Javalin ni SQLite. **Cambiar la persistencia de SQLite a memoria es una variable de entorno (`STORAGE=memory`)** y no toca ni el dominio ni la aplicación.

### Frontend (`frontend/js`)

```
application/      Puertos (GameService, RoomService), casos de uso (LocalGame, OnlineGame) y generación de PGN.
infrastructure/   Adaptadores: HTTP/WebSocket, mocks, almacenamiento en el navegador, vistas del DOM y sonido.
main.js           Raíz de composición: elige los adaptadores y los conecta.
```

- **Puertos**:
  - `GameService` (`createGame`, `getGame`, `move`, `legalMoves`) para la partida local.
  - `RoomService` (`createRoom`, `joinRoom`, `getRoom`, `move`, `legalMoves`, `resign`, `offerDraw`, `declineDraw`, `rematch`, `messages`, `sendMessage`, `subscribe`) para jugar en sala.
- **Casos de uso**: `LocalGame` y `OnlineGame` orquestan cada modo de juego. No tocan el DOM: construyen un objeto `match` con todo lo que hay que mostrar (jugadores, partida, reloj, ofertas…) y se lo pasan a la pantalla, que es otro puerto (`showMatch`, `showHints`, `showMessages`, `addMessage`, `showError`).
- **Adaptadores**:
  - Servicios reales: `HttpGameService` y `HttpRoomService`. Este último usa `fetch` y `WebSocket`, y se reconecta solo si la conexión se cae.
  - `MockGameService` y `MockRoomService`: en memoria y sin reglas, para trabajar en la interfaz sin backend.
  - `GameScreen`: fachada de la pantalla de juego. Reparte el `match` entre vistas pequeñas (`BoardView`, `PlayersView`, `OptionsView`, `GraveyardView`, `RoomBarView`, `TurnBannerView`, `ClockView`, `HistoryView`, `ChatView`) y decide qué sonido suena (`SoundEffects`).
  - `LobbyView` (pantalla de inicio) y `PromotionDialog`.
  - `PixelPieces`: las piezas son dibujos de 16×16 píxeles escritos como texto, que se convierten en SVG.
  - `BrowserSessionStore` (sesión del jugador, por pestaña) y `BrowserPreferences` (nombre, tiempo elegido y sonido, recordados entre visitas).
- **Para pasar a los mocks basta con poner `USE_MOCK = true` en `main.js`**.
- Las reglas del ajedrez **no se duplican en el frontend**. Los movimientos legales, el SAN, las capturas y el tiempo los calcula el backend. Lo que hace el frontend (seleccionar solo tus piezas en tu turno, descontar el reloj en pantalla) es comodidad visual: el backend vuelve a validarlo todo.

## Cómo ejecutarlo

Requisitos: JDK 27 y Maven, o IntelliJ IDEA, que trae Maven integrado.

El backend sirve también el frontend, así que basta con arrancar un único proceso **desde la raíz del repositorio**. Ejecuta `software.ulpgc.chess.infrastructure.Main` desde IntelliJ, o bien:

```
mvn -f backend/pom.xml compile exec:java -Dexec.mainClass=software.ulpgc.chess.infrastructure.Main
```

Abre http://localhost:7070.

Variables de entorno opcionales:

| Variable | Por defecto | Descripción |
|---|---|---|
| `PORT` | `7070` | Puerto del servidor |
| `STORAGE` | (SQLite) | `memory` para no guardar nada en disco |
| `DATABASE` | `chess.db` | Ruta del archivo SQLite (relativa al directorio de trabajo) |
| `FRONTEND` | `frontend` | Carpeta del frontend que sirve Javalin (relativa al directorio de trabajo) |

Javalin sirve los ficheros del frontend con `config.staticFiles` (`Location.EXTERNAL`). La página y la API comparten origen, así que no hace falta CORS y el frontend usa `location.origin` como URL de la API. Los módulos ES no funcionan abriendo el HTML con doble clic (`file://`); siempre hay que entrar por http://localhost:7070.

**Para probar una sala tú solo**, abre dos pestañas. En una elige *Multijugador*, el tiempo y pulsa *Crear sala*. En la otra escribe el código y pulsa *Unirse*. La sesión se guarda por pestaña (`sessionStorage`), así que cada pestaña es un jugador distinto y recargar no te saca de la sala.

## Tests

```
cd backend
mvn test
```

o, en IntelliJ, click derecho en `backend/src/test/java` → *Run 'All Tests'*.

Los tests están en `backend/src/test/java`, en el mismo paquete que la clase que prueban:

| Capa | Qué se prueba |
|---|---|
| `domain` | Movimiento de cada pieza, jaque y clavadas, enroque, en passant, promoción, mate, ahogado, material insuficiente, regla de los 50 movimientos, SAN, reloj, reglas de la sala, chat |
| `domain.ai` | Random siempre juega un movimiento legal, greedy captura la pieza de más valor, minimax encuentra el mate en una (con blancas y con negras) y no regala la dama |
| `application` | Casos de uso con repositorios en memoria: crear y unirse a salas, turnos, tiempo agotado, tablas, rendición, revancha, chat, partidas locales e IA |
| `infrastructure.persistence` | Los repositorios SQLite guardan y recuperan partidas (con promociones, final y reloj), salas y mensajes, sobre una base de datos temporal |

Ayudantes de test:
- `Positions` monta posiciones a partir de una lista de piezas (`"Ke1"`, `"ra8"`: mayúsculas blancas, minúsculas negras).
- `Games` juega una partida a partir de movimientos en notación UCI (`"e2e4"`, `"a7b8n"`).
- `MutableClock` es un reloj que se adelanta a mano, para probar el tiempo sin esperar.
- `RecordingNotifier` apunta los avisos que se enviarían a los jugadores.
- `RoomFixture` monta todos los casos de uso de salas con repositorios en memoria.

El frontend no tiene tests automáticos: no usa herramientas de compilación ni dependencias, y toda la lógica del juego está en el backend.

## La interfaz

Estilo retro 16-bit en tres columnas (en pantallas estrechas se apilan, con el tablero arriba):

- **Izquierda**: jugadores con su nombre y si están conectados; opciones de la partida (modo, tiempo, sonido) y botones de *Revancha*, *Pedir tablas*, *Rendirse* (pide confirmación), *Mute* y *Salir*, o *Nueva partida* en modo local; y el cementerio con las piezas capturadas por cada bando y la ventaja de material.
- **Centro**: código de sala con botón para copiarlo, el tablero (orientado según tu color, con la última jugada, el jaque y los movimientos legales resaltados) y una franja que dice de quién es el turno, las ofertas del rival (con botones para aceptar o rechazar) o el resultado.
- **Derecha**: relojes, historial en SAN con descarga en PGN, y chat.

Las piezas son pixel-art propio y los sonidos se generan con la Web Audio API (onda cuadrada), sin archivos de audio. Las fuentes son *Press Start 2P* y *VT323* (Google Fonts); el resto es CSS propio, sin frameworks.

## API

### Partidas (modo local)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `POST` | `/games` | Crea una partida | 201 |
| `GET` | `/games/{id}` | Estado de una partida | 200, 404 |
| `GET` | `/games/{id}/moves?from=e2` | Casillas a las que puede moverse la pieza de `from` | 200, 400, 404 |
| `POST` | `/games/{id}/moves` | Realiza un movimiento | 200, 400, 404, 409, 422 |
| `POST` | `/games/{id}/ai-move?level=random` | La IA del nivel indicado juega el movimiento del bando que mueve | 200, 400, 404, 409 |
| `GET` | `/ai/levels` | Niveles de IA disponibles, en orden | 200 |

### Salas

Las rutas marcadas con 🔑 requieren la cabecera `X-Player-Token`.

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `POST` | `/rooms` | Crea una sala. El creador juega con blancas. Cuerpo opcional: `{ "name": "Luis", "minutes": 5 }` (0 = sin reloj, máximo 60) | 201, 400 |
| `POST` | `/rooms/{code}/players` | Se une a la sala como negras. Cuerpo opcional: `{ "name": "Ana" }`. Arranca el reloj | 201, 404, 409 |
| `GET` | `/rooms/{code}` | Estado de la sala. Con el token, `you` indica tu color | 200, 404 |
| `POST` | `/rooms/{code}/moves` 🔑 | Mueve | 200, 400, 403, 404, 409, 422 |
| `POST` | `/rooms/{code}/resign` 🔑 | Se rinde | 200, 403, 404, 409 |
| `POST` | `/rooms/{code}/draw-offer` 🔑 | Ofrece tablas, o las acepta si el rival ya las había ofrecido | 200, 403, 404, 409 |
| `DELETE` | `/rooms/{code}/draw-offer` 🔑 | Rechaza (o retira) la oferta de tablas | 200, 403, 404 |
| `POST` | `/rooms/{code}/rematch` 🔑 | Pide revancha; cuando la piden los dos empieza una partida nueva con los colores cambiados | 200, 403, 404, 409 |
| `GET` | `/rooms/{code}/messages` | Mensajes del chat | 200, 404 |
| `POST` | `/rooms/{code}/messages` 🔑 | Envía un mensaje: `{ "text": "hola" }` | 201, 400, 403, 404 |
| `WS` | `/rooms/{code}/events?token=…` | Eventos de la sala (ver abajo) | — |

Crear o unirse a una sala devuelve el token del jugador. **Es la única vez que se envía**: ninguna otra respuesta incluye tokens.

```json
{ "color": "White", "playerToken": "…", "room": { "…": "…" } }
```

Estado de una sala:

```json
{
  "code": "K7Q2MX",
  "you": "White",
  "white": { "name": "Luis", "joined": true },
  "black": { "name": "Ana", "joined": true },
  "drawOfferedBy": null,
  "rematchOfferedBy": null,
  "clock": { "initialMillis": 300000, "whiteMillis": 287450, "blackMillis": 300000, "running": "White" },
  "game": { "…": "…" }
}
```

- `you` es el color de quien hace la petición (según su token), o `null` para un espectador. Hace falta porque la revancha intercambia los colores.
- `clock` es `null` en las salas sin reloj. Los tiempos están calculados en el momento de responder y `running` dice qué reloj corre (`null` si está parado). Si a un jugador se le acaba el tiempo, la siguiente petición a la sala cierra la partida con `Timeout`.

Eventos del WebSocket. El token va en la URL porque un WebSocket del navegador no puede llevar cabeceras propias; sirve para saber qué jugador está conectado.

```json
{ "type": "room", "room": { "…": "…" } }
{ "type": "presence", "white": true, "black": false }
{ "type": "chat", "message": { "author": "White", "authorName": "Luis", "text": "¡Suerte!", "sentAt": "2026-10-21T16:00:00Z" } }
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
  "inCheck": false,
  "moves": ["e2e4", "d7d5", "e4d5"],
  "san": ["e4", "d5", "exd5"],
  "captured": { "White": ["p"], "Black": [] }
}
```

- `pieces` usa notación FEN: mayúsculas para blancas y minúsculas para negras.
- `status` puede ser `InProgress`, `Checkmate`, `Stalemate`, `InsufficientMaterial`, `FiftyMoveRule`, `Resignation`, `DrawByAgreement` o `Timeout`.
- `winner` tiene valor (`White` o `Black`) en jaque mate, rendición y tiempo agotado.
- `inCheck` indica si el bando que mueve está en jaque.
- `moves` es el historial en notación UCI (origen + destino + pieza de promoción, por ejemplo `e7e8q`) y `san` el mismo historial en notación algebraica estándar.
- `captured` son las piezas que ha capturado cada bando.

Códigos de error:

| Código | Significado |
|---|---|
| 400 | Datos mal formados (casilla o pieza de promoción inválidas, mensaje vacío o demasiado largo, minutos fuera de rango) |
| 403 | No es tu turno, o el token no corresponde a ningún jugador de la sala |
| 404 | La partida o la sala no existen |
| 409 | La partida ha terminado (o todavía no, en el caso de la revancha), la sala está llena o todavía falta el rival |
| 422 | Movimiento ilegal |

## Persistencia

SQLite, en un único archivo (`chess.db`), con cuatro tablas:

| Tabla | Contenido |
|---|---|
| `games` | Una fila por partida: fecha de creación, final forzado (rendición, tablas acordadas o tiempo) y estado del reloj |
| `moves` | Una fila por movimiento: partida, número de medio movimiento, origen, destino, promoción y fecha |
| `rooms` | Una fila por sala: código, partida actual, token y nombre de cada jugador, y ofertas pendientes |
| `messages` | Una fila por mensaje del chat |

**El tablero no se guarda: se guarda la lista de movimientos** y la partida se reconstruye repitiéndolos desde la posición inicial (`Game.restore`). Esto tiene varias ventajas:
- el historial completo, con fecha de cada jugada, queda registrado (trazabilidad),
- no hace falta inventar un formato para guardar el tablero,
- y cada movimiento se vuelve a validar al cargar.

Al arrancar se crean las tablas que falten y se añaden las columnas nuevas a una base de datos de una versión anterior (`ALTER TABLE … ADD COLUMN`), así que un `chess.db` antiguo sigue funcionando.

## Decisiones de diseño

- **Dominio inmutable**: mover devuelve una `Position` nueva en lugar de modificar la actual. Eso permite simular un movimiento para comprobar si deja el rey en jaque sin tener que deshacerlo.
- **El enroque no es una `MovementRule`**: `AttackDetector` reutiliza las reglas de movimiento para saber qué casillas están atacadas. Si el enroque fuera una regla más, comprobar ataques podría acabar comprobando enroques, que a su vez comprueban ataques. Además, un enroque nunca ataca. Por eso solo lo consulta `LegalMoveValidator`.
- **El estado de la partida se calcula**: `Game.status()` lo deduce de la posición (mate, ahogado, tablas…). Solo se guardan los finales que no se pueden deducir del tablero: rendición, tablas acordadas y tiempo agotado.
- **El reloj lo lleva el servidor**: se guarda cuánto le queda a cada jugador y cuándo empezó el turno, y el tiempo restante se calcula al momento. El navegador solo lo descuenta en pantalla y, cuando llega a cero, vuelve a pedir la sala para que el servidor lo compruebe. Así nadie puede hacer trampas con el reloj de su ordenador.
- **Jugadores identificados por token**: al crear o unirse a una sala, el servidor genera un UUID aleatorio que solo conoce ese jugador. Así no hace falta un sistema de cuentas y nadie puede mover por otro.
- **WebSocket solo de bajada**: los movimientos, las ofertas y los mensajes se envían por HTTP, que ya valida, responde errores con su código y es fácil de probar. El WebSocket solo avisa de los cambios. El cliente, al conectarse o reconectarse, pide además el estado actual, por si se perdió algún aviso.
- **Casos de uso de sala `synchronized`**: dos peticiones simultáneas (por ejemplo, dos personas uniéndose a la vez) se procesan una detrás de otra.
- **Regla de los 50 movimientos automática**: en el reglamento FIDE es una tabla que se reclama. Aquí se aplica automáticamente para simplificar.
- **Reloj solo en partidas online**: la partida local no tiene reloj.
- **IA con profundidad fija**: minimax no puede recorrer todo el árbol del ajedrez, como sí se puede en el 3 en raya. Busca 3 medios movimientos y evalúa la posición a la que llega. Con la poda alfa-beta, cada jugada tarda del orden de 100 ms; con profundidad 4 serían unos 600 ms en mitad de partida.
- **No implementado (por ahora)**: tablas por triple repetición.
