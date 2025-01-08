package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.p4pa_debt_positions.dto.generated.DebtPositionTypeOrg;
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
public class DebtPositionClientImplTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  @Mock
  private RestTemplate restTemplateMock;

  private DebtPositionClientImpl debtPositionClient;

  private static final String ORG_BASE_URL = "orgBaseUrl";
  private static final Long VALID_DEBT_POSITION_TYPE_ORG_ID = 1L;
  private static final String VALID_DEBT_POSITION_TYPE_ORG_DESCR = "description";
  private static final Boolean VALID_DEBT_POSITION_TYPE_ORG_MANDATORY_DUE_DATE = true;
  private static final Long INVALID_DEBT_POSITION_TYPE_ORG_ID = 9L;
  private static final DebtPositionTypeOrg VALID_DEBT_POSITION_TYPE_ORG = new DebtPositionTypeOrg()
    .debtPositionTypeOrgId(VALID_DEBT_POSITION_TYPE_ORG_ID)
    .description(VALID_DEBT_POSITION_TYPE_ORG_DESCR)
    .flagMandatoryDueDate(VALID_DEBT_POSITION_TYPE_ORG_MANDATORY_DUE_DATE);

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    debtPositionClient = new DebtPositionClientImpl(ORG_BASE_URL, restTemplateBuilderMock);
  }



  @Test
  void givenValidDebtPositionTypeOrgIdWhenGetDebtPositionTypeOrgByIdThenOk(){
    //given
    ResponseEntity<DebtPositionTypeOrg> responseEntity = new ResponseEntity<>(VALID_DEBT_POSITION_TYPE_ORG, HttpStatus.OK);
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<DebtPositionTypeOrg>() {})
    )).thenReturn(responseEntity);

    //when
    DebtPositionTypeOrg response = debtPositionClient.getDebtPositionTypeOrgById(VALID_DEBT_POSITION_TYPE_ORG_ID, TestUtils.getFakeAccessToken());

    //verify
    Assertions.assertEquals(VALID_DEBT_POSITION_TYPE_ORG, response);
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<DebtPositionTypeOrg>() {}));
  }

  @Test
  void givenNotFoundDebtPositionTypeOrgIdWhenGetDebtPositionTypeOrgByIdThenRestClientException(){
    //given
    ResponseEntity<DebtPositionTypeOrg> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<DebtPositionTypeOrg>() {})
    )).thenReturn(responseEntity);

    //when
    RestClientException exception = Assertions.assertThrows(RestClientException.class,
      () -> debtPositionClient.getDebtPositionTypeOrgById(INVALID_DEBT_POSITION_TYPE_ORG_ID, TestUtils.getFakeAccessToken()));

    //verify
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<DebtPositionTypeOrg>() {}));
  }

  @Test
  void givenApiInvocationErrorWhenGetDebtPositionTypeOrgByIdThenRestClientException(){
    //given
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<DebtPositionTypeOrg>() {})
    )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    //when
    HttpServerErrorException exception = Assertions.assertThrows(HttpServerErrorException.class,
      () -> debtPositionClient.getDebtPositionTypeOrgById(INVALID_DEBT_POSITION_TYPE_ORG_ID, TestUtils.getFakeAccessToken()));

    //verify
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<DebtPositionTypeOrg>() {}));
  }

}
