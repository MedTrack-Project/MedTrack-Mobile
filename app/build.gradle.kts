import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun projectConfig(name: String, fallback: String): String {
    return providers.gradleProperty(name)
        .orElse(localProperties.getProperty(name) ?: fallback)
        .get()
}

android {
    namespace = "com.example.piec_1"
    compileSdk = 37


    defaultConfig {
        applicationId = "com.example.piec_1"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiBaseUrl = projectConfig("MEDTRACK_API_BASE_URL", "http://192.168.1.123:8081/")
        val scanUrl = projectConfig("MEDTRACK_SCAN_URL", "http://192.168.1.107:8000/detect")

        buildConfigField("String", "MEDTRACK_API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "MEDTRACK_SCAN_URL", "\"$scanUrl\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.R",
                    "*.R$*",
                    "*.Manifest*",
                    "*.*GeneratedInjector*",
                    "_com_example_piec_1_*",
                    "hilt_aggregated_deps.*",
                    "hilt_aggregated_deps/*",
                    "dagger.hilt.internal.aggregatedroot.codegen.*",
                    "dagger/hilt/internal/aggregatedroot/codegen/*",
                    "dagger.hilt.internal.processedrootsentinel.codegen.*",
                    "dagger/hilt/internal/processedrootsentinel/codegen/*",
                    "com.example.piec_1.MainActivity",
                    "com.example.piec_1.MainActivityKt*",
                    "com.example.piec_1.MedTrackApp",
                    "com.example.piec_1.di.*",
                    "com.example.piec_1.data.local.AppDatabase*",
                    "com.example.piec_1.data.local.Migrations*",
                    "com.example.piec_1.data.local.daos.*",
                    "com.example.piec_1.data.local.entity.*",
                    "com.example.piec_1.data.remote.ApiService*",
                    "com.example.piec_1.data.remote.dto.*",
                    "com.example.piec_1.data.repository.AuthRepository*",
                    "com.example.piec_1.data.repository.LoginData*",
                    "com.example.piec_1.data.repository.LoginException*",
                    "com.example.piec_1.data.repository.MedicamentoRepository*",
                    "com.example.piec_1.data.repository.ScanRepository*",
                    "com.example.piec_1.data.session.*",
                    "com.example.piec_1.domain.service.*",
                    "com.example.piec_1.ui.components.*",
                    "com.example.piec_1.ui.navigation.*",
                    "com.example.piec_1.ui.screen.Tela*",
                    "com.example.piec_1.ui.screen.viewModel.CameraViewModel*",
                    "com.example.piec_1.ui.screen.viewModel.LoginViewModel*",
                    "com.example.piec_1.ui.theme.*",
                    "com.example.piec_1.utils.MultipartImageUtils*",
                    "com.example.piec_1.utils.connection.*",
                    "com.example.piec_1.utils.exceptions.*",
                    "com.example.piec_1.utils.notifications.*",
                    "*.*_Factory",
                    "*.*_MembersInjector",
                    "*.Hilt_*",
                    "*.*Hilt*",
                    "*.*Dagger*",
                    "*.*Module",
                    "*.*ComposableSingletons*",
                    "**/*Activity*",
                    "**/*Fragment*",
                    "**/hilt_aggregated_deps/**",
                    "**/dagger/hilt/internal/**",
                    "hilt_aggregated_deps/**",
                    "dagger/hilt/internal/**"
                )
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.camera.camera2.pipe)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.google.mlkit.text.recognition)
    implementation(libs.google.mlkit.objects.detection)
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.converter.gson)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.guava)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
}
