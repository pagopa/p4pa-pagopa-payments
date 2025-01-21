package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.fileshare.dto.generated.UploadIngestionFlowFileResponseDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
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
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class FileShareClientImplTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;
  @Mock
  private RestTemplate restTemplateMock;
  private FileShareClientImpl fileShareClient;

  private final PodamFactory podamFactory;

  FileShareClientImplTest() {
    this.podamFactory = TestUtils.getPodamFactory();
  }

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    fileShareClient = new FileShareClientImpl("fileShareBaseUrl", restTemplateBuilderMock);
  }

  //region uploadRt

  @Test
  void givenValidRtWhenUploadRtThenOk() {
    //given
    String expectedIngestionFlowId = "ingestionFlowId";
    ResponseEntity<UploadIngestionFlowFileResponseDTO> responseEntity = new ResponseEntity<>(new UploadIngestionFlowFileResponseDTO()
      .ingestionFlowFileId(expectedIngestionFlowId),HttpStatus.OK);
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<UploadIngestionFlowFileResponseDTO>() {
      })
    )).thenReturn(responseEntity);

    PaSendRtDTO paSendRtDTO = podamFactory.manufacturePojo(PaSendRtDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    //when
    String ingestionFlowId = fileShareClient.uploadRt(paSendRtDTO, organization, TestUtils.getFakeAccessToken());

    //verify
    Assertions.assertEquals(expectedIngestionFlowId, ingestionFlowId);
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<UploadIngestionFlowFileResponseDTO>() {
      }));
  }

  @Test
  void givenNotFoundRtWhenUploadRtThenRestClientException() {
    //given
    ResponseEntity<UploadIngestionFlowFileResponseDTO> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<UploadIngestionFlowFileResponseDTO>() {
      })
    )).thenReturn(responseEntity);

    PaSendRtDTO paSendRtDTO = podamFactory.manufacturePojo(PaSendRtDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    //when
    Assertions.assertThrows(RestClientException.class,
      () -> fileShareClient.uploadRt(paSendRtDTO, organization, TestUtils.getFakeAccessToken()));

    //verify
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<UploadIngestionFlowFileResponseDTO>() {
      }));
  }

  @Test
  void givenApiInvocationErrorWhenUploadRtThenRestClientException() {
    //given
    Mockito.when(restTemplateMock.exchange(
      Mockito.any(RequestEntity.class),
      Mockito.eq(new ParameterizedTypeReference<UploadIngestionFlowFileResponseDTO>() {
      })
    )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    PaSendRtDTO paSendRtDTO = podamFactory.manufacturePojo(PaSendRtDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    //when
    HttpServerErrorException exception = Assertions.assertThrows(HttpServerErrorException.class,
      () -> fileShareClient.uploadRt(paSendRtDTO, organization, TestUtils.getFakeAccessToken()));

    //verify
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    Mockito.verify(restTemplateMock, Mockito.times(1))
      .exchange(Mockito.any(RequestEntity.class), Mockito.eq(new ParameterizedTypeReference<UploadIngestionFlowFileResponseDTO>() {
      }));
  }

  //endregion
}
