plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	id("org.sonarqube") version "6.0.1.5171"
	id("com.github.ben-manes.versions") version "0.51.0"
	id("org.openapi.generator") version "7.10.0"
  id("org.ajoberstar.grgit") version "5.3.0"
  //code generation for soap webservices classes (via jaxb)
  id("com.intershop.gradle.jaxb") version "7.0.1"
}

group = "it.gov.pagopa.payhub"
version = "0.0.1"
description = "p4pa-pagopa-payments"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val springDocOpenApiVersion = "2.7.0"
val openApiToolsVersion = "0.2.6"
val micrometerVersion = "1.4.1"
val bouncycastleVersion = "1.79"
val jaxbVersion = "4.0.5"
val jaxbApiVersion = "4.0.2"
val activationVersion = "2.1.3"
val wsdl4jVersion = "1.6.3"
val xmlSchemaVersion = "2.3.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-web-services")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")

  //webservice soap
  implementation("wsdl4j:wsdl4j:$wsdl4jVersion")
  implementation("org.apache.ws.xmlschema:xmlschema-core:$xmlSchemaVersion")
  runtimeOnly("org.glassfish.jaxb:jaxb-runtime:$jaxbVersion")
  //jaxb
  jaxb("org.glassfish.jaxb:jaxb-runtime:$jaxbVersion")
  jaxb("com.sun.xml.bind:jaxb-xjc:$jaxbVersion")
  jaxb("com.sun.xml.bind:jaxb-jxc:$jaxbVersion")
  jaxb("com.sun.xml.bind:jaxb-core:$jaxbVersion")
  jaxb("jakarta.xml.bind:jakarta.xml.bind-api:$jaxbApiVersion")
  jaxb("jakarta.activation:jakarta.activation-api:$activationVersion")
  jaxbext("org.jvnet.jaxb:jaxb-plugin-annotate:3.0.2")
  jaxbext("org.slf4j:slf4j-simple:2.0.16") // see https://github.com/IntershopCommunicationsAG/jaxb-gradle-plugin/issues/37

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

  //security
  implementation("org.bouncycastle:bcprov-jdk18on:$bouncycastleVersion")

  //	Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito:mockito-core")
	testImplementation ("org.projectlombok:lombok")
  testImplementation("uk.co.jemos.podam:podam:8.0.2.RELEASE")

}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required = true
	}
}

val projectInfo = mapOf(
	"artifactId" to project.name,
	"version" to project.version
)

tasks {
	val processResources by getting(ProcessResources::class) {
		filesMatching("**/application.yml") {
			expand(projectInfo)
		}
	}
}

configurations {
	compileClasspath {
		resolutionStrategy.activateDependencyLocking()
	}
}

tasks.compileJava {
  dependsOn("dependenciesBuild")
}

tasks.register("dependenciesBuild") {
  group = "AutomaticallyGeneratedCode"
  description = "grouping all together automatically generate code tasks"

  dependsOn(
    "openApiGenerateP4PAAUTH",
    "openApiGenerate",
    "openApiGenerateDEBTPOSITIONS",
    "openApiGenerateORGANIZATION",
    "openApiGenerateFILESHARE",
    "openApiGeneratePaCreatePosition",
    "jaxbJavaGenPaForNode"
  )
}

configure<SourceSetContainer> {
	named("main") {
		java.srcDir("$projectDir/build/generated/src/main/java")
	}
}

springBoot {
	mainClass.value("it.gov.pagopa.pu.pagopapayments.PagoPaPaymentsApplication")
}

var targetEnv = when (grgit.branch.current().name) {
  "uat" -> "uat"
  "main" -> "main"
  else -> "develop"
}

openApiGenerate {
  generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/p4pa-pagopa-payments.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.pagopapayments.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.pagopapayments.dto.generated")
  configOptions.set(mapOf(
    "dateLibrary" to "java8",
    "requestMappingMode" to "api_interface",
    "useSpringBoot3" to "true",
    "interfaceOnly" to "true",
    "useTags" to "true",
    "useBeanValidation" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "additionalModelTypeAnnotations" to "@lombok.Builder"
  ))
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateP4PAAUTH") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-auth/refs/heads/$targetEnv/openapi/p4pa-auth.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.auth.generated")
  apiPackage.set("it.gov.pagopa.pu.auth.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.auth.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "false",
    "generatedConstructorWithRequiredArgs" to "true",
    "additionalModelTypeAnnotations" to "@lombok.Data @lombok.Builder @lombok.AllArgsConstructor"
  ))
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateDEBTPOSITIONS") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-debt-positions/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.debtpositions.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.debtpositions.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true"
  ))
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateORGANIZATION") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-organization/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.organization.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.organization.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true"
  ))
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateFILESHARE") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-fileshare/refs/heads/$targetEnv/openapi/p4pa-fileshare.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.fileshare.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.fileshare.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "useAbstractionForFiles" to "true"
  ))
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGeneratePaCreatePosition") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  inputSpec.set("$rootDir/openapi/paCreatePosition.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.nodo.pacreateposition.controller.generated")
  modelPackage.set("it.gov.pagopa.nodo.pacreateposition.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true"
  ))
  library.set("resttemplate")
}

jaxb {
  javaGen {
    register("paForNode") {
      extension = true
      args = listOf("-wsdl", "-Xannotate")
      outputDir = file("$projectDir/build/generated/jaxb/java")
      schema = file("$rootDir/src/main/resources/soap/wsdl/paForNode.wsdl")
      bindings = layout.files("$rootDir/src/main/resources/soap/wsdl/paForNode.xjb")
    }
    register("nodeForPa") {
      args = listOf("-wsdl")
      outputDir = file("$projectDir/build/generated/jaxb/java")
      schema = file("$rootDir/src/main/resources/soap/wsdl/nodeForPa.wsdl")
      bindings = layout.files("$rootDir/src/main/resources/soap/wsdl/nodeForPa.xjb")
    }
  }
}

