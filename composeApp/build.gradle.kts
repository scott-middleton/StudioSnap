import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.google.services) apply false
}

// Only apply google-services if the config file exists (allows VPS compilation without it)
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Android x
                implementation(libs.androidx.navigation.compose)

                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.animation)

                // Coil
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor)

                // Koin
                implementation(libs.bundles.koin.compose)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)

                // Firebase
                implementation(libs.firebase.analytics)
                implementation(libs.firebase.auth)
                implementation(libs.firebase.crashlytics)

                // Ktor
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)

                // RevenueCat
                implementation(libs.purchases.core)
                implementation(libs.purchases.result)

                // Room
                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)

                // Okio (used by ImageCacheManager — transitive via Ktor but needed explicitly for metadata compilation)
                implementation("com.squareup.okio:okio:3.11.0")

            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.koin.android)
                implementation(libs.ktor.client.cio)
                implementation(libs.bundles.compose)

                // Authentication
                implementation(libs.androidx.exifinterface)
                implementation(libs.bundles.google.id.android)
                implementation(libs.google.play.services.auth)
                implementation(libs.androidx.security.crypto)

                // In-App Review
                implementation(libs.play.review)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain) // Now this works
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }
}

android {
    namespace = "com.middleton.studiosnap"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.middleton.studiosnap"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 4
        versionName = "1.0"
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
}

buildkonfig {
    packageName = "com.middleton.studiosnap.composeapp"

    defaultConfigs {
        val isDebug = gradle.startParameter.taskNames.any {
            it.contains("debug", ignoreCase = true)
        }
        buildConfigField(BOOLEAN, "IS_DEBUG", isDebug.toString())

        buildConfigField(
            STRING,
            "REVENUE_CAT_ANDROID_KEY",
            "TODO_REVENUE_CAT_ANDROID_KEY"
        )

        buildConfigField(
            STRING,
            "REVENUE_CAT_IOS_KEY",
            "TODO_REVENUE_CAT_IOS_KEY"
        )

        buildConfigField(
            STRING,
            "REVENUE_CAT_SECRET_KEY",
            "TODO_REVENUE_CAT_SECRET_KEY"
        )

        buildConfigField(
            STRING,
            "REVENUE_CAT_PROJECT_ID",
            "TODO_REVENUE_CAT_PROJECT_ID"
        )

        buildConfigField(
            STRING,
            "SCHEME",
            "com.middleton.studiosnap"
        )

        buildConfigField(
            STRING,
            "GOOGLE_SERVER_CLIENT_ID",
            "TODO_GOOGLE_SERVER_CLIENT_ID"
        )

        buildConfigField(
            STRING,
            "REPLICATE_API_TOKEN",
            "TODO_REPLICATE_API_TOKEN"
        )
    }
}


dependencies {
    debugImplementation(compose.uiTooling)
    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

