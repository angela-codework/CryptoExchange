# CryptoExchange - Market Feature Technical Documentation

## 1. Overview

This document outlines the technical architecture of the Market feature in the CryptoExchange application.

The architecture is based on Google's official "Guide to app architecture", utilizing key principles of Clean Architecture and a reactive, state-driven UI model. The primary goal is to create a scalable, maintainable, and testable codebase.

**Key Principles:**
- **Clean Architecture:** Separation of concerns into three main layers: Presentation, Domain, and Data.
- **MVVM (Model-View-ViewModel):** The pattern used in the presentation layer.
- **Reactive Programming:** The entire application is driven by Kotlin Flows. The UI is a simple function of the state (`UI = f(state)`).
- **Single Source of Truth (SSoT):** The `MarketRepository` acts as the SSoT for all market data.
- **Dependency Injection:** Hilt is used to manage dependencies throughout the app.

## 2. Architecture Diagram

The data flows in a unidirectional loop, from the data sources up to the UI. The UI sends events down to the ViewModel to trigger actions.

```
+--------------------------------+
|      UI Layer (Compose)        |
|  [MarketScreen, Components]    |
|         ^                      |
|         | (Observes StateFlow) |
+---------|------------------------+
          |
+---------|------------------------+
| Presentation Layer (ViewModel) |
|      [MarketViewModel]         |
|         |                      |
|         v (Invokes UseCases)   |
+---------|------------------------+
          |
+---------|------------------------+
|      Domain Layer (UseCases)     |
| [SyncMarketsUseCase, Get...]   |
|         |                      |
|         v (Calls Repository)   |
+---------|------------------------+
          |
+---------|------------------------+
|       Data Layer (Repository)    |
|      [MarketRepositoryImpl]      |
|         |                      |
|         +----------------------+
|         |                      |
|         v                      v
|  [MarketApi]         [WebSocketClient]
|  (Retrofit)          (OkHttp)
+--------------------------------+
```

## 3. Key Components

### 3.1. Presentation Layer

- **`MarketViewModel.kt`**:
  - The brain of the UI. It does not hold any direct references to Android Framework components.
  - It orchestrates calls to various UseCases.
  - It constructs a single `uiState` as a `StateFlow<MarketUiState>` by combining multiple data streams (`market data`, `loading status`, `connection status`, etc.) using Flow operators like `combine`.
  - It provides public functions (e.g., `onRetry`, `onSelectMarketTypeChange`) for the UI to call in response to user interactions.

- **`MarketScreen.kt`**:
  - A Jetpack Compose screen that is fully stateless.
  - It observes the `uiState` from the ViewModel using `collectAsStateWithLifecycle`.
  - It renders the UI based on the properties of the `MarketUiState` object (e.g., shows a `LoadingIndicator` if `isLoading` is true, shows an `ErrorMessage` if `error` is not null).
  - It calls the ViewModel's public functions to report user actions.

### 3.2. Domain Layer

- **UseCases (e.g., `SyncMarketsUseCase`, `GetMarketsWithPriceUseCase`, `ObserveNetworkStatusUseCase`)**:
  - Each UseCase encapsulates a single, specific piece of business logic.
  - They act as a bridge between the ViewModel and the Repository.
  - This layer decouples the ViewModel from the data layer, making the ViewModel unaware of where the data comes from. This makes the system more modular and easier to test.

### 3.3. Data Layer

- **`MarketRepositoryImpl.kt`**:
  - The implementation of the `MarketRepository` interface and the app's Single Source of Truth for market data.
  - It manages and orchestrates data from all sources:
    - **`MarketApi` (Retrofit):** For one-shot fetching of the initial market list.
    - **`WebSocketClient` (OkHttp):** For receiving a continuous stream of real-time price updates.
    - **In-memory Cache:** A simple time-based cache for the market list. Since the out-of-date market and price should not be displayed, market local cache time set to 30s while price data is not cached at all. 
  - It exposes a primary data stream (`getMarketsWithRealTimePrice`) which reactively combines the base market list with real-time prices.
  - It contains the logic for WebSocket lifecycle management (`onStart`/`onCompletion`) tied to the reactive stream's collectors.

- **`WebSocketClient.kt`**:
  - A wrapper around OkHttp's WebSocket functionality.
  - It is responsible for connecting, disconnecting, and parsing incoming messages.
  - It contains the internal **auto-retry logic** (e.g., retry 3 times with backoff) when an unexpected disconnection occurs.
  - It exposes the connection status (`CONNECTED`, `DISCONNECTED`) as a `StateFlow`.

### 3.4. Core Module

- **`ConnectivityObserver.kt`**:
  - An application-wide utility that monitors the device's overall network connectivity.
  - It uses a `callbackFlow` to wrap Android's `ConnectivityManager.NetworkCallback` into a modern, reactive `Flow<Status>`.

## 4. Data & State Flow

The most complex part of the architecture is the reactive stream in the `MarketViewModel` that constructs the `uiState`.

1.  **Combine**: A top-level `combine` operator merges the latest values from five different flows:
    - The main market data flow.
    - The selected market type.
    - The loading status.
    - The WebSocket connection status.
    - The device network status.
2.  **Logic**: Inside the `combine` block, logic is applied to filter the market list and determine the final error message based on priority (Network > WebSocket > No Data).
3.  **`stateIn`**: The resulting flow is converted into a hot `StateFlow` using `stateIn`, which is then exposed to the UI. This ensures the state is shared and survives configuration changes.
4.  **Silent Flow Fix**: To prevent the `combine` operator from getting stuck waiting for initial values from cold flows, the `.onStart { emit(...) }` operator is used to provide a default initial value for them.

## 5. Dependency Injection

- **Hilt** is used for dependency injection.
- **Modules (`AppModule`, `RepoModule`, `NetworkModule`, etc.)** are defined to instruct Hilt on how to provide instances of interfaces (`MarketRepository`, `ConnectivityObserver`) and external libraries (`Retrofit`, `OkHttpClient`).
- ViewModels are injected with their required UseCases using `@HiltViewModel` and constructor injection.

## 6. Testing

- **Unit Tests (`MarketViewModelTest.kt`)**:
  - Use `JUnit4` and `kotlinx-coroutines-test` (`runTest`).
  - **Mocking**: Dependencies (UseCases) are mocked using the `mockk` library.
  - **Flow Testing**: The `Turbine` library is used to test `Flow` and `StateFlow` emissions in a structured and predictable way.
  - **Android Dependencies**: To run tests on a local JVM, Android framework dependencies like `android.util.Log` are handled by providing a test implementation to the `AppLogger` abstraction layer in the `@Before` setup block.
