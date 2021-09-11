plugins {
    application
    id("com.google.devtools.ksp")
}

dependencies {
    compileOnly(project(":komapper-annotation"))
    implementation(project(":komapper-tx-jdbc"))
    ksp(project(":komapper-processor"))
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
}

application {
    mainClass.set("example.ApplicationKt")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
