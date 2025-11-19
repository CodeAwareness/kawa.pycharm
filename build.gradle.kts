plugins {
    id("java")
	id("org.jetbrains.intellij") version "1.17.4"
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
}

group = "com.codeawareness"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

// Exclude Kotlin stdlib to avoid conflicts with IntelliJ Platform version
configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
}

// Configure Gradle IntelliJ Plugin
intellij {
    version.set("2023.3")
    type.set("IC") // Community Edition
    plugins.set(listOf())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set(provider { null })
        
        // Plugin version (uses project.version by default)
        version.set(project.version.toString())
        
        // Change notes for marketplace (update for each release)
        changeNotes.set("""
            <h3>Initial Release - 1.0.0</h3>
            <ul>
                <li>Real-time peer code highlighting - See which lines your teammates are modifying</li>
                <li>Conflict detection - Identify merge conflicts before they happen</li>
                <li>Overlap detection - Find overlapping changes across team members</li>
                <li>Side-by-side diff viewing - Compare your code with teammates' versions</li>
                <li>Branch comparison - Compare your working copy against other branches</li>
                <li>Low-noise design - Non-intrusive visual indicators</li>
            </ul>
        """.trimIndent())
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    test {
        useJUnitPlatform()
    }
}
