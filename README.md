# p4pa-pagopa-payments

This application belong to the **inbound/outbound** tier of the **Piattaforma Unitaria** product.

See [PU Microservice Architecture](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1405845916/Architettura+microservizi) for more details.

## 🧱 Role

* To exchange data with pagoPa `Nodo dei Pagamenti SPC`

## 🌐 APIs
See [OpenAPI](openapi/generated.openapi.json), exposed through the following path:
* `/swagger-ui/index.html`

See [paForNode WSDL](src/main/resources/soap/wsdl/paForNode.wsdl), exposed through the following path:
* `/soap/node/wsdl/PaForNode.wsdl`
* Exposed to pagoPA `Nodo dei Pagamenti SPC` in order to:
  * Returns payments data on synchronous scenario;
  * Accept payment receipts (RT).

See [Postman collection](/postman/PagopaPayments%20E2E.postman_collection.json) and [Postman Environment](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1094615081/Environment+collection+postman).

### 📌 Relevant APIs
* `POST /aca/sync`: To synchronize an Installment with ACA;
* `POST /gpd/sync`: To synchronize an Installment with GPD;
* `GET /taxonomies/fetch`: To fetch taxonomies;
* `GET /paymentsReporting/{organizationId}`: To get payments reporting list of an organization;
* `GET /paymentsReporting/{organizationId}/fetch`: To retrieve a payments reporting file of an organization;
* `GET /printpaymentnotice/generate`: To retrieve a pdf notice related to a single installment;
* `POST /printpaymentnotice/generateMassive`: To submit a request to generate an archive having multiple installment pdf notices;
* `GET /printpaymentnotice/{organizationId}/{folderId}/getSignedUrl`: To obtain a download URL towards the archive having multiple installment pdf notices requested.

### 📌 Relevant SOAP endpoints
* `paVerifyPaymentNotice`: To return payment data on synchronous scenario;
* `paGetPaymentV2`: To confirm payment data on synchronous scenario;
* `paSendRTV2`: To accept the receipt (RT) related to a paid installment.

### 📌 Common HTTP status returned:
* `401`: Invalid access token provided, thus a new login is required;
* `403`: Trying to access a not authorized resource.

## 🌐 AsyncAPIs
See [AsyncAPI](asyncapi/generated.asyncapi.json), exposed through the following path:
* `/springwolf/asyncapi-ui.html`

## 🔎 Monitoring
See available actuator endpoints through the following path:
* `/actuator`

### 📌 Relevant endpoints
* Health (provide an accessToken to see details): `/actuator/health`
  * Liveness: `/actuator/health/liveness`
  * Readiness: `/actuator/health/readiness`
* Metrics: `/actuator/metrics`
  * Prometheus: `/actuator/prometheus`

Further endpoints are exposed through the JMX console.

## ✏️ Logging
See [log configured pattern](/src/main/resources/logback-spring.xml).

## 🔗 Dependencies

### 🧩 Microservices
* [p4pa-auth](https://github.com/pagopa/p4pa-auth):
  * To obtain a technical access token used to perform inner invocations during the handling of pagoPa `Nodo dei Pagamenti SPC` invocations made through the `PaForNode` SOAP endpoints;
* [p4pa-debt-positions](https://github.com/pagopa/p4pa-debt-positions):
  * To retrieve debt position data on synchronous payment scenario;
  * To update notification fee on synchronous payment scenario;
* [p4pa-fileshare](https://github.com/pagopa/p4pa-fileshare):
  * To submit the ingestion of a receipt (RT) obtained from pagoPa `Nodo dei Pagamenti SPC` (WS soap `paForNode`, endpoint `paSendRTV2`);
  * To submit the ingestion of a payments reporting file obtained invoking pagoPa `Nodo dei Pagamenti SPC` (WS soap `nodeForPa`, endpoint `nodoChiediFlussoRendicontazione`);
* [p4pa-organization](https://github.com/pagopa/p4pa-organization):
  * To obtain broker's info and api keys;
  * To obtain organization's info and api keys;
* [p4pa-pu-sil](https://github.com/pagopa/p4pa-pu-sil):
  * On synchronous scenario, to retrieve notification fee (actualization) if the organization has configured a service for a given debt position type;
* [p4pa-send-notification](https://github.com/pagopa/p4pa-send-notification):
  * On synchronous scenario, to retrieve notification fee (actualization) if there is no a service configured on the organization debt position type but there is a SEND api key.
* [p4pa-cie](https://github.com/pagopa/p4pa-cie):
  * To create debt positions in the CIE scenario

### 🌍 External
* pagoPA `Nodo dei Pagamenti SPC` - PagoPA services to handle payments:
  * [nodeForPa WSDL](src/main/resources/soap/wsdl/nodeForPa.wsdl): To fetch payments reporting data;
  * [GPD openAPI](openapi/external/gpd.json): To handle GPD payment integration scenario;
  * [ACA openAPI](openapi/external/paCreatePosition.yaml): To handle ACA payment integration scenario;
  * [Print Payment Notices openApi](openapi/external/pagopa-stampa-avvisi.openapi.json): To retrieve pdf payment notices;
  * [Taxonomy json](https://api.platform.pagopa.it/taxonomy/service/v1/taxonomy): To retrieve taxonomy.

## 🔧 Configuration

See [application.yml](src/main/resources/application.yml) for each configurable property.

### 📌 Relevant configurations

#### 🌐 Application Server
| ENV                           | DESCRIPTION                                                                     | DEFAULT               |
|-------------------------------|---------------------------------------------------------------------------------|-----------------------|
| SERVER_PORT                   | Application server listening port                                               | 8080                  |
| PAGOPA_PAYMENTS_WSDL_BASE_URL | Base URL through which the service is exposed, used to expose the WSDL document | http://localhost:8080 |
| APP_NODE_FOR_PA_URL           | Base URL through which the service is exposed, used to expose the WSDL document |                       |

#### ✏️ Logging
| ENV                                   | DESCRIPTION                                                                                                                                                                     | DEFAULT |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| LOG_LEVEL_ROOT                        | Base level                                                                                                                                                                      | INFO    |
| LOG_LEVEL_PAGOPA                      | Base level of custom classes                                                                                                                                                    | INFO    |
| LOG_LEVEL_SPRING                      | Level applied to Spring framework                                                                                                                                               | INFO    |
| LOG_LEVEL_SPRING_BOOT_AVAILABILITY    | To print availability events                                                                                                                                                    | DEBUG   |
| LOGGING_LEVEL_API_REQUEST_EXCEPTION   | Level applied to APIs exception                                                                                                                                                 | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG             | Level applied to [PerformanceLog](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.-Log-di-performance)                                               | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_API_REQUEST | Level applied to [API Performance Log](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.2.1.-Log-di-perfomance-per-le-API)                            | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_REST_INVOKE | Level applied to [REST invoke Performance Log](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.2.2.-Log-di-performance-per-i-servizi-REST-integrati) | INFO    |

#### 🔁 Integrations

##### 📋 [Caching](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1542128077/Caching)
| ENV                                        | DESCRIPTION                                                                  | DEFAULT |
|--------------------------------------------|------------------------------------------------------------------------------|---------|
| CACHE_BROKER_API_SEGREGATION_CODES_SIZE    | Broker api keys and organization segregation codes cache size                | 100     |
| CACHE_BROKER_API_SEGREGATION_CODES_MINUTES | Broker api keys and organization segregation codes cache retention (minutes) | 60      |
| CACHE_BROKER_API_FISCAL_CODE_SIZE          | Broker api keys and organization data cache size                             | 100     |
| CACHE_BROKER_API_FISCAL_CODE_MINUTES       | Broker api keys and organization data cache retention (minutes)              | 60      |
| CACHE_DEBTPOSITION_TYPE_ORG_SIZE           | DebtPositionTypeOrg data cache size                                          | 1000    |
| CACHE_DEBTPOSITION_TYPE_ORG_MINUTES        | DebtPositionTypeOrg data cache retention (minutes)                           | 60      |
| CACHE_NOTIFICATION_FEE_SIZE                | Notification fee data cache size                                             | 1000    |
| CACHE_NOTIFICATION_FEE_MINUTES             | Notification fee data cache retention (minutes)                              | 10      |
##### 🔗 REST
| ENV                                               | DESCRIPTION                               | DEFAULT |
|---------------------------------------------------|-------------------------------------------|---------|
| DEFAULT_REST_CONNECTION_POOL_SIZE                 | Default connection pool size              | 10      |
| DEFAULT_REST_CONNECTION_POOL_SIZE_PER_ROUTE       | Default connection pool size per route    | 5       |
| DEFAULT_REST_CONNECTION_POOL_TIME_TO_LIVE_MINUTES | Default connection pool TTL (minutes)     | 10      |
| DEFAULT_REST_TIMEOUT_CONNECT_MILLIS               | Default connection timeout (milliseconds) | 120000  |
| DEFAULT_REST_TIMEOUT_READ_MILLIS                  | Default read timeout (milliseconds)       | 120000  |

##### 🧩 Microservices
| ENV                                     | DESCRIPTION                                        | DEFAULT |
|-----------------------------------------|----------------------------------------------------|---------|
| AUTH_BASE_URL                           | Auth microservice URL                              |         |
| AUTH_MAX_ATTEMPTS                       | Auth API max attempts                              | 3       |
| AUTH_WAIT_TIME_MILLIS                   | Auth retry waiting time (milliseconds)             | 500     |
| AUTH_PRINT_BODY_WHEN_ERROR              | To print body when an error occurs                 | true    |
| DEBT_POSITION_BASE_URL                  | DebtPositions microservice URL                     |         |
| DEBT_POSITION_MAX_ATTEMPTS              | DebtPositions API max attempts                     | 3       |
| DEBT_POSITION_WAIT_TIME_MILLIS          | DebtPositions retry waiting time (milliseconds)    | 500     |
| DEBT_POSITION_PRINT_BODY_WHEN_ERROR     | To print body when an error occurs                 | true    |
| FILESHARE_BASE_URL                      | FileShare microservice URL                         |         |
| FILESHARE_MAX_ATTEMPTS                  | FileShare API max attempts                         | 3       |
| FILESHARE_WAIT_TIME_MILLIS              | FileShare retry waiting time (milliseconds)        | 500     |
| FILESHARE_PRINT_BODY_WHEN_ERROR         | To print body when an error occurs                 | true    |
| ORGANIZATION_BASE_URL                   | Organization microservice URL                      |         |
| ORGANIZATION_MAX_ATTEMPTS               | Organization API max attempts                      | 3       |
| ORGANIZATION_WAIT_TIME_MILLIS           | Organization retry waiting time (milliseconds)     | 500     |
| ORGANIZATION_PRINT_BODY_WHEN_ERROR      | To print body when an error occurs                 | true    |
| SEND_NOTIFICATION_BASE_URL              | SendNotification microservice URL                  |         |
| SEND_NOTIFICATION_MAX_ATTEMPTS          | SendNotification API max attempts                  | 3       |
| SEND_NOTIFICATION_WAIT_TIME_MILLIS      | SendNotification retry waiting time (milliseconds) | 500     |
| SEND_NOTIFICATION_PRINT_BODY_WHEN_ERROR | To print body when an error occurs                 | true    |
| PU_SIL_BASE_URL                         | PuSil microservice URL                             |         |
| PU_SIL_MAX_ATTEMPTS                     | PuSil API max attempts                             | 3       |
| PU_SIL_WAIT_TIME_MILLIS                 | PuSil retry waiting time (milliseconds)            | 500     |
| PU_SIL_PRINT_BODY_WHEN_ERROR            | To print body when an error occurs                 | true    |
| CIE_BASE_URL                            | Cie microservice URL                               |         |
| CIE_MAX_ATTEMPTS                        | Cie API max attempts                               | 3       |
| CIE_WAIT_TIME_MILLIS                    | Cie retry waiting time (milliseconds)              | 500     |
| CIE_PRINT_BODY_WHEN_ERROR               | To print body when an error occurs                 | true    |

##### 🌍 External services
| ENV                                                        | DESCRIPTION                                                     | DEFAULT |
|------------------------------------------------------------|-----------------------------------------------------------------|---------|
| PAGOPA_NODE_ACA_BASE_URL                                   | PagoPA ACA service URL                                          |         |
| PAGOPA_NODE_ACA_MAX_ATTEMPTS                               | PagoPA ACA API max attempts                                     | 3       |
| PAGOPA_NODE_ACA_WAIT_TIME_MILLIS                           | PagoPA ACA retry waiting time (milliseconds)                    | 500     |
| PAGOPA_NODE_ACA_PRINT_BODY_WHEN_ERROR                      | To print body when an error occurs                              | true    |
| PAGOPA_NODE_GPD_BASE_URL                                   | PagoPA GPD service URL                                          |         |
| PAGOPA_NODE_GPD_MAX_ATTEMPTS                               | PagoPA GPD API max attempts                                     | 3       |
| PAGOPA_NODE_GPD_WAIT_TIME_MILLIS                           | PagoPA GPD retry waiting time (milliseconds)                    | 500     |
| PAGOPA_NODE_GPD_PRINT_BODY_WHEN_ERROR                      | To print body when an error occurs                              | true    |
| PAGOPA_TAXONOMIES_BASE_URL                                 | PagoPA Taxonomy service URL                                     |         |
| PAGOPA_TAXONOMIES_PRINT_BODY_WHEN_ERROR                    | To print body when an error occurs                              | true    |
| PAGOPA_PRINT_PAYMENT_NOTICE_BASE_URL                       | PagoPA PrintPaymentNotice service URL                           |         |
| PAGOPA_PRINT_PAYMENT_NOTICE_MAX_ATTEMPTS                   | PagoPA PrintPaymentNotice API max attempts                      | 3       |
| PAGOPA_PRINT_PAYMENT_NOTICE_WAIT_TIME_MILLIS               | PagoPA PrintPaymentNotice retry waiting time (milliseconds)     | 500     |
| PAGOPA_PRINT_PAYMENT_NOTICE_PRINT_BODY_WHEN_ERROR          | To print body when an error occurs                              | true    |
| PAGOPA_NODE_SYNC_PAYMENTS_REPORTING_BASE_URL               | PagoPA sync PaymentReporting service URL                        |         |
| PAGOPA_NODE_SYNC_PAYMENTS_REPORTING_MAX_ATTEMPTS           | PagoPA sync PaymentReporting API max attempts                   | 3       |
| PAGOPA_NODE_SYNC_PAYMENTS_REPORTING_WAIT_TIME_MILLIS       | PagoPA sync PaymentReporting retry waiting time (milliseconds)  | 500     |
| PAGOPA_NODE_SYNC_PAYMENTS_REPORTING_PRINT_BODY_WHEN_ERROR  | To print body when an error occurs                              | true    |

##### 🌀 KAFKA
| ENV                                              | DESCRIPTION                                                        | DEFAULT   |
|--------------------------------------------------|--------------------------------------------------------------------|-----------|
| KAFKA_BINDER_BROKER                              | Comma separated list of brokers to which the Kafka binder connects |           |
| KAFKA_CONFIG_HEARTBEAT_INTERVAL_MS               | Hearth beat interval (milliseconds)                                | 3000      |
| KAFKA_CONFIG_SESSION_TIMEOUT_MS                  | Session timeout (milliseconds)                                     | 30000     |
| KAFKA_CONFIG_REQUEST_TIMEOUT_MS                  | Request timeout (milliseconds)                                     | 60000     |
| KAFKA_CONFIG_METADATA_MAX_AGE                    | Metadata max age (milliseconds)                                    | 180000    |
| KAFKA_CONFIG_SASL_MECHANISM                      | SASL mechanism                                                     | PLAIN     |
| KAFKA_CONFIG_SECURITY_PROTOCOL                   | Security protocol                                                  | SASL_SSL  |
| KAFKA_CONFIG_MAX_REQUEST_SIZE                    | Max request size                                                   | 1000000   |

###### 📤 KAFKA PRODUCERS
| ENV                                                | DESCRIPTION                                       | DEFAULT                    |
|----------------------------------------------------|---------------------------------------------------|----------------------------|
| KAFKA_TOPIC_REGISTRIES                             | Topic where to publish registry events            | p4pa-payhub-registries-evh |
| KAFKA_REGISTRIES_PRODUCER_SASL_JAAS_CONFIG         | JAAS Config string used to perform authentication |                            |
| KAFKA_REGISTRIES_PRODUCER_CONNECTION_MAX_IDLE_TIME | Max producer idle time (milliseconds)             | 180000                     |
| KAFKA_REGISTRIES_PRODUCER_RETRY_MS                 | Producer retry waiting time (milliseconds)        | 10000                      |
| KAFKA_REGISTRIES_PRODUCER_LINGER_MS                | Producer linger time (milliseconds)               | 2                          |
| KAFKA_REGISTRIES_PRODUCER_BATCH_SIZE               | Producer batch size                               | 16384                      |

#### 🔑 keys
| ENV                    | DESCRIPTION                                                              | DEFAULT |
|------------------------|--------------------------------------------------------------------------|---------|
| JWT_TOKEN_PUBLIC_KEY   | p4pa-auth JWT public key                                                 |         |
| AUTH_CLIENT_SECRET     | client_secret used on M2M authentication to get a technical access token |         |

## 🛠️ Getting Started

### 📝 Prerequisites

Ensure the following tools are installed on your machine:

1. **Java 21+**
2. **Gradle** (or use the Gradle wrapper included in the repository)
3. **Docker** (to build and run on an isolated environment, optional)

### 🔐 Write Locks

```sh
./gradlew dependencies --write-locks
```

### ⚙️ Build

```sh
./gradlew clean build
```

### 🧪 Test

#### 📌 JUnit
```sh
./gradlew test
```

### 🚀 Run local

```sh
./gradlew bootRun
```

### 🐳 Build & run through Docker
```sh
docker build -t <APP_NAME> .
docker run --env-file <ENV_FILE> <APP_NAME>
```

### ⚖️ Generate dependencies licenses
```sh
./gradlew generateLicenseReport
```
