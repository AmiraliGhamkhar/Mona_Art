# Gradle wrapper checksum

`gradle/wrapper/gradle-wrapper.properties` now contains:

```
distributionSha256Sum=21bd5da2de49b642904077f38c0efdc7778e92b3ed0889392dca61b97e389607
```

Provenance: the checksum was read from the pinned transparency-log entry
`f-droid/gradle-transparency-log@48787bf2f54a573eea0d918959b09c23ca28386e`,
path `sha256/gradle-9.8.2-bin.zip.sha256`, via the GitHub contents API. It
matches the value published in the Gradle 9.8.2 package metadata found during
the Phase 0 analysis.

Because the Gradle distribution host is blocked in this sandbox, the wrapper
could not be asked to verify the download locally. The checksum is therefore
**recorded but unverified here**; CI with normal egress will verify it on the
first `./gradlew` invocation.
