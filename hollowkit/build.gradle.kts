plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.funnywolf.hollowkit.dependencies")
}

group = "com.github.funnywolfdadada"

android {

    defaultConfig {
        minSdk = Versions.MinSdk
        targetSdk = Versions.TargetSdk
        compileSdk = Versions.CompileSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(Libs.KotlinStdlib)
    api(Libs.Appcompat)
    api(Libs.CoreKtx)

    api(Libs.KotlinxCoroutinesAndroid)

    api(Libs.Constraintlayout)
    api(Libs.Recyclerview)
    api(Libs.Okhttp)

    testImplementation(TestLibs.Junit)
    androidTestImplementation(TestLibs.AndroidxJunit)
    androidTestImplementation(TestLibs.EspressoCore)
    debugImplementation(TestLibs.FragmentTesting)
}
