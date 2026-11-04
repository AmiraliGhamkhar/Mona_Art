# Build report

Generated: 2026-11-04T23:03:47.088332+00:00

Commit: `abfbb83b79d16823e2e5aa1264654e3fab6185b5`

## Step status

| Step | Exit code | Result |
|---|---|---|
| engine_selfcheck | 1 | FAIL |
| engine_unit_tests | 1 | FAIL |
| app_unit_tests | 1 | FAIL |
| assemble_debug | 1 | FAIL |
| assemble_release | 1 | FAIL |
| lint | 1 | FAIL |

## Engine self-check
_no report produced_

## Artifacts


## assembleDebug: errors

```
FAILURE: Build failed with an exception.
```

## release: errors

```
FAILURE: Build failed with an exception.
```

## app tests: errors

```
FAILURE: Build failed with an exception.
```

## engine tests: errors

```
FAILURE: Build failed with an exception.
```

## assembleDebug: log tail

```
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
Calculating task graph as no cached configuration is available for tasks: :app:assembleDebug
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* Where:
Build file '/home/runner/work/Mona_Art/Mona_Art/app/build.gradle.kts' line: 1
* What went wrong:
An exception occurred applying plugin request [id: 'org.jetbrains.kotlin.android', version: '2.4.21']
> Failed to apply plugin 'org.jetbrains.kotlin.android'.
   > ⛔ Failed to apply plugin 'org.jetbrains.kotlin.android'
     The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP 9.0.
     Solution: Remove the 'org.jetbrains.kotlin.android' plugin from this project's build file: app/build.gradle.kts.
     See https://kotl.in/gradle/agp-built-in-kotlin for more details.
      > java.lang.Throwable (no error message)
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 7s
Configuration cache entry stored.
```

## release: log tail

```
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
Calculating task graph as no cached configuration is available for tasks: :app:assembleRelease :app:bundleRelease
[Incubating] Problems report is available at: file:///home/runner/work/Mona_Art/Mona_Art/build/reports/problems/problems-report.html
FAILURE: Build failed with an exception.
* Where:
Build file '/home/runner/work/Mona_Art/Mona_Art/app/build.gradle.kts' line: 1
* What went wrong:
An exception occurred applying plugin request [id: 'org.jetbrains.kotlin.android', version: '2.4.21']
> Failed to apply plugin 'org.jetbrains.kotlin.android'.
   > ⛔ Failed to apply plugin 'org.jetbrains.kotlin.android'
     The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP 9.0.
     Solution: Remove the 'org.jetbrains.kotlin.android' plugin from this project's build file: app/build.gradle.kts.
     See https://kotl.in/gradle/agp-built-in-kotlin for more details.
      > java.lang.Throwable (no error message)
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
BUILD FAILED in 7s
Configuration cache entry stored.
```
