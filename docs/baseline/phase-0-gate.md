# GATE 0 — Phase 0 result

**Status: NOT PASSED. Phase 1 is not started.**

The engineered program requires every item to be ✓ before the next phase. This
sandbox cannot provide Gradle 9.8.2 or the Android SDK, so several items are
honestly marked ✗ rather than faked.

## Checklist

| # | Gate item | Status | Evidence / reason |
|---|---|---|---|
| 1 | baseline outputs committed under `docs/baseline/` | ✓ partial | `gradle-baseline.log`, direct-JVM compile/test/self-check logs and the explicit unavailable-results notes are committed. Full Gradle app outputs are absent; see `app-baseline-unavailable.md`. |
| 2 | detekt + ktlint + lint + Kover run in CI and block merges | ✗ | Configuration and CI steps added, but none executed here; detekt baseline is empty and Kover coverage is unmeasured. |
| 3 | CI: `setup-java` + Gradle caching present; no more report self-commits | ✓ | `.github/workflows/android.yml` rewritten with `actions/setup-java@v4`, `gradle/actions/setup-gradle@v4`, job summary + artifact instead of `git commit`/`git push`. Not executed here. |
| 4 | wheel untracked; catalog cleaned; ProGuard rules targeted; new release size recorded | ✗ | First three done (`git rm --cached`, `*.whl`, dead aliases removed, targeted R8 rules). Post-change release size **not** re-measured; see `release-size.md`. |
| 5 | `androidTest` smoke test exists and compiles (`assembleDebugAndroidTest` passes) | ✗ | Source exists and is wired into CI, but compile is unverified; see `android-test-smoke.md`. |
| 6 | `:engine:test`, `:engine:selfCheck`, `:app:testDebugUnitTest` still 100 % green | ✗ | Engine verified directly (48 tests, 27/27 self-checks); app unit tests not run. Real Gradle tasks unverified. |

## Commands run (exact)

```bash
# Requested Phase 0.1 baseline — FAILED before configuration
./gradlew --no-daemon --console=plain :engine:test :engine:selfCheck \
  :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
# → exit=1, SSLHandshakeException on services.gradle.org

# Direct-JVM engine verification (see jvm-toolchain.md)
java ... K2JVMCompiler ... -d engine-out engine/src/main/kotlin
java -cp "engine-out:..." com.monaart.engine.diag.SelfCheckKt --report=...
java -cp "engine-test-out:engine-out:..." org.junit.JUnitShimRunner \
  com.monaart.engine.brush.BrushEngineTest \
  com.monaart.engine.brush.StrokeQualityTest \
  com.monaart.engine.document.DocumentTest \
  com.monaart.engine.io.PersistenceTest
```

## Verbatim key output

```
:engine main sources → 93 class files, warnings only
SelfCheck → **27/27 checks passed.**
Engine suites → OK (48 tests)
perf.stroke_throughput → 0.18 ms per stylus event (5485 events/s)
StrokeQualityTest → [perf] engine stroke+flush: 0.170 ms/sample
Gradle baseline → exit=1
```

## Anything not verified

- Every Gradle task: distribution host blocked.
- Every Android/Compose/Robolectric task: no Android SDK.
- Detekt/ktlint results and baseline contents: plugins cannot resolve here.
- Kover starting line coverage: no XML report.
- Release APK/AAB size after R8 change: no release build.
- `androidTest` compilation and execution: no device/emulator.
- Docs claims audit: deferred to Phase 5 as specified; Phase 0 only corrected
  the most directly contradicted claims in this commit.

Per ground rule 6, work stops here. Do **not** begin Phase 1 until a
full-environment run turns every ✗ into ✓.
