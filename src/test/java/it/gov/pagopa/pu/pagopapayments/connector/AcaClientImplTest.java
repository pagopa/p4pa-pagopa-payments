package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.DebtPositionResponse;
import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
public class AcaClientImplTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  @Mock
  private RestTemplate restTemplateMock;

  private AcaClientImpl acaClient;

  private static final String ORG_BASE_URL = "orgBaseUrl";

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    acaClient = new AcaClientImpl(ORG_BASE_URL, restTemplateBuilderMock);
  }


  @Test
  void givenValidDebtPositionWhenPaCreatePositionThenOk(){
    //given
    DebtPositionResponse validResponse = new DebtPositionResponse();
    ResponseEntity<DebtPositionResponse> responseEntity = new ResponseEntity<>(validResponse, HttpStatus.OK);
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<DebtPositionResponse>() {})
    )).thenReturn(responseEntity);

    //when
    DebtPositionResponse response = acaClient.paCreatePosition(new NewDebtPositionRequest(), "apiKey", "01");

    //verify
    Assertions.assertEquals(validResponse, response);
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<DebtPositionResponse>() {}));
  }

  @Test
  void givenApiInvocationErrorWhenPaCreatePositionThenRestClientException(){
    //given
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<DebtPositionResponse>() {})
    )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    //when
    HttpServerErrorException exception = Assertions.assertThrows(HttpServerErrorException.class,
      () -> acaClient.paCreatePosition(new NewDebtPositionRequest(), "apiKey", "01"));

    //verify
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<DebtPositionResponse>() {}));
  }

}
