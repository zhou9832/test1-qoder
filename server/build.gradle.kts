plugins {
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("java")
}

group = "com.taskboard"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

val springBootVersion = "4.0.1"

dependencies {
    // Web starter (includes Tomcat)
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    
    // H2 Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    runtimeOnly("com.h2database:h2")
    
    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.test {
    useJUnitPlatform()
}
