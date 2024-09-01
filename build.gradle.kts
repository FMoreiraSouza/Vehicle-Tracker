plugins {
    kotlin("jvm") version "2.0.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(dependencyNotation = "com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")

}

tasks.test {
    useJUnitPlatform()
}