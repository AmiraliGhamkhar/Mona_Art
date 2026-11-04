# Build report

Generated: 2026-11-04T23:01:33.667590+00:00

Commit: `ae4d03626b792eaf30d06a3065bb50d0a39b1e87`

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
FAILURE: Build failed with an exception.
* Where:
Build file '/home/runner/work/Mona_Art/Mona_Art/build.gradle.kts' line: 1
* What went wrong:
Plugin [id: 'com.google.devtools.ksp', version: '2.4.21-2.0.4', apply: false] was not found in any of the following sources:
- Gradle Core Plugins (plugin is not in 'org.gradle' namespace)
- Included Builds (No included builds contain this plugin)
- Plugin Repositories (could not resolve plugin artifact 'com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.4.21-2.0.4')
  Searched in the following repositories:
    Google
    MavenRepo
    Gradle Central Plugin Repository
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
BUILD FAILED in 6s
Configuration cache entry stored.
```

## release: log tail

```
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.8.2/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
Calculating task graph as no cached configuration is available for tasks: :app:assembleRelease :app:bundleRelease
FAILURE: Build failed with an exception.
* Where:
Build file '/home/runner/work/Mona_Art/Mona_Art/build.gradle.kts' line: 1
* What went wrong:
Plugin [id: 'com.google.devtools.ksp', version: '2.4.21-2.0.4', apply: false] was not found in any of the following sources:
- Gradle Core Plugins (plugin is not in 'org.gradle' namespace)
- Included Builds (No included builds contain this plugin)
- Plugin Repositories (could not resolve plugin artifact 'com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.4.21-2.0.4')
  Searched in the following repositories:
    Google
    MavenRepo
    Gradle Central Plugin Repository
* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.
BUILD FAILED in 6s
Configuration cache entry stored.
```
