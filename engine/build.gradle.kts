plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}

tasks.withType<Test> { useJUnit() }

// Runs the engine's self-checks on the JVM: `./gradlew :engine:selfCheck`
tasks.register<JavaExec>("selfCheck") {
    group = "verification"
    description = "Runs the engine self-check suite (JVM)."
    mainClass.set("com.monaart.engine.diag.SelfCheckMainKt")
    classpath = sourceSets["main"].runtimeClasspath
}
