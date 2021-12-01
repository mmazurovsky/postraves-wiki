import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.spring") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.20"
    id("nu.studer.jooq") version "5.2.1"
    id("org.flywaydb.flyway") version "7.11.1"
    id("co.uzzu.dotenv.gradle") version "1.2.0"
}

group = "com.postraves.wiki" //changed from com.postraves.backend
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework:spring-jdbc:5.3.9")
    implementation("org.projectlombok:lombok:1.18.18")
    implementation("com.google.firebase:firebase-admin:8.0.1")
    implementation("org.jooq:jooq:3.14.14")
//    jooqGenerator("org.jooq:jooq-meta-extensions:3.14.14")
    jooqGenerator("org.postgresql:postgresql:42.2.18")
    runtimeOnly("org.postgresql:postgresql:42.2.18")
    implementation("org.flywaydb:flyway-core:7.1.1")
    implementation("io.lettuce:lettuce-core:6.1.3.RELEASE")
    testImplementation("it.ozimov:embedded-redis:0.7.3") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }
    testImplementation("org.testcontainers:postgresql:1.15.3")
    testImplementation("org.testcontainers:junit-jupiter:1.15.3")
    implementation(kotlin("test"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("io.github.microutils:kotlin-logging:1.12.5")
    testImplementation("org.mockito:mockito-inline:3.11.2")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    languageVersion = "1.4"
    jvmTarget = "11"
}

tasks.test {
    useJUnitPlatform()
}

val POSTGRES_USER = System.getenv("POSTGRES_USER")
val POSTGRES_PASSWORD = System.getenv("POSTGRES_PASSWORD")
val POSTGRES_HOST = System.getenv("POSTGRES_HOST")
val POSTGRES_URL = "jdbc:postgresql://${POSTGRES_HOST}:5432/postraves"

//tasks.register("envvars", type = JavaExec::class) {
//    env.allVariables.forEach { environment(it.key, it.value) }
//}

//tasks.named("flywayMigrate") {
//    dependsOn()
//}

//tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
//    dependsOn(":envvars")
//}

flyway {
    url = POSTGRES_URL
    user = POSTGRES_USER
    password = POSTGRES_PASSWORD
    schemas = arrayOf("public")
}

jooq {
    version.set("3.14.14")
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

    configurations {
        create("main") {  // name of the jOOQ configuration
            // to generate files on each project build or not
            generateSchemaSourceOnCompilation.set(false)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = POSTGRES_URL
                    user = POSTGRES_USER
                    password = POSTGRES_PASSWORD
                    // INFO I changed ssl value to false manually
                    properties.add(org.jooq.meta.jaxb.Property().withKey("ssl").withValue("false"))
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
//                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
//                        properties.add(org.jooq.meta.jaxb.Property().withKey("scripts").withValue("/src/main/resources/db/migration/postgres"))
//                        properties.add(org.jooq.meta.jaxb.Property().withKey("unqualifiedSchema").withValue("public"))
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

//tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
//    // make jOOQ task participate in incremental builds (which is also a prerequisite for participating in build caching)
//    allInputsDeclared.set(true)
//
//    // make jOOQ task participate in build caching
//    outputs.cacheIf { true }
//}