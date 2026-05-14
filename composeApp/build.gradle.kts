import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kspCompose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.room3)
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

fun gitShortSha(): String =
    runCatching {
        val output = ByteArrayOutputStream()
        val process =
            ProcessBuilder("git", "rev-parse", "--short", "HEAD")
                .directory(rootDir)
                .redirectErrorStream(true)
                .start()

        process.inputStream.copyTo(output)
        check(process.waitFor() == 0)

        output.toString().trim().ifEmpty { "unknown" }
    }.getOrElse { "unknown" }

val releaseVersion = providers.gradleProperty("app.version").orNull
val appVersion = releaseVersion ?: gitShortSha()
val packageVersion = releaseVersion ?: "0.0.0"
val generatedVersionKotlinDir = layout.buildDirectory.dir("generated/appVersion/commonMain/kotlin")

val generateAppVersionArtifacts =
    tasks.register("generateAppVersionArtifacts") {
        outputs.dir(generatedVersionKotlinDir)

        doLast {
            val kotlinFile =
                generatedVersionKotlinDir
                    .get()
                    .file("cash/atto/wallet/config/AppVersion.kt")
                    .asFile
            kotlinFile.parentFile.mkdirs()
            kotlinFile.writeText(
                """
                package cash.atto.wallet.config

                object AppVersion {
                    const val value = "$appVersion"
                }
                """.trimIndent(),
            )
        }
    }

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("attoWallet")
        browser {
            commonWebpackConfig {
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(project.rootDir.path)
                                add(project.projectDir.path)
                            }
                    }
            }
        }
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val desktopMain by getting
        val wasmJsMain by getting
        val wasmJsTest by getting
        val androidInstrumentedTest by getting
        val commonMain by getting
        val commonTest by getting
        val androidMain by getting

        commonMain.kotlin.srcDir(generatedVersionKotlinDir)

        val jvmMain by creating {
            dependsOn(commonMain)
        }
        androidMain.dependsOn(jvmMain)
        desktopMain.dependsOn(jvmMain)

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.io.core)
            implementation(libs.atto.commons.wallet)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.koin.composeVM)

            implementation(libs.material3.window.size)

            implementation(libs.decompose)
            implementation(libs.decompose.extensions.compose)
            implementation(libs.bignum)
            implementation(libs.qrose)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.room3.runtime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(compose.preview)

            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.androidx.activity.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.core.splashscreen)

            // Koin support for Android
            implementation(libs.koin.android)

            // Permissions
            implementation(libs.accompanist.permissions)

            implementation(libs.slf4j.simple)

            // QR scanning
            implementation(libs.androidx.camera.camera2) // Update to the latest version
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)

            implementation(libs.barcode.scanning)

            implementation(libs.sqlite.bundled)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.jna)
            implementation(libs.jna.platform)

            implementation(libs.slf4j.simple)

            implementation(libs.sqlite.bundled)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.runner)
            implementation(libs.androidx.rules)
            implementation(libs.androidx.ext.junit)
        }
        wasmJsMain.dependencies {
            implementation(libs.atto.commons.worker.web)
            implementation(libs.androidx.datastore.core.okio)
            implementation(libs.androidx.datastore.preferences.core)
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
            implementation(libs.sqlite.web)
            implementation(npm("@zxing/library", "0.21.3"))
            implementation(
                npm(
                    "sqlite-web-worker",
                    layout.projectDirectory.dir("sqlite-web-worker").asFile,
                ),
            )
            implementation(
                devNpm(
                    "html-webpack-plugin",
                    "5.6.3",
                ),
            )
        }

        wasmJsTest.dependencies {
            implementation(devNpm("puppeteer", "latest"))
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

tasks.configureEach {
    if (
        name.startsWith("compile") ||
        name.startsWith("ksp") ||
        name.startsWith("runKtlint") ||
        name.endsWith("ProcessResources")
    ) {
        dependsOn(generateAppVersionArtifacts)
    }
}

android {
    namespace = "cash.atto.wallet"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "cash.atto.wallet"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    buildToolsVersion = "35.0.0"
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.transport.runtime)
    testImplementation(libs.junit.jupiter)

    listOf(
        "kspAndroid",
        "kspDesktop",
        "kspWasmJs",
    ).forEach {
        add(it, libs.room3.compiler)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.toString()
}

compose.desktop {
    application {
        mainClass = "cash.atto.wallet.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.Rpm)
            packageName = "AttoWallet"
            packageVersion = packageVersion
            modules("jdk.charsets")

            linux {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/app_logo.png"))
                shortcut = true
            }

            windows {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/app_logo.ico"))
                shortcut = true
            }

            macOS {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/app_logo.png"))
            }
        }
    }
}

afterEvaluate {
    val kspAndroidTasks = tasks.matching { it.name.startsWith("ksp") && it.name.endsWith("KotlinAndroid") }

    val composeResourceGenerators =
        tasks.matching { t ->
            t.name == "generateComposeResClass" ||
                (
                    t.name.startsWith("generate") &&
                        (
                            t.name.contains("ResourceAccessors", ignoreCase = true) ||
                                t.name.contains("ResourceCollectors", ignoreCase = true)
                        )
                )
        }

    kspAndroidTasks.configureEach {
        dependsOn(composeResourceGenerators)
    }
}
