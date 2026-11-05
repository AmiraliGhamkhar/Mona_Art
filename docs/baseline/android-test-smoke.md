# Instrumented smoke test

Added `app/src/androidTest/java/com/monaart/studio/EditorSmokeTest.kt`. It:

1. launches `MainActivity`,
2. creates a 256×256 transparent `ArtDocument`,
3. draws a 21-sample `BrushEngine` stroke,
4. saves via `ProjectRepository`,
5. reloads via the same repository,
6. asserts `alphaOf(pixel) > 0` before and after reload.

`app/build.gradle.kts` now declares `androidTestImplementation(libs.junit)` and
`androidTestImplementation(libs.androidx.test.core)`; the runner, Espresso and
Compose tooling dependencies were already present.

**Compile status: unverified.** `:app:assembleDebugAndroidTest` is in the CI
gate, but this sandbox has no Android SDK, so the source has not been compiled.
It is not `@Ignore`d, so the first emulator-capable CI run will execute it.
