# Direct-JVM verification (no Gradle)

The following commands were run from the repository root after extracting the
committed JDK wheel and downloading the Kotlin 2.4.21 compiler bundle from the
allowed npm registry. Paths under `/home/user/.cache` are sandbox-local and are
not part of the repository.

```bash
JAVA_HOME=/home/user/.cache/jdk4py/jdk4py/java-runtime
KOTLIN=/home/user/.cache/kotlin/compiler
CP="$KOTLIN/lib/kotlin-stdlib.jar:$KOTLIN/lib/kotlin-stdlib-jdk7.jar:$KOTLIN/lib/kotlin-stdlib-jdk8.jar:$KOTLIN/lib/annotations-13.0.jar"

# :engine main sources
java -cp "$KOTLIN/lib/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -no-stdlib -no-reflect -jvm-target 17 -Xjvm-default=all \
  -classpath "$CP" -d engine-out engine/src/main/kotlin

# 27 self-checks
java -cp "engine-out:$CP" com.monaart.engine.diag.SelfCheckKt \
  --report=docs/baseline/selfcheck-report.md

# Existing four JUnit suites (local shim supplies org.junit.Test/Assert)
java -cp "$KOTLIN/lib/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -no-stdlib -no-reflect -jvm-target 17 -Xjvm-default=all \
  -classpath "engine-out:$CP" -d engine-test-out \
  junit-shim/org/junit/JunitShim.kt \
  junit-shim/org/junit/rules/TemporaryFolder.kt \
  engine/src/test/kotlin

java -cp "engine-test-out:engine-out:$CP" org.junit.JUnitShimRunner \
  com.monaart.engine.brush.BrushEngineTest \
  com.monaart.engine.brush.StrokeQualityTest \
  com.monaart.engine.document.DocumentTest \
  com.monaart.engine.io.PersistenceTest
```

## Key output

```
:engine main sources → 93 class files, warnings only
SelfCheck → **27/27 checks passed.**
Engine suites → OK (48 tests)
perf.stroke_throughput → 0.18 ms per stylus event (5485 events/s)
StrokeQualityTest → mean 0.170 ms/sample, p99 1.55 ms
```

No engine source file was modified. The shim is a verification harness only;
the committed suites remain the contract.
