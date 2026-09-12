import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.oqba26.jafr"
    compileSdk = 35

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    val localPropertiesFile = rootProject.file("local.properties")
    val localProperties = Properties()
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    fun getProp(name: String): String? {
        return System.getenv(name) ?: project.findProperty(name) as? String ?: (keystoreProperties[name] as? String) ?: (localProperties[name] as? String)
    }

    defaultConfig {
        applicationId = "com.oqba26.jafr"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "1.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val supabaseUrl = getProp("SUPABASE_URL") ?: "https://ftufsygeartwukonclkz.supabase.co"
        val supabaseKey = getProp("SUPABASE_KEY") ?: "sb_publishable_H4mAU1Ds-vZ22HwMSfCaBQ_QjBfmRd5"

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
    }

    signingConfigs {
        create("release") {
            val storeFileProp = getProp("RELEASE_STORE_FILE")
            val storePasswordProp = getProp("RELEASE_STORE_PASSWORD")
            val keyAliasProp = getProp("RELEASE_KEY_ALIAS")
            val keyPasswordProp = getProp("RELEASE_KEY_PASSWORD")

            val signingCredentialsSet = listOf(
                storeFileProp, storePasswordProp, keyAliasProp, keyPasswordProp
            ).all { !it.isNullOrEmpty() }

            if (signingCredentialsSet) {
                storeFile = file(storeFileProp!!)
                storePassword = storePasswordProp
                keyAlias = keyAliasProp
                keyPassword = keyPasswordProp
            }
            // اگر اعتبارنامه‌های امضا (env var / gradle property / keystore.properties) تنظیم نشده
            // باشند، عمداً هیچ مقدار fallback هاردکد شده‌ای ست نمی‌شود تا بیلد release با خطای
            // شفاف AGP شکست بخورد؛ بیلدهای debug تحت تأثیر قرار نمی‌گیرند.
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.persiandate)
    implementation(libs.datastore)
    implementation(libs.gson)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)

    // Ktor + kotlinx.serialization for UpdateManager
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}