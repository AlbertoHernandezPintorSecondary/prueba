# Prueba técnica · Rick and Morty

App Android (Kotlin + Jetpack Compose) que busca personajes en la [Rick and Morty API](https://rickandmortyapi.com/) con paginación y muestra su detalle.

## Funcionalidad

- **Búsqueda**: campo de texto con *debounce* (400 ms). También se puede buscar al momento con la acción "Buscar" del teclado.
- **Paginación** (scroll infinito): al acercarse al final de la lista se pide la siguiente página (20 resultados por página). Si una página falla, aparece un pie con "Reintentar" sin perder lo ya cargado.
- **Cancelación**: si se lanza una búsqueda nueva mientras otra (o una página siguiente) está en curso, se cancela, incluida la petición HTTP. Sus resultados nunca llegan a la pantalla.
- **Empty state en 404**: la API responde 404 cuando no hay resultados (o el personaje no existe). En ambos casos se muestra un *empty state*, no un error.
- **Detalle**: imagen, nombre, estado, especie, tipo, género, origen, última ubicación y número de episodios.

## Arquitectura

Clean Architecture con **un módulo Gradle por capa**:

```
:app  ──►  :presentation  ──►  :domain  ◄──  :data
  └──────────────────────────────┴─────────────┘
       (app conoce todos los módulos solo para que Hilt los ensamble)
```

| Módulo | Tipo | Contenido |
|---|---|---|
| `:app` | Android application | `@HiltAndroidApp`, `MainActivity` (`@AndroidEntryPoint`). Nada de lógica ni de UI propia. |
| `:presentation` | Android library | Compose, ViewModels MVI (`@HiltViewModel`), modelos de UI (`CharacterUi`) y su mapper, navegación type-safe, tema, recursos. Depende solo de `:domain`. |
| `:domain` | Kotlin puro (JVM) | `RickAndMortyCharacter`, `CharacterPage`, `CharacterRepository` (interfaz), casos de uso, `DomainResult` y `DomainError`. Su única dependencia son las anotaciones estándar `javax.inject`. Expone además *test fixtures* compartidos (`src/testFixtures`). |
| `:data` | Kotlin puro (JVM) | `CharacterRemoteDataSource` (Retrofit + kotlinx.serialization, DTOs), `CharacterLocalDataSource` (cache en memoria con TTL + LRU), `CharacterRepositoryImpl` (coordina ambas y mapea DTO → dominio) y los módulos Hilt (`NetworkModule`, `CacheModule`, `DispatcherModule`, `RepositoryModule`). |

`:presentation` no conoce `:data`. La implementación del repositorio se inyecta por Hilt a través de la interfaz de `:domain`.

### Modelos por capa

Cada capa tiene sus modelos y el mapeo se hace en la frontera:

```
API (JSON) ─► CharacterDto ──[CharacterRepositoryImpl]──► RickAndMortyCharacter ──[ViewModel]──► CharacterUi ─► Compose
                 (data)                                        (domain)                            (presentation)
```

- **DTO → dominio en el repositorio** (`data/mapper/CharacterMapper.kt`). Por encima del repositorio nadie ve un DTO.
- **Dominio → UI en el ViewModel**, en cuanto responde el caso de uso (`presentation/common/mapper/CharacterUiMapper.kt`). El estado y los composables solo manejan `CharacterUi`, que ya viene preparado para pintar: estado con su texto y color (`CharacterStatusUi`), y `type` a `null` cuando no hay tipo, para ocultar esa fila. Un cambio en el dominio no toca la UI.

### Inyección de dependencias: Hilt

- `:data` usa `hilt-core` + KSP (es un módulo JVM, sin Android). Contiene `@Module`s que proveen OkHttp, Retrofit y las caches, y que hacen `@Binds` de `CharacterRepositoryImpl` → `CharacterRepository`.
- Los casos de uso de `:domain` usan `@Inject constructor`.
- `:presentation` usa `@HiltViewModel`. Las pantallas obtienen el ViewModel con `hiltViewModel()`, y el detalle lee el id del `SavedStateHandle`.
- Versiones: Hilt 2.60.1 y KSP 2.3.6 (las primeras compatibles con AGP 9 y su Kotlin integrado).

### Presentación: MVI

Cada pantalla define un contrato (`SearchContract.kt`, `DetailContract.kt`) con:

- **Intent**: acciones del usuario (`QueryChanged`, `SubmitSearch`, `LoadNextPage`, `Retry`, `CharacterClicked`…). Todas entran por `onIntent()`.
- **State**: un único estado inmutable expuesto como `StateFlow`. Solo cambia mediante un *reducer* (`setState { copy(...) }`).
- **Effect**: eventos de un solo uso, como la navegación, emitidos por un `Channel`.

`BaseViewModel<I, S, E>` es la base común. Cada pantalla tiene un `XxxRoute` con estado (obtiene el ViewModel y recoge los efectos) y un `XxxScreen` sin estado (pinta el `State` y emite `Intent`s).

**Previews**: cada composable de UI tiene su `@Preview`, y `SearchScreen`/`DetailScreen` las tienen para todos sus estados (`PreviewParameterProvider`). Los componentes compartidos están en `common/components`, un fichero por composable. Los únicos composables sin preview son los de cableado, que no pintan UI propia y necesitan un ViewModel real de Hilt: `SearchRoute`, `DetailRoute`, `AppNavHost` y `CollectEffects`.

### Concurrencia y cancelación

`SearchViewModel` mantiene como mucho un `Job` activo (primera página o página siguiente). Cada búsqueda nueva:

1. Cancela el job anterior. Las funciones `suspend` de Retrofit son cancelables, así que también se cancela la llamada de OkHttp.
2. Limpia los resultados (estado `Loading`).
3. Antes de publicar el resultado, comprueba `ensureActive()` como defensa extra.

El data source **relanza siempre `CancellationException`**. Es el único sitio donde se capturan excepciones; de ahí hacia arriba los errores son valores (ver "Errores tipados").

**Búsquedas equivalentes**: `normalizeSearchQuery()` (domain) es la única definición de "misma búsqueda" (`trim` + minúsculas). La usan el caso de uso, el repositorio (clave de cache) y el ViewModel. Si el texto cambia pero la búsqueda normalizada es la misma («rick» → «Rick »), no se cancela nada, no aparece el spinner y no se vuelve a buscar.

**Muerte del proceso**: la query se guarda en `SavedStateHandle`. Si Android mata la app en segundo plano, al volver aparece el mismo texto y se relanza la primera página sin debounce.

**Diagnóstico**: cuando una petición falla (salvo el 404, que es un empty state), los ViewModels registran el error y su excepción original a través de un `Logger` inyectado. En la app es `AndroidLogger` (Logcat, tags `SearchViewModel` y `DetailViewModel`); en los tests, un `FakeLogger` que permite comprobar qué se ha registrado. Así los ViewModels no dependen de `android.util.Log`. El pie de la paginación muestra además el motivo concreto (sin conexión, error del servidor…).

### Errores tipados

Los errores no viajan como excepciones, sino como valores:

```kotlin
sealed interface DomainResult<out T> { Success(data) ; Failure(error: DomainError) }
sealed interface DomainError { Network(cause) ; NotFound ; Server(code) ; Unknown(cause) }
```

- `RetrofitCharacterRemoteDataSource` es el **único sitio que captura excepciones**: `IOException` → `Network`, HTTP 404 → `NotFound`, otro HTTP → `Server(code)`, respuesta inesperada → `Unknown`. La `CancellationException` se relanza.
- El repositorio y los casos de uso devuelven `DomainResult`, y el repositorio encadena con `map` / `onSuccess`.
- Los ViewModels usan `when` **exhaustivos** sobre `DomainError`. Si mañana se añade un tipo de error, el proyecto no compila hasta tratarlo en cada pantalla. `NotFound` → empty state; el resto → error con su mensaje.
- `cause` conserva la excepción original solo para el log.

Comparado con `kotlin.Result`, cuyo fallo es cualquier `Throwable`: el tipo de error es conocido, no hace falta capturar excepciones en el dominio, y una cancelación nunca se confunde con un error.

### Rendimiento en Compose

- Los composables solo reciben modelos de UI (`CharacterUi`), definidos en `:presentation` y marcados como `@Immutable`, igual que los estados (`SearchState`, `SearchContent`, `DetailState`). Así Compose puede saltarse la recomposición cuando no cambian, sin necesidad de un fichero de configuración de estabilidad para el dominio.
- La `LazyColumn` usa `key` (id del personaje) y `contentType` (personaje / pie), para reutilizar composiciones solo entre elementos del mismo tipo.

### Paginación

- `CharacterPage(characters, nextPage)`: al mapear la respuesta, el repositorio traduce `info.next` de la API a un número de página.
- La lista calcula si se está cerca del final (umbral de 5 elementos) y emite `LoadNextPage`. El ViewModel ignora peticiones duplicadas o si no hay más páginas, y une los resultados sin duplicados (`distinctBy { id }`).
- Si falla una página, se conserva lo cargado y se muestra "Reintentar" en el pie. La carga automática se pausa hasta que el usuario reintenta.

### Cache

`InMemoryCache<K, V>` es thread-safe y tiene:

- **TTL de 5 minutos**, con un reloj inyectable (`TimeProvider`) para testear la expiración. Las entradas caducadas se borran al leerlas y también en cada escritura, así no se acumulan.
- **Tamaño máximo con política LRU**: 50 páginas de búsqueda y 1000 personajes. Cuando se llena, se expulsa la entrada usada hace más tiempo, así que la memoria está acotada aunque el usuario haga muchas búsquedas.

### Repositorio y fuentes de datos

`CharacterRepositoryImpl` solo coordina dos abstracciones:

- **`CharacterRemoteDataSource`** devuelve los **modelos de respuesta de la API (DTOs)** tal cual, dentro de un `DomainResult`. Solo traduce excepciones de red y HTTP a `DomainError`.
- **`CharacterLocalDataSource`** guarda modelos de dominio ya mapeados. La implementación actual, `InMemoryCharacterLocalDataSource`, usa las dos `InMemoryCache`. Como sus funciones son `suspend`, se podría cambiar por una persistente (Room) sin tocar el repositorio.

Reglas del repositorio:

- Guarda **por (query normalizada, página)**.
- Si hay un resultado local válido, lo devuelve **sin llamar a red**. Si no hay o ha caducado, va a red, **mapea DTO → dominio** y **lo guarda en local**.
- Los errores (incluido el 404) no se guardan.
- Los personajes de cada página se guardan también por id, así que abrir el detalle no hace otra petición.
- **Dispatcher inyectado**: todo el trabajo se ejecuta en `@IoDispatcher` (`Dispatchers.IO` en la app, un dispatcher de test en los tests). El repositorio es *main-safe* sin importar desde dónde se le llame, y el mapeo y la cache no corren en el hilo principal.

## Tests

```bash
./gradlew :domain:test :data:test :presentation:testDebugUnitTest
```

**Datos de test compartidos**: `:domain` aplica el plugin `java-test-fixtures`. En `domain/src/testFixtures` están `aCharacter()` y los helpers `successData()` / `failureError()` para comprobar un `DomainResult`. Los tests de los tres módulos los usan con `testImplementation(testFixtures(project(":domain")))`, así que no se repiten en cada módulo.

| Capa | Test | Qué valida |
|---|---|---|
| Domain | `SearchCharactersUseCaseTest`, `GetCharacterDetailUseCaseTest` | Casos de uso con **repositorio mockeado (MockK)**: página por defecto, normalización, query vacía sin llamar al repo, página inválida, errores tipados, propagación de la cancelación. |
| Domain | `DomainResultTest` | `map` y `onSuccess`. |
| Data | `InMemoryCacheTest` | Acierto dentro del TTL, expiración, refresco, expiración independiente por clave, limpieza de caducadas al escribir, **límite de tamaño y expulsión LRU**. |
| Data | `InMemoryCharacterLocalDataSourceTest` | Guardar y leer páginas por query y página, caducidad, personajes por id. |
| Data | `CharacterRepositoryImplTest` | Repositorio con la fuente local real y un reloj falso (dispatcher de test inyectado): **mapeo DTO → dominio** (todos los campos, estado, siguiente página), cache por query y página sin red, normalización, **re-fetch tras expirar y actualización**, errores y 404 no guardados, detalle desde local. |
| Data | `RetrofitCharacterRemoteDataSourceTest` | MockWebServer: parámetros `name`/`page`, parseo a DTOs, excepciones → errores tipados (404 → `NotFound`, 500 → `Server`, JSON inesperado → `Unknown`, sin conexión → `Network` con su causa). |
| Presentation | `SearchViewModelTest` | Debounce, **cancelación sin resultados obsoletos**, query normalizada (no repite búsquedas equivalentes), restauración desde `SavedStateHandle`, 404 → empty (sin log), cada error → su `UiError`, log con la causa original, error + reintento, **paginación** (añadir, última página, duplicados, error y reintento, cancelación de página en curso), efecto de navegación. |
| Presentation | `DetailViewModelTest` | Carga y mapeo a UI, 404 → empty (sin log), error con log + reintento, efecto de volver. |
| Presentation | `CharacterUiMapperTest` | **Mapeo dominio → UI**: todos los campos, estados, tipo vacío → `null`, orden de las listas. |

## Stack

Kotlin 2.2 · Jetpack Compose (Material 3) · Hilt + KSP · Navigation Compose (rutas `@Serializable`) · Coroutines/Flow · Retrofit 3 + OkHttp · kotlinx.serialization · Coil 3 · JUnit4 · MockK · MockWebServer.
