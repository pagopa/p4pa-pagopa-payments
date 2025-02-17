package it.gov.pagopa.pu.pagopapayments.connector.fileshare;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.fileshare.client.FileShareClient;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileShareServiceTest {

  @Mock
  private FileShareClient clientMock;

  private FileShareService service;

  @BeforeEach
  void init(){
    service = new FileShareServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenUploadRtThenInvokeClient(){
    // Given
    PaSendRtDTO paSendRtDTO = new PaSendRtDTO();
    Organization organization = new Organization();
    String accessToken = "ACCESSTOKEN";
    Long expectedResult = 1L;

    Mockito.when(clientMock.uploadRt(Mockito.same(paSendRtDTO), Mockito.same(organization), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    Long result = service.uploadRt(paSendRtDTO, organization, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenUploadPaymentReportingThenInvokeClient(){
    // Given
    PaPaymentReportingDTO paPaymentReportingDTO = new PaPaymentReportingDTO();
    Long organizationId = 1L;
    String fileName = "FILENAME";
    String accessToken = "ACCESSTOKEN";
    Long expectedResult = 2L;

    Mockito.when(clientMock.uploadPaymentReporting(Mockito.same(paPaymentReportingDTO), Mockito.same(organizationId), Mockito.same(fileName), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    Long result = service.uploadPaymentReporting(paPaymentReportingDTO, organizationId, fileName, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
