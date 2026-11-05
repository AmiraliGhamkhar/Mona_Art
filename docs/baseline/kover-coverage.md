# Kover engine line coverage

**Starting line coverage: not measured.**

`org.jetbrains.kotlinx.kover` 0.9.11 is now applied to `:engine`, and CI runs
`:engine:koverHtmlReport :engine:koverXmlReport`. The report script parses the
resulting XML and writes `docs/baseline/kover-coverage.txt` plus a job-summary
section.

This sandbox cannot resolve Gradle dependencies or the Android SDK, so no Kover
XML exists yet. The placeholder `kover-coverage.txt` says exactly that; it is
not a fabricated percentage.
