import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.nio.charset.StandardCharsets

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kspCompose)
    alias(libs.plugins.room)
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
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
                outputFileName = "attoWallet.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
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
        val androidInstrumentedTest by getting
        val commonMain by getting
        val androidMain by getting

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
        }

        androidMain.dependencies {
            implementation(compose.preview)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.activity.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.core.splashscreen)

            // Koin support for Android
            implementation(libs.koin.android)

            // Permissions
            implementation(libs.accompanist.permissions)

            implementation(libs.slf4j.simple)

            implementation(libs.room.runtime.android)

            //QR scanning
            implementation(libs.androidx.camera.camera2) // Update to the latest version
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)

            implementation(libs.barcode.scanning)

            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.jna)
            implementation(libs.jna.platform)

            implementation(libs.slf4j.simple)

            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.runner)
            implementation(libs.androidx.rules)
            implementation(libs.androidx.ext.junit)
        }
        wasmJsMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "cash.atto.wallet"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "cash.atto.wallet"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

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

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.transport.runtime)
    testImplementation(libs.junit.jupiter)

    listOf(
        "kspAndroid",
        "kspDesktop",
        "kspCommonMainMetadata"
    ).forEach {
        add(it, libs.room.compiler)
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
            packageVersion = "1.0.13"
            modules("jdk.charsets")

            linux {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/logo.png"))
                shortcut = true
            }

            windows {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/logo.ico"))
                shortcut = true
            }

            macOS {
                iconFile.set(project.file("src/commonMain/composeResources/drawable/logo.png"))
            }
        }
    }
}

afterEvaluate {
    val kspAndroidTasks = tasks.matching { it.name.startsWith("ksp") && it.name.endsWith("KotlinAndroid") }

    val composeResourceGenerators = tasks.matching { t ->
        t.name == "generateComposeResClass" ||
                (t.name.startsWith("generate") &&
                        (t.name.contains("ResourceAccessors", ignoreCase = true) ||
                                t.name.contains("ResourceCollectors", ignoreCase = true)))
    }

    kspAndroidTasks.configureEach {
        dependsOn(composeResourceGenerators)
    }
}
