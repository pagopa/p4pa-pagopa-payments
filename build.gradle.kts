import java.util.*
import com.github.jk1.license.render.*
import com.github.jk1.license.filter.*
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  java
  id("org.springframework.boot") version "4.0.0"
  id("io.spring.dependency-management") version "1.1.7"
  jacoco
  id("org.sonarqube") version "7.2.1.6560"
  id("com.github.ben-manes.versions") version "0.53.0"
  id("org.openapi.generator") version "7.17.0"
  id("org.ajoberstar.grgit") version "5.3.2"
  //code generation for soap webservices classes (via jaxb)
  id("com.intershop.gradle.jaxb") version "8.0.1"
  id("com.gorylenko.gradle-git-properties") version "2.5.4"
  id("com.github.jk1.dependency-license-report") version "3.0.1"
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
  compileClasspath {
    resolutionStrategy.activateDependencyLocking()
  }
}

licenseReport {
  renderers =
    arrayOf(XmlReportRenderer("third-party-libs.xml", "Back-End Libraries"))
  outputDir = "$projectDir/dependency-licenses"
  filters = arrayOf(SpdxLicenseBundleNormalizer())
}
tasks.classes {
  finalizedBy(tasks.generateLicenseReport)
}

repositories {
  mavenCentral()
}

val springDocOpenApiVersion = "3.0.0"
val janinoVersion = "3.1.12"
val openApiToolsVersion = "0.2.8"
val micrometerVersion = "1.6.1"
val bouncycastleVersion = "1.83"
val jaxbVersion = "4.0.6"
val jaxbApiVersion = "4.0.4"
val activationVersion = "2.1.4"
val wsdl4jVersion = "1.6.3"
val xmlSchemaVersion = "2.3.2"
val podamVersion = "8.0.2.RELEASE"
val caffeineVersion = "3.2.3"
val httpClientVersion = "5.5.1"
val springWolfAsyncApiVersion = "1.20.0"
val commonsLang3Version = "3.20.0"
val springCloudDepsVersion = "2025.1.0"
val lz4JavaVersion = "1.10.1"

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudDepsVersion")
  }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
  implementation("org.springframework.boot:spring-boot-starter-restclient")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.data:spring-data-commons")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-web-services")
  implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka") {
    exclude(group = "org.lz4", module = "lz4-java")
  }
  implementation("at.yawk.lz4:lz4-java:$lz4JavaVersion")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion") {
    exclude(group = "org.apache.commons", module = "commons-lang3")
  }
  implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
  implementation("org.codehaus.janino:janino:$janinoVersion")
  implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")
  implementation("org.bouncycastle:bcprov-jdk18on:$bouncycastleVersion")
  implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
  implementation("org.apache.httpcomponents.client5:httpclient5:$httpClientVersion")
  implementation("io.github.springwolf:springwolf-kafka:${springWolfAsyncApiVersion}") {
    exclude(group = "org.lz4", module = "lz4-java")
  }
  implementation("io.github.springwolf:springwolf-ui:${springWolfAsyncApiVersion}")
  implementation("io.github.springwolf:springwolf-cloud-stream:${springWolfAsyncApiVersion}")

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
  testAnnotationProcessor("org.projectlombok:lombok")

  //	Testing
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-security-test")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.projectlombok:lombok")
  testImplementation("uk.co.jemos.podam:podam:$podamVersion")

}

tasks.withType<Test> {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
  mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}
tasks {
  test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    testLogging.events = setOf(TestLogEvent.FAILED)
    testLogging.exceptionFormat = TestExceptionFormat.FULL
  }
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

tasks.compileJava {
  dependsOn("dependenciesBuild")
}

tasks.register("dependenciesBuild") {
  group = "AutomaticallyGeneratedCode"
  description = "grouping all together automatically generate code tasks"

  dependsOn(
    "openApiGenerate",
    "openApiGenerateP4PAAUTH",
    "openApiGenerateDEBTPOSITIONS",
    "openApiGenerateORGANIZATION",
    "openApiGenerateFILESHARE",
    "openApiGeneratePaCreatePosition",
    "openApiGenerateGPD",
    "openApiGenerateACA",
    "openApiGenerateNodeFdrOrganization",
    "openApiGeneratePrintPaymentNoticeClient",
    "openApiGenerateSENDNOTIFICATION",
    "openApiGenerateREGISTRIES",
    "openApiGenerateWORKFLOWHUB",
    "openApiGeneratePUSIL",
    "jaxbJavaGenPaForNode",
    "jaxbJavaGenNodeForPa",
    "jaxbJavaGenFlussoRiversamento",
    "openApiGenerateForOrgs"
  )
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("$projectDir/build/generated/src/main/java")
  }
}

springBoot {
  buildInfo()
  mainClass.value("it.gov.pagopa.pu.pagopapayments.PagoPaPaymentsApplication")
}

openApiGenerate {
generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/p4pa-pagopa-payments.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.pagopapayments.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.pagopapayments.dto.generated")
  typeMappings.set(
    mapOf(
      "DebtPositionDTO" to "it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO",
      "InstallmentStatus" to "it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO.StatusEnum"
    )
  )
  configOptions.set(
    mapOf(
      "dateLibrary" to "java8",
      "requestMappingMode" to "api_interface",
      "useSpringBoot3" to "true",
      "interfaceOnly" to "true",
      "useTags" to "true",
      "useBeanValidation" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
}

var targetEnv = when (Objects.requireNonNullElse(
  System.getProperty("targetBranch"),
  grgit.branch.current().name
)) {
  "uat" -> "uat"
  "main" -> "main"
  else -> "develop"
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
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
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
  typeMappings.set(
    mapOf(
      "LocalDateTime" to "java.time.LocalDateTime"
    )
  )
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
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
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
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
  typeMappings.set(
    mapOf(
      "StartNotificationResponse" to "String"
    )
  )
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "useAbstractionForFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
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
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateGPD") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  inputSpec.set("$rootDir/openapi/gpd.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.nodo.gpd.controller.generated")
  modelPackage.set("it.gov.pagopa.nodo.gpd.dto.generated")
  typeMappings.set(
    mapOf(
      "DateTime" to "String" //GPD needs LocalDateTime instead of OffsetDateTime autogenerated, mapping to String because, for the same reason, we configured the ObjectMapper in order to always transform LocalDateTime into OffsetDateTime
    )
  )
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateACA") {
  group = "openapi"
  description = "Generate ACA client from GPD v1 spec"

  generatorName.set("java")
  inputSpec.set("$rootDir/openapi/aca-gpd-v1.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.aca.gpd.v1.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.aca.gpd.v1.dto.generated")
  invokerPackage.set("it.gov.pagopa.pu.aca.gpd.v1.generated")
  typeMappings.set(
    mapOf(
      "DateTime" to "String"
    )
  )

  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )

  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateNodeFdrOrganization") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  inputSpec.set("$rootDir/openapi/fdr_organization.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.nodo.fdrorganization.controller.generated")
  modelPackage.set("it.gov.pagopa.nodo.fdrorganization.dto.generated")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGeneratePrintPaymentNoticeClient") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  inputSpec.set("$rootDir/openapi/pagopa-stampa-avvisi.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.api")
  modelPackage.set("it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto")
  modelNameSuffix.set("DTO")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true"
    )
  )
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateSENDNOTIFICATION") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-send-notification/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.sendnotification.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.sendnotification.dto.generated")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateREGISTRIES") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-registries/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.registries.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.registries.dto.generated")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGeneratePUSIL") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-pu-sil/refs/heads/$targetEnv/openapi/generated-internal.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.pusil.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.pusil.dto.generated")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateWORKFLOWHUB") {
  group = "openapi"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-workflow-hub/refs/heads/$targetEnv/openapi/p4pa-workflow-hub.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.workflowhub.generated")
  apiPackage.set("it.gov.pagopa.pu.workflowhub.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.workflowhub.dto.generated")
  typeMappings.set(
    mapOf(
      "DebtPositionDTO" to "it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO",
      "IngestionFlowFileType" to "String",
      "WfExecutionConfig" to "tools.jackson.databind.JsonNode",
      "ExportFileType" to "String",
      "WorkflowTypeOrg" to "String",
      "ScheduleEnum" to "String",
      "WorkflowExecutionStatus" to "String"
    )
  )
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateForOrgs") {
  group = "openapi"
  description = "description"

  generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/openapiForOrgs.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.fororgs.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.fororgs.dto.generated")
  typeMappings.set(
    mapOf(
      "DateTime" to "String"
    )
  )
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "interfaceOnly" to "true",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("spring-boot")
}

jaxb {
  javaGen {
    register("paForNode") {
      extension = true
      args = listOf("-wsdl", "-Xannotate")
      outputDir = file("$projectDir/build/generated/jaxb/java")
      schema = file("$rootDir/src/main/resources/soap/wsdl/paForNode.wsdl")
      bindings =
        layout.files("$rootDir/src/main/resources/soap/wsdl/paForNode.xjb")
    }
    register("nodeForPa") {
      args = listOf("-wsdl")
      outputDir = file("$projectDir/build/generated/jaxb/java")
      schema = file("$rootDir/src/main/resources/soap/wsdl/nodeForPa.wsdl")
      bindings =
        layout.files("$rootDir/src/main/resources/soap/wsdl/nodeForPa.xjb")
    }
    register("FlussoRiversamento") {
      extension = true
      args = listOf("-wsdl")
      outputDir = file("$projectDir/build/generated/jaxb/java")
      schema = file("src/main/resources/soap/xsd-pu/FlussoRiversamento.xsd")
      bindings = layout.files("src/main/resources/soap/xsd-pu/FlussoRiversamento.xjb")
    }
  }
}

