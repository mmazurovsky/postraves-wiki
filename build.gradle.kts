import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.spring") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.20"
    id("nu.studer.jooq") version "5.2.1"
//    id("org.flywaydb.flyway") version "7.11.1"
}

group = "com.postraves.backend"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.projectlombok:lombok:1.18.18")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation ("com.google.firebase:firebase-admin:7.1.0")
    jooqGenerator("org.postgresql:postgresql:42.2.14")
    implementation("org.jooq:jooq:3.14.12")
    runtimeOnly("com.h2database:h2:1.4.200")
//    implementation("io.zonky.test:embedded-postgres:1.3.0")
    runtimeOnly ("org.postgresql:postgresql:42.2.18")
    compileOnly("org.flywaydb:flyway-core:7.1.1")
    implementation("org.springframework:spring-jdbc:5.3.8")
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
//    implementation("org.springframework.data:spring-data-jpa:2.5.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

//flyway {
//    url = "jdbc:postgresql://localhost:5432/postraves"
//    user = "mmazurovsky"
//    password = System.getenv("LOCAL_PG_PASSWORD")
//    schemas = arrayOf("public")
//}

buildscript {
    configurations["classpath"].resolutionStrategy.eachDependency {
        if (requested.group == "org.jooq") {
            useVersion("3.14.12")
        }
    }
}

jooq {
    version.set("3.14.12")  // default (can be omitted)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)  // default (can be omitted)

    configurations {
        create("main") {  // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(true)  // default (can be omitted)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/postraves"
                    user = "mmazurovsky"
                    password = System.getenv("LOCAL_PG_PASSWORD")
                    // TODO I changed ssl value to false manually
                    properties.add(org.jooq.meta.jaxb.Property().withKey("ssl").withValue("false"))
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
//                        forcedTypes.addAll(arrayOf(
//                            org.jooq.meta.jaxb.ForcedType()
//                                .withName("varchar")
//                                .withIncludeExpression(".*")
//                                .withIncludeTypes("JSONB?"),
//                            org.jooq.meta.jaxb.ForcedType()
//                                .withName("varchar")
//                                .withIncludeExpression(".*")
//                                .withIncludeTypes("INET")
//                        ).toList())
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = false
                        // I changed it to false in order to build
                        isFluentSetters = false
                    }
                    target.apply {
                        packageName = "jooq"
                        directory = "src/main/kotlin/com/postraves/backend/postraveswiki/generated"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}
