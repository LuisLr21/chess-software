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
| API REST para crear partidas, consultarlas y mover | ✅ |
| Frontend web con tablero, movimientos legales resaltados y selector de promoción | ✅ |
| Salas para dos jugadores | ⏳ |
| Sincronización en tiempo real entre jugadores | ⏳ |
| Persistencia de partidas e historial de movimientos | ⏳ |
| Aportación propia | ⏳ |
| Tests automáticos | ⏳ |

## Estructura del repositorio

```
chess-software/
├── backend/     Java 27, Maven, Javalin
└── frontend/    HTML + CSS + JavaScript (módulos ES, sin dependencias ni compilación)
```

## Arquitectura

Backend y frontend siguen una **arquitectura hexagonal** (puertos y adaptadores): el núcleo no depende de ningún framework ni de la tecnología de comunicación, y cada detalle técnico es un adaptador sustituible.

### Backend (`software.ulpgc.chess`)

```
domain/           Reglas del ajedrez. Java puro, sin dependencias externas.
application/      Casos de uso y puertos (interfaces) que necesitan.
infrastructure/   Adaptadores: HTTP (Javalin), persistencia y punto de entrada (Main).
```

- **`domain`**: modelo inmutable. `Board` (piezas), `Position` (tablero + turno + derechos de enroque + casilla de en passant + contador de medios movimientos, el mismo contenido que una cadena FEN) y `Game` (partida con identificador). Cada regla vive en una clase pequeña:
  - `KnightMovementRule`, `PawnMovementRule`… implementan `MovementRule`, y `MovementRules` despacha según el tipo de pieza.
  - `AttackDetector` sabe qué casillas están atacadas. `CheckDetector` lo usa para el jaque y `CastlingRule` para que el rey no enroque pasando por casillas atacadas.
  - `LegalMoveValidator` combina las reglas de movimiento, el enroque y la comprobación de que el propio rey no queda en jaque.
  - `LegalMoveGenerator` lista los movimientos legales. `GameStatusEvaluator` decide si la partida sigue, es mate, ahogado o tablas.
- **`application`**: `CreateGameUseCase`, `GetGameUseCase`, `MovePieceUseCase` y `GetLegalMovesUseCase`. Dependen del puerto `GameRepository`, no de una implementación concreta.
- **`infrastructure`**:
  - `http/GameController` traduce HTTP ⇄ casos de uso, con DTOs (`GameResponse`, `MoveRequest`, `ErrorResponse`).
  - `persistence/InMemoryGameRepository` implementa `GameRepository`.
  - `Main` monta las piezas. Es el único sitio que conoce todas las implementaciones.

Los casos de uso no saben nada de HTTP ni de Javalin. Cambiar de framework o de forma de persistir solo implica escribir otro adaptador.

### Frontend (`frontend/js`)

```
application/      Puerto GameService: lo que el frontend necesita del backend.
infrastructure/   Adaptadores: HttpGameService, MockGameService y las vistas (BoardView, StatusView, PromotionDialog).
main.js           Raíz de composición: elige los adaptadores y los conecta.
```

- `GameService` define el contrato: `createGame`, `getGame`, `move` y `legalMoves`.
- `HttpGameService` lo implementa llamando a la API real. `MockGameService` lo implementa en memoria, sin reglas, para trabajar en la interfaz sin backend.
- **Para cambiar de adaptador basta con modificar una línea en `main.js`**. Las vistas no conocen al servicio: solo pintan el estado y avisan de las acciones del usuario mediante callbacks.
- Las reglas del ajedrez **no se duplican en el frontend**. Los puntos de movimientos legales los calcula el backend (`GET /games/{id}/moves?from=e2`).

## Cómo ejecutarlo

Requisitos: JDK 27 y Maven, o IntelliJ IDEA, que trae Maven integrado.

**1. Backend** (puerto 7070 por defecto, configurable con la variable de entorno `PORT`):

Ejecuta `software.ulpgc.chess.infrastructure.Main` desde IntelliJ, o bien:

```
cd backend
mvn compile exec:java -Dexec.mainClass=software.ulpgc.chess.infrastructure.Main
```

**2. Frontend** (puerto 8000). Los módulos ES no funcionan abriendo el HTML con doble clic (`file://`), así que hay que servirlo por HTTP. El JDK incluye `jwebserver`:

```
cd frontend
jwebserver
```

Abre http://localhost:8000.

## API REST

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `POST` | `/games` | Crea una partida | 201 |
| `GET` | `/games/{id}` | Estado de una partida | 200, 404 |
| `GET` | `/games/{id}/moves?from=e2` | Casillas a las que puede moverse la pieza de `from` | 200, 400, 404 |
| `POST` | `/games/{id}/moves` | Realiza un movimiento | 200, 400, 404, 409, 422 |

Cuerpo de `POST /games/{id}/moves`. `promotion` es opcional (`q`, `r`, `b` o `n`); si se omite, la promoción es a dama:

```json
{ "from": "e7", "to": "e8", "promotion": "n" }
```

Respuesta de una partida:

```json
{
  "id": "…",
  "sideToMove": "White",
  "pieces": { "e1": "K", "e8": "k", "…": "…" },
  "status": "InProgress",
  "winner": null
}
```

- `pieces` usa notación FEN: mayúsculas para blancas y minúsculas para negras.
- `status` puede ser `InProgress`, `Checkmate`, `Stalemate`, `InsufficientMaterial` o `FiftyMoveRule`.
- `winner` solo tiene valor (`White` o `Black`) en caso de jaque mate.

Códigos de error: 400 para datos mal formados (casilla o pieza de promoción inválidas), 404 si la partida no existe, 409 si la partida ya ha terminado y 422 si el movimiento es ilegal.

## Decisiones de diseño

- **Dominio inmutable**: mover devuelve una `Position` nueva en lugar de modificar la actual. Eso permite simular un movimiento para comprobar si deja el rey en jaque sin tener que deshacerlo.
- **El enroque no es una `MovementRule`**: `AttackDetector` reutiliza las reglas de movimiento para saber qué casillas están atacadas. Si el enroque fuera una regla más, comprobar ataques podría acabar comprobando enroques, que a su vez comprueban ataques. Además, un enroque nunca ataca. Por eso solo lo consulta `LegalMoveValidator`.
- **El estado de la partida se calcula, no se guarda**: `Game.status()` lo deduce de la posición, así que nunca puede quedar desincronizado.
- **Regla de los 50 movimientos automática**: en el reglamento FIDE es una tabla que se reclama. Aquí se aplica automáticamente para simplificar.
- **No implementado (por ahora)**: tablas por triple repetición.

## Próximos pasos

- Salas: crear una sala, unirse con un código y asignar color a cada jugador.
- Tiempo real: que cada jugador vea los movimientos del rival sin recargar.
- Persistencia: un adaptador de `GameRepository` que guarde las partidas y su historial.
- Interfaz: pantalla de inicio, indicador de turno, historial de movimientos y diseño final.
- Aportación propia.
- Tests.
