import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * empty plugin for import dependencies objects
 */
class DependenciesPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        println("Using dependencies plugin")
    }
}

/**
 * versions for build
 */
object Versions {
    const val CompileSdk = 30
    const val TargetSdk = 30
    const val MinSdk = 24
    const val VersionCode = 1
    const val VersionName = "1.0"

    const val KotlinOptionsJvmTarget = "1.8"
    const val ComposeKotlinCompilerExtensionVersion = "1.1.1"
}

/**
 * libraries
 */
object Libs {

    const val KotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10"
    const val Appcompat = "androidx.appcompat:appcompat:1.3.0"
    const val CoreKtx = "androidx.core:core-ktx:1.3.2"
    const val KotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9"
    const val LifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:2.3.1"

    const val Material = "com.google.android.material:material:1.3.0"
    const val Swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    const val Constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.4"
    const val Recyclerview = "androidx.recyclerview:recyclerview:1.2.0"

    const val Okhttp = "com.squareup.okhttp3:okhttp:4.9.0"
    const val Coil = "io.coil-kt:coil:0.11.0"

    const val ComposeUiTooling = "androidx.compose.ui:ui-tooling:1.0.1"
    const val ComposeMaterial = "androidx.compose.material:material:1.0.1"
    const val ComposeAnimation = "androidx.compose.animation:animation:1.0.1"
    const val LifecycleViewmodelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07"

}

/**
 * libraries for test
 */
object TestLibs {

    const val Junit = "junit:junit:4.12"
    const val AndroidxJunit = "androidx.test.ext:junit:1.1.1"
    const val EspressoCore = "androidx.test.espresso:espresso-core:3.2.0"
    const val FragmentTesting = "androidx.fragment:fragment-testing:1.2.5"

}

