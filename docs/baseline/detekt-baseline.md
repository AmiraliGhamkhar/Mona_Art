# Detekt / ktlint baseline

Phase 0 adds:

- root `build.gradle.kts` applies `io.gitlab.arturbosch.detekt` and
  `org.jlleitschuh.gradle.ktlint` to both modules,
- `config/detekt/detekt.yml` (default rules, build-upon-default, exclusions),
- `config/detekt/baseline.xml` (seed),
- `.editorconfig` for ktlint,
- CI `detekt ktlintCheck` in the exit gate.

**Not verified:** the seed baseline is empty. It must be generated with
`./gradlew detektBaseline` in a full Gradle environment, then burned down in
Phase 1 with `maxIssues = 0`. Inventing issue IDs here would violate the
"evidence over assertion" rule. Because detekt/ktlint cannot be executed in this
sandbox, no clean/issue-free claim is made.
