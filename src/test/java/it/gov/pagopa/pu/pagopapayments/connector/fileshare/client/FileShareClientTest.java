package it.gov.pagopa.pu.pagopapayments.connector.fileshare.client;

import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.dto.generated.UploadIngestionFlowFileResponseDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.fileshare.config.FileShareApisHolder;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class FileShareClientTest {

  @Mock
  private FileShareApisHolder apisHolderMock;
  @Mock
  private IngestionFlowFileApi ingestionFlowFileApiMock;

  private FileShareClient client;

  @BeforeEach
  void setUp() {
    client = new FileShareClient(apisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      apisHolderMock,
      ingestionFlowFileApiMock
    );
  }

  @Test
  void whenUploadRtIdThenInvokeApi() {
    //Given
    PaSendRtDTO paSendRtDTO = TestUtils.getPodamFactory().manufacturePojo(PaSendRtDTO.class);
    Organization organization = TestUtils.getPodamFactory().manufacturePojo(Organization.class);
    String accessToken = "ACCESSTOKEN";
    Long expectedResult = 1L;

    Mockito.when(apisHolderMock.getIngestionFlowFileApi(accessToken))
      .thenReturn(ingestionFlowFileApiMock);
    Mockito.when(ingestionFlowFileApiMock.uploadIngestionFlowFile(
        Mockito.same(organization.getOrganizationId()),
        Mockito.eq(IngestionFlowFileType.RECEIPT_PAGOPA),
        Mockito.eq(FileOrigin.PAGOPA),
        Mockito.eq("RT_" + paSendRtDTO.getNoticeNumber() + ".xml"),
        Mockito.argThat(i -> {
          try {
            return Arrays.equals(i.getContentAsByteArray(), paSendRtDTO.getReceiptBytes());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })))
      .thenReturn(UploadIngestionFlowFileResponseDTO.builder()
        .ingestionFlowFileId(expectedResult)
        .build());

    // When
    Long result = client.uploadRt(paSendRtDTO, organization, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenUploadPaymentReportingThenInvokeApi() {
    //Given
    PaPaymentReportingDTO paPaymentReportingDTO = TestUtils.getPodamFactory().manufacturePojo(PaPaymentReportingDTO.class);
    Long organizationId = 1L;
    String fileName = "FILENAME";
    String accessToken = "ACCESSTOKEN";
    Long expectedResult = 2L;

    Mockito.when(apisHolderMock.getIngestionFlowFileApi(accessToken))
      .thenReturn(ingestionFlowFileApiMock);
    Mockito.when(ingestionFlowFileApiMock.uploadIngestionFlowFile(
        Mockito.same(organizationId),
        Mockito.eq(IngestionFlowFileType.PAYMENTS_REPORTING_PAGOPA),
        Mockito.eq(FileOrigin.PAGOPA),
        Mockito.same(fileName),
        Mockito.argThat(i -> {
          try {
            return Arrays.equals(i.getContentAsByteArray(), paPaymentReportingDTO.getPaymentReportingBytes());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })))
      .thenReturn(UploadIngestionFlowFileResponseDTO.builder()
        .ingestionFlowFileId(expectedResult)
        .build());

    // When
    Long result = client.uploadPaymentReporting(paPaymentReportingDTO, organizationId, fileName, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

}
