import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin module. Its only dependency is the standard JSR-330 annotations (@Inject).
plugins {
    alias(libs.plugins.kotlin.jvm)
    // Shared test data (src/testFixtures), reused by the tests of :domain, :data and :presentation.
    `java-test-fixtures`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
