# Mini Mechanic Service App — Milestones

Assignment: `C:\Users\Krish\Downloads\Android Development Internship — Assignment.md`
One milestone at a time, top to bottom. Every milestone ends on a runnable app.

## Locked decisions

- **API**: mockapi.io — base `https://6a981a5b7160beda2292abe1.mockapi.io/api/mechanicsdata/`,
  resources `mechanics` (GET) and `serviceRequests` (POST). Verified writable; nested
  JSON survives the round-trip.
- **Structure**: single `:app` module, clean architecture by package.
- **Bonus scope**: Hilt DI, Room offline cache, unit tests, search/filter.
  Firebase + auth deliberately skipped (documented as a trade-off in the README).

## Environment facts

- `JAVA_HOME` is unset in the shell — **prefix every build** with `JAVA_HOME="D:/Android studio/jbr"`.
  `~/.gradle/gradle.properties` pins `org.gradle.java.home`, but the `gradlew` launcher
  script needs `java` on PATH before that ever applies.
- `gradle/gradle-daemon-jvm.properties` was **deleted** in M0. It pinned
  `toolchainVersion=21` with no vendor and outranked `org.gradle.java.home`, which is
  the cause of the `jlink` / `JdkImageTransform` failure.
- compileSdk is **37**, not 36.1 — `core-ktx 1.19.0` and `lifecycle 2.11.0` both require it.
- `Icons.Default.*` needs `material-icons-core` explicitly on Compose BOM 2026.02.01.
- Retrofit 3.0.0 resolves **okhttp 4.12.0**, not 5.x. The okhttp BOM pins 4.12.0 so
  `logging-interceptor` / `mockwebserver` cannot drag the app onto okhttp 5.
- Device is **wireless adb only** and drops out. Rediscover with `adb mdns services`,
  then `adb connect <ip:port>`. If it refuses, wireless debugging must be re-enabled
  on the phone by hand.

## M0 — Foundations ✅ DONE

- [x] Delete the `gradle-daemon-jvm.properties` jlink bomb
- [x] `android.disallowKotlinSourceSets=false` for KSP vs AGP 9
- [x] Version catalog: Retrofit 3, okhttp BOM, Room 2.8.4, Hilt 2.60.1, KSP 2.2.10-2.0.2,
      Navigation 2.10.0, serialization, material-icons-core, test deps — all versions
      verified against Maven Central / Google's repo before use
- [x] compileSdk 37, `jvmTarget = JVM_11`, `BuildConfig.BASE_URL`
- [x] `MiniMechanicApp` (`@HiltAndroidApp`) + `@AndroidEntryPoint` on MainActivity
- [x] Manifest: INTERNET, ACCESS_NETWORK_STATE, `<queries>` for the tel: dial intent
- [x] `assembleDebug` green (6m14s) — Hilt component sources confirmed generated
- [x] `testDebugUnitTest` green — 2/2 smoke tests prove mockk + turbine + coroutines-test wire up
- [x] `git init` + initial commit (standalone repo, was untracked inside the `C:\Users\Krish` mega-repo)
- [ ] **On-device launch — BLOCKED**, device dropped wireless adb mid-install

## M1 — Data & domain

- [ ] `domain/model`: Mechanic, ServiceOffering, WorkingHours, ServiceRequest
- [ ] `domain/repository` interfaces + use cases
- [ ] `data/remote`: DTOs (`ignoreUnknownKeys = true` — mockapi injects `avatar`/`createdAt`), MechanicApi, mappers
- [ ] `data/local`: Room entity/DAO/database
- [ ] Offline-first repository: Room is the source of truth, network writes through
- [ ] Typed failures (no-network / timeout / HTTP / parse), never a raw `e.message`
- [ ] Seed the `mechanics` resource with 8–10 realistic garages via POST
- Done when: unit tests cover mappers + open/closed logic. No UI.

## M2 — Home screen

- [ ] Mechanic cards: name, rating, distance, location, service chips, open/closed badge
- [ ] Loading / error-with-retry / empty states
- [ ] Search on name + services; filter open-now; sort by rating or distance
- [ ] Visible list is a **computed** property, not a stored second list

## M3 — Mechanic details

- [ ] Name, rating, address, services, working-hours table with today highlighted
- [ ] Tappable phone number (dial intent), Request Service CTA

## M4 — Request service

- [ ] Form: name, phone, vehicle number, service dropdown (from that mechanic), description
- [ ] Per-field validation, inline errors, submit disabled until valid
- [ ] Real POST to `serviceRequests`, confirmation with a request reference

## M5 — Tests

- [ ] Mappers, open/closed, haversine distance, form validation
- [ ] `HomeViewModel` transitions against a fake repository
- [ ] Repository error paths on MockWebServer
- [ ] Route args via property reference, **never** `toRoute<T>()` (pulls in a real
      `android.os.Bundle` and kills the whole test class at construction)
- [ ] Delete `ToolchainSmokeTest` once real tests exist

## M6 — Submission

- [ ] README: setup, API details, architecture, assumptions, trade-offs
- [ ] On-device screenshots of all four screens + short recording
- [ ] Push to GitHub
