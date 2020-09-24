object Versions {
    // General
    const val kotlin = "1.3.21"

    // Plugins
    const val androidGradle = "3.6.4"

    // Libs
    const val androidArchitectureComponents = "1.1.0-beta2"
    const val androidSupportLib = "27.1.1"
    const val androidJob = "1.2.5"
    const val kotlinLogging = "1.5.4"
    const val leakCanary = "1.5.4"
    const val multidexVersion = "1.0.3"
    const val rxjava2 = "2.1.12"
    const val rxandroid = "2.0.2"
    const val rxkotlin = "2.2.0"
    const val slf4jAndroidLogger = "1.0.5"

    // Testing
    const val junit = "4.12"
    const val robolectric = "3.8"
    const val assertj = "3.9.1"
    const val mockito = "2.18.0"
}

object Config {
    const val min_sdk = 16
    const val target_sdk = 27
    const val compile_sdk = 27
}

object Plugins {
    val android_gradle = "com.android.tools.build:gradle:${Versions.androidGradle}"
    val kotlin_gradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}

object Libs {
    // Android Architecture Libraries
    val room_runtime = "android.arch.persistence.room:runtime:${Versions.androidArchitectureComponents}"
    val room_rxjava2 = "android.arch.persistence.room:rxjava2:${Versions.androidArchitectureComponents}"
    val room_compiler = "android.arch.persistence.room:compiler:${Versions.androidArchitectureComponents}"

    // Android Support Library
    val support_appcompat_v7 = "com.android.support:appcompat-v7:${Versions.androidSupportLib}"
    val support_preference_v7 = "com.android.support:preference-v7:${Versions.androidSupportLib}"

    // Android-Job
    val android_job = "com.evernote:android-job:${Versions.androidJob}"

    // Kotlin
    val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"

    // LeakCanary
    val leak_canary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    val leak_canary_no_op = "com.squareup.leakcanary:leakcanary-android-no-op:${Versions.leakCanary}"

    // Logging
    val slf4j_android_logger = "de.psdev.slf4j-android-logger:slf4j-android-logger:${Versions.slf4jAndroidLogger}"
    val kotlin_logging = "io.github.microutils:kotlin-logging:${Versions.kotlinLogging}"

    // MultiDex
    val multidex = "com.android.support:multidex:${Versions.multidexVersion}"
    val multidex_instrumentation = "com.android.support:multidex-instrumentation:${Versions.multidexVersion}"

    // RxJava
    val rxjava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxjava2}"
    val rxandroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxandroid}"
    val rxkotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxkotlin}"

    // Testing
    val junit = "junit:junit:${Versions.junit}"
    val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    val assertj = "org.assertj:assertj-core:${Versions.assertj}"
    val mockito = "org.mockito:mockito-core:${Versions.mockito}"
}
