plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.funnywolf.hollowkit.dependencies")
}

android {
    defaultConfig {
        applicationId = "com.funnywolf.hollowkit"
        minSdk = Versions.MinSdk
        targetSdk = Versions.TargetSdk
        compileSdk = Versions.CompileSdk
        versionCode = Versions.VersionCode
        versionName = Versions.VersionName
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = Versions.KotlinOptionsJvmTarget
    }

    viewBinding {
        isEnabled = true
    }

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.ComposeKotlinCompilerExtensionVersion
    }
}

dependencies {
    api(project(":hollowkit"))

    implementation(Libs.KotlinStdlib)
    implementation(Libs.Appcompat)
    implementation(Libs.CoreKtx)
    implementation(Libs.LifecycleRuntimeKtx)

    implementation(Libs.Material)
    implementation(Libs.Recyclerview)
    implementation(Libs.Constraintlayout)
    implementation(Libs.Swiperefreshlayout)

    implementation(Libs.Coil)

    implementation(Libs.KotlinxCoroutinesAndroid)

    // jetpack compose
    implementation(Libs.ComposeUiTooling)
    implementation(Libs.ComposeMaterial)
    implementation(Libs.ComposeAnimation)
    implementation(Libs.LifecycleViewmodelCompose)

    testImplementation(TestLibs.Junit)
    androidTestImplementation(TestLibs.AndroidxJunit)
    androidTestImplementation(TestLibs.EspressoCore)
    debugImplementation(TestLibs.FragmentTesting)
}
