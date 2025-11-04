plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.plugin.compose")
}

android {
	namespace = "com.example.spotifyclone"
	compileSdk = 34

	defaultConfig {
		applicationId = "com.example.spotifyclone"
		minSdk = 24
		targetSdk = 34
		versionCode = 1
		versionName = "1.0"

		vectorDrawables.useSupportLibrary = true
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
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlin {
		jvmToolchain(17)
	}

	buildFeatures {
		compose = true
	}

	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {
	val composeBom = platform("androidx.compose:compose-bom:2024.09.02")
	implementation(composeBom)
	androidTestImplementation(composeBom)

	implementation("androidx.core:core-ktx:1.13.1")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
	implementation("androidx.activity:activity-compose:1.9.3")

	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-graphics")
	implementation("androidx.compose.material3:material3")
	implementation("androidx.compose.ui:ui-tooling-preview")
	debugImplementation("androidx.compose.ui:ui-tooling")
	debugImplementation("androidx.compose.ui:ui-test-manifest")

	implementation("androidx.navigation:navigation-compose:2.8.3")
	implementation("io.coil-kt:coil-compose:2.7.0")

	// Material Components (XML themes)
	implementation("com.google.android.material:material:1.12.0")

	// Media3 ExoPlayer
	implementation("androidx.media3:media3-exoplayer:1.4.1")
	implementation("androidx.media3:media3-ui:1.4.1")
	implementation("androidx.media3:media3-session:1.4.1")

	// Coroutines for state handling
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

	implementation("androidx.compose.material:material-icons-extended:1.5.3")

	// DataStore for persistence
	implementation("androidx.datastore:datastore-preferences:1.1.1")

	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.2.1")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
	androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
