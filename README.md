# NBA App

A small NBA app built for the TGLab Android task, using **MVVM** with **Kotlin Multiplatform**
and **Compose Multiplatform** (Android and iOS share all the code, including the UI).

| Screen | What it does |
|---|---|
| **Home** tab | Lists the current NBA teams (name, city, conference). The **Name** button opens a dialog to sort by Name, City or Conference; the choice updates the button title and the list. |
| **Team games** bottom sheet | Opened by tapping a team. Receives the team id and lists its games (home team, home score, visitor team, visitor score), newest first, with **endless scroll**. Closes with the Back button, by dragging down or with **< Home**. |
| **Players** tab | Searches players by name (first name, last name, team). Tapping a player reuses the team games bottom sheet for the player's team. |

## Setup

### API key

The endpoints in the task (`https://www.balldontlie.io/api/v1/...`) no longer exist and return
`404`. balldontlie moved the API to **`https://api.balldontlie.io/v1`**, which:

- requires an **API key** in the `Authorization` header (free account at
  [balldontlie.io](https://www.balldontlie.io); the free tier covers Teams, Players and Games);
- uses **cursor pagination** (`meta.next_cursor` → `?cursor=`) instead of page numbers;
- allows **5 requests per minute** on the free tier.

Add the key to `local.properties` (it is git-ignored, so the key is never committed):

```properties
nba.api.key=YOUR_API_KEY
```

or export it as the `NBA_API_KEY` environment variable. The `generateApiConfig` Gradle task turns
it into the `ApiConfig` object used by the HTTP client. Without a key the app shows an
"API key is missing or invalid" message.

### Running

- **Android:** `./gradlew :androidApp:installDebug`, or the `androidApp` run configuration.
- **iOS:** open [`iosApp`](./iosApp) in Xcode and run it.

### Tests

- Android (JVM): `./gradlew :shared:testAndroidHostTest`
- iOS simulator: `./gradlew :shared:iosSimulatorArm64Test`

The same 47 tests in `commonTest` run on both platforms.

## Architecture

```
UI (Compose)  ──events──▶  ViewModel  ──▶  Repository (interface)  ──▶  RemoteDataSource  ──▶  balldontlie
              ◀──StateFlow<UiState>──      ◀── Result<T> ─────────      (Ktor + DTOs)
```

- **UI** — stateless `…Content` composables draw a `UiState` and send events up; the
  `…Screen` wrapper gets the ViewModel from Koin and collects its state.
- **ViewModel** — owns one `StateFlow<…UiState>` per screen and turns events into state changes.
- **Domain** — plain models (`Team`, `Game`, `Player`, `Page<T>`) and repository interfaces.
  No Ktor, Compose or serialization types leak into it.
- **Data** — repository implementations, DTOs, mappers and the Ktor client. Every failure is
  mapped to an `ApiError` (`NETWORK`, `UNAUTHORIZED`, `RATE_LIMITED`, …), shown with a
  user-friendly message and a Retry button.

### Packages (`shared/src/commonMain/kotlin/com/nb/app`)

```
core/
  di/          Koin modules (platform HTTP engine, network, data, ViewModels)
  network/     HttpClient factory, ApiError, safeApiCall
  ui/          theme, reusable components (DataTable, state views, paging footer, endless scroll)
data/
  remote/      NbaRemoteDataSource + DTOs
  mapper/      DTO → domain
  repository/  Team, Game and Player repositories
domain/
  model/       Team, Game, Player, Page
  repository/  repository interfaces
presentation/
  navigation/  tabs, selected team, scoped ViewModel stores
  home/        Home tab, sort dialog
  teamgames/   team games bottom sheet
  players/     player search
```

### Libraries

Compose Multiplatform + Material 3, AndroidX Lifecycle ViewModel, Kotlin Coroutines/Flow,
Ktor Client (OkHttp on Android, Darwin on iOS), kotlinx.serialization, kotlinx-datetime and Koin.
Tests use kotlin-test, kotlinx-coroutines-test and Ktor `MockEngine`.

## Decisions

- **Recent games first.** The games endpoint only returns games oldest first (from 1946) and has
  no sort option. Each page of the endless scroll is therefore one **season**: the repository
  loads the whole season (one or two requests of 100, following the API cursor), drops games not
  played yet and sorts it newest first. Scrolling to the end loads the previous season. Before
  tip-off, when the current season has no games yet, the first page shows the last season.
- **Only current teams.** `/teams` also returns 59 defunct franchises (e.g. Chicago Stags) with an
  empty conference. They are filtered out, leaving the 30 current teams.
- **Respecting the rate limit.** The team list is cached in memory, the player search waits
  500 ms after the user stops typing and needs at least 2 letters, older searches are cancelled,
  and a failed "load more" waits for the user to tap Retry instead of retrying on every scroll.
- **Sorting is local.** All teams come in one response, so sorting just reorders the list; ties
  are broken by the full name so the order is stable.
- **One bottom sheet, two entry points.** The sheet is driven by a `SelectedTeam` (team id + name)
  held at the app level, so Home and Players open the same view. Its ViewModel lives in its own
  `ViewModelStore`: it survives rotation and is cleared when the sheet closes, so ViewModels don't
  pile up for every team opened.
- **State survives configuration changes.** ViewModels keep the data; the selected tab and team
  use `rememberSaveable`, and each tab keeps its scroll position when switching tabs.
