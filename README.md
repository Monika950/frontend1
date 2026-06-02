# Treasure Hunt Android App

Android frontend for the Treasure Hunt platform (Jetpack Compose + Hilt + Retrofit + Socket.IO).

## What is implemented

- Authentication: login, register, token restore, logout redirect.
- Home: dashboard layout with join card, active hunts, FAB, bottom nav.
- My Hunts: created/joined tabs, owner vs participant routing from Home.
- Create Hunt: redesigned screen with cover upload card, schedule pills, add location CTA.
- Active Hunt:
  - Loads selected hunt, participants, and locations.
  - Owner view: live tracker layout + participants progress list.
  - Participant view: map + target card + distance + answer task CTA.
  - Owner-only Create Location action.
  - Live tracking socket connection (`/tracking`) with room join/leave.
  - Device location streaming to backend (`tracking:update_position`).
- Notifications:
  - REST load (`GET /notifications`).
  - Realtime updates via socket namespace `/notifications`.
  - Handles `notifications:new`, `notifications:read`, `notifications:read_batch`.

## Tech stack

- Kotlin, Jetpack Compose, Navigation Compose
- Hilt DI
- Retrofit + OkHttp
- DataStore (token/current hunt persistence)
- Google Maps Compose
- Socket.IO client
- Fused Location Provider

## Project structure (high level)

- `app/src/main/java/com/example/treasurehuntapp/features` - UI screens + viewmodels
- `app/src/main/java/com/example/treasurehuntapp/data/repository` - repositories
- `app/src/main/java/com/example/treasurehuntapp/data/source/remote` - REST APIs/dtos
- `app/src/main/java/com/example/treasurehuntapp/data/source/realtime` - Socket.IO layer
- `app/src/main/java/com/example/treasurehuntapp/data/source/location` - device location source

## Realtime flow

### Notifications (`/notifications`)

1. `NotificationsScreen` calls `load()` and `connectRealtime()`.
2. REST loads initial list.
3. Socket connects with JWT from `TokenStorage`.
4. Incoming socket events are parsed in `NotificationsSocketDataSource`.
5. Events are emitted via `SharedFlow`.
6. `NotificationsViewModel` collects and updates UI state.

### Tracking (`/tracking`)

1. `ActiveHuntViewModel` connects socket and joins `hunt:{huntId}` room.
2. Location permission is requested in `ActiveHuntScreen`.
3. Device updates are collected from `DeviceLocationDataSource`.
4. ViewModel emits `tracking:update_position` (throttled to 1/sec).

## Permissions

Declared in manifest:

- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.ACCESS_COARSE_LOCATION`
- `android.permission.ACCESS_FINE_LOCATION`

Runtime request:

- Fine location permission is requested when opening Active Hunt.

## Setup

### 1. Google Maps API Key

The app requires a Google Maps API key to display maps.

1. Get an API key from [Google Cloud Console](https://console.cloud.google.com/google/maps-apis)
2. Copy `local.properties.example` to `local.properties`
3. Add your API key to `local.properties`:
   ```
   MAPS_API_KEY=your_actual_api_key_here
   ```
4. **Important**: Restrict your API key in Google Cloud Console:
   - Add application restrictions (Android apps)
   - Add your app's package name: `com.example.treasurehuntapp`
   - Add your SHA-1 certificate fingerprint

**Note**: `local.properties` is git-ignored and should never be committed to version control.

## Build and run

1. Complete the setup steps above (configure Google Maps API key).
2. Open in Android Studio.
3. Sync Gradle.
4. Run app on emulator/device (API 24+).

## Backend base URL

Configured in `NetworkModule` and `SocketFactory`:

- `http://178.104.200.100:3000`

If backend host changes, update both files.
