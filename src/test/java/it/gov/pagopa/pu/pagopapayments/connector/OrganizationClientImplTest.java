package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
public class OrganizationClientImplTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  @Mock
  private RestTemplate restTemplateMock;

  private OrganizationClientImpl organizationClient;

  private static final String ORG_BASE_URL = "orgBaseUrl";
  private static final Long VALID_ORG_ID = 1L;
  private static final Long VALID_BROKER_ID = 1L;
  private static final Long INVALID_ORG_ID = 9L;
  private static final String VALID_ACA_KEY = "VALID_ACA_KEY";

  private static final Organization VALID_ORG = new Organization()
    .organizationId(VALID_ORG_ID)
    .brokerId(VALID_BROKER_ID)
    .applicationCode("01");
  private static final BrokerApiKeys VALID_BROKER_API_KEYS = new BrokerApiKeys()
    .acaKey(VALID_ACA_KEY);

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    organizationClient = new OrganizationClientImpl(ORG_BASE_URL, restTemplateBuilderMock);
  }



  @Test
  void givenValidOrganizationWhenGetOrganizationByIdThenOk(){
    //given
    ResponseEntity<Organization> responseEntity = new ResponseEntity<>(VALID_ORG, HttpStatus.OK);
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<Organization>() {})
    )).thenReturn(responseEntity);

    //when
    Organization response = organizationClient.getOrganizationById(VALID_ORG_ID, TestUtils.getFakeAccessToken());

    //verify
    Assertions.assertEquals(VALID_ORG, response);
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<Organization>() {}));
  }

  @Test
  void givenNotFoundOrganizationWhenGetOrganizationByIdThenRestClientException(){
    //given
    ResponseEntity<Organization> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<Organization>() {})
    )).thenReturn(responseEntity);

    //when
    RestClientException exception = Assertions.assertThrows(RestClientException.class,
      () -> organizationClient.getOrganizationById(INVALID_ORG_ID, TestUtils.getFakeAccessToken()));

    //verify
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<Organization>() {}));
  }

  @Test
  void givenApiInvocationErrorWhenGetOrganizationByIdThenRestClientException(){
    //given
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<Organization>() {})
    )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    //when
    HttpServerErrorException exception = Assertions.assertThrows(HttpServerErrorException.class,
      () -> organizationClient.getOrganizationById(INVALID_ORG_ID, TestUtils.getFakeAccessToken()));

    //verify
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<Organization>() {}));
  }

  @Test
  void givenValidBrokerWhenGetAcaApiKeyByBrokerIdThenOk(){
    //given
    ResponseEntity<BrokerApiKeys> responseEntity = new ResponseEntity<>(VALID_BROKER_API_KEYS, HttpStatus.OK);
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<BrokerApiKeys>() {})
    )).thenReturn(responseEntity);

    //when
    BrokerApiKeys response = organizationClient.getApiKeyByBrokerId(VALID_BROKER_ID, TestUtils.getFakeAccessToken());

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(VALID_ACA_KEY, response.getAcaKey());
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<BrokerApiKeys>() {}));
  }
}
