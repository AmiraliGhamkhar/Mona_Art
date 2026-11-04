plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    testImplementation(libs.junit)
}

tasks.withType<Test> { useJUnit() }

// Runs the engine's self-check suite on the JVM: `./gradlew :engine:selfCheck`
tasks.register<JavaExec>("selfCheck") {
    group = "verification"
    description = "Runs the engine self-check suite (JVM)."
    mainClass.set("com.monaart.engine.diag.SelfCheckKt")
    classpath = sourceSets["main"].runtimeClasspath
    // Optional markdown report: -PmonaReport=/path/report.md
    providers.gradleProperty("monaReport").orNull?.let { args("--report=$it") }
}
