import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.sqldelight)
}

// Конфигурация сборки -> сгенерированный BuildConfig.kt в commonMain (значения берутся из gradle.properties).
val generateBuildConfig = tasks.register("generateBuildConfig") {
    val baseUrl = providers.gradleProperty("tamyr.baseUrl").orElse("")
    val useRemote = providers.gradleProperty("tamyr.useRemote").orElse("false")
    val registrationEnabled = providers.gradleProperty("tamyr.registrationEnabled").orElse("false")
    val outDir = layout.buildDirectory.dir("generated/buildconfig")
    inputs.property("baseUrl", baseUrl)
    inputs.property("useRemote", useRemote)
    inputs.property("registrationEnabled", registrationEnabled)
    outputs.dir(outDir)
    doLast {
        val file = outDir.get().file("ru/tatarcha/tatlearn/core/common/BuildConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package ru.tatarcha.tatlearn.core.common

            /** Сгенерировано из gradle.properties задачей :shared:generateBuildConfig. Не редактировать. */
            object BuildConfig {
                const val BASE_URL = "${baseUrl.get()}"
                const val USE_REMOTE = ${useRemote.get()}

                /** Фича-флаг: регистрация и вход. false — гостевой режим (профиль на устройстве). */
                const val REGISTRATION_ENABLED = ${registrationEnabled.get()}
            }
            """.trimIndent(),
        )
    }
}

sqldelight {
    databases {
        create("TamyrDatabase") {
            packageName.set("ru.tatarcha.tatlearn.core.database")
        }
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "ru.tatarcha.tatlearn.shared")
            linkerOpts("-lsqlite3")
        }
    }
    
    android {
       namespace = "ru.tatarcha.tatlearn.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        commonMain { kotlin.srcDir(generateBuildConfig) }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.work.runtime)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.androidDriver)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.androidx.navigationevent.compose)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.kotlinx.serialization.json)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewModel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktorfit.lib)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.multiplatform.settings)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.nativeDriver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.multiplatform.settings.test)
        }
    }
}

dependencies {
    // Кодогенерация Ktorfit, по одной конфигурации KSP на каждый таргет компиляции.
    add("kspAndroid", libs.ktorfit.ksp)
    add("kspIosArm64", libs.ktorfit.ksp)
    add("kspIosSimulatorArm64", libs.ktorfit.ksp)
    androidRuntimeClasspath(libs.compose.uiTooling)
}