import io.gitlab.arturbosch.detekt.Detekt
import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun projectConfig(name: String): String? = providers.environmentVariable(name).orNull
    ?.trim()
    ?.takeIf { it.isNotBlank() }
    ?: providers.gradleProperty(name).orNull?.trim()?.takeIf { it.isNotBlank() }
    ?: localProperties.getProperty(name)?.trim()?.takeIf { it.isNotBlank() }

fun validatedEndpoint(name: String, value: String, allowHttp: Boolean, requireTrailingSlash: Boolean): String {
    val uri = runCatching { URI(value) }
        .getOrElse { throw GradleException("$name deve ser uma URL valida.") }
    val allowedSchemes = if (allowHttp) setOf("http", "https") else setOf("https")

    require(uri.scheme?.lowercase() in allowedSchemes) {
        "$name deve usar ${if (allowHttp) "HTTP ou HTTPS" else "HTTPS"}."
    }
    require(!uri.host.isNullOrBlank()) { "$name deve conter um host valido." }
    require(uri.scheme?.lowercase() != "http" || uri.host in setOf("10.0.2.2", "localhost")) {
        "$name permite HTTP apenas para 10.0.2.2 ou localhost."
    }
    require(uri.userInfo == null) { "$name nao pode conter credenciais." }
    require(uri.fragment == null) { "$name nao pode conter fragmento." }
    require(!requireTrailingSlash || value.endsWith('/')) {
        "$name deve terminar com '/'."
    }

    return value
}

fun buildConfigString(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

val releaseWasRequested = gradle.startParameter.taskNames.any { taskName ->
    val normalized = taskName.lowercase()
    normalized.contains("release") ||
        normalized.endsWith("assemble") ||
        normalized.endsWith("bundle") ||
        normalized.endsWith("build")
}

val debugApiBaseUrl = validatedEndpoint(
    name = "MEDTRACK_API_BASE_URL",
    value = projectConfig("MEDTRACK_API_BASE_URL") ?: "http://10.0.2.2:8081/",
    allowHttp = true,
    requireTrailingSlash = true,
)
val debugScanUrl = validatedEndpoint(
    name = "MEDTRACK_SCAN_URL",
    value = projectConfig("MEDTRACK_SCAN_URL") ?: "http://10.0.2.2:8000/detect",
    allowHttp = true,
    requireTrailingSlash = false,
)

fun releaseEndpoint(name: String, requireTrailingSlash: Boolean): String {
    val placeholder = "https://configuration-required.invalid${if (requireTrailingSlash) "/" else "/detect"}"
    if (!releaseWasRequested) return placeholder

    val configuredValue = projectConfig(name) ?: throw GradleException("$name e obrigatoria para builds de release.")

    return validatedEndpoint(
        name = name,
        value = configuredValue,
        allowHttp = false,
        requireTrailingSlash = requireTrailingSlash,
    )
}

android {
    namespace = "com.medtrack.mobile"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.medtrack.mobile"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "MEDTRACK_API_BASE_URL", buildConfigString(debugApiBaseUrl))
            buildConfigField("String", "MEDTRACK_SCAN_URL", buildConfigString(debugScanUrl))
        }
        release {
            buildConfigField(
                "String",
                "MEDTRACK_API_BASE_URL",
                buildConfigString(releaseEndpoint("MEDTRACK_API_BASE_URL", requireTrailingSlash = true)),
            )
            buildConfigField(
                "String",
                "MEDTRACK_SCAN_URL",
                buildConfigString(releaseEndpoint("MEDTRACK_SCAN_URL", requireTrailingSlash = false)),
            )
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    packaging {
        jniLibs.keepDebugSymbols += setOf(
            "**/libandroidx.graphics.path.so",
            "**/libimage_processing_util_jni.so",
            "**/libmlkitcommonpipeline.so",
            "**/libsurface_util_jni.so",
        )
    }
}

val validateReleaseConfiguration = tasks.register("validateReleaseConfiguration") {
    group = "verification"
    description = "Valida endpoints HTTPS obrigatorios antes de qualquer tarefa de release."

    doLast {
        validatedEndpoint(
            name = "MEDTRACK_API_BASE_URL",
            value = projectConfig("MEDTRACK_API_BASE_URL")
                ?: throw GradleException("MEDTRACK_API_BASE_URL e obrigatoria para builds de release."),
            allowHttp = false,
            requireTrailingSlash = true,
        )
        validatedEndpoint(
            name = "MEDTRACK_SCAN_URL",
            value = projectConfig("MEDTRACK_SCAN_URL")
                ?: throw GradleException("MEDTRACK_SCAN_URL e obrigatoria para builds de release."),
            allowHttp = false,
            requireTrailingSlash = false,
        )
    }
}

tasks.configureEach {
    if (name.contains("release", ignoreCase = true) && name != validateReleaseConfiguration.name) {
        dependsOn(validateReleaseConfiguration)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

ktlint {
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    baseline = rootProject.file("config/detekt/baseline.xml")
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
        md.required.set(false)
    }
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
                    "_com_medtrack_mobile_*",
                    "hilt_aggregated_deps.*",
                    "hilt_aggregated_deps/*",
                    "dagger.hilt.internal.aggregatedroot.codegen.*",
                    "dagger/hilt/internal/aggregatedroot/codegen/*",
                    "dagger.hilt.internal.processedrootsentinel.codegen.*",
                    "dagger/hilt/internal/processedrootsentinel/codegen/*",
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
                    "dagger/hilt/internal/**",
                )
            }
        }
        verify {
            rule("Baseline global inicial") {
                // Gate conservador sobre todo o código de produção. Deve subir sem novas exclusões.
                minBound(5)
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
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
    debugImplementation(platform(libs.androidx.compose.bom))
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
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.androidx.concurrent.futures.ktx)
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
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.concurrent.futures)
    androidTestImplementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
}
