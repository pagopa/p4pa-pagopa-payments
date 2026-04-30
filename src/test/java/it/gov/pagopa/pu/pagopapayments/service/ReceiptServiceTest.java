package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.service.receipt.ReceiptService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

  @Mock
  private PaForNodeRequestValidatorService paForNodeRequestValidatorServiceMock;
  @Mock
  private FileShareService fileShareServiceMock;
  @Mock
  private AuthnService authnServiceMock;

  @InjectMocks
  private ReceiptService receiptService;

  private final PodamFactory podamFactory;

  private static final String VALID_ACCESS_TOKEN = "accessToken";

  public ReceiptServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  @Test
  void givenValidReceiptWhenProcessReceivedReceiptThenOk(){
    // given
    Long validIngestionFlowId = 1L;
    PaSendRtDTO request = podamFactory.manufacturePojo(PaSendRtDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    Mockito.when(authnServiceMock.getAccessToken()).thenReturn(VALID_ACCESS_TOKEN);
    Mockito.when(authnServiceMock.getAccessToken(Mockito.anyString())).thenReturn(VALID_ACCESS_TOKEN);
    Mockito.when(paForNodeRequestValidatorServiceMock.paSendRtRequestValidate(request, VALID_ACCESS_TOKEN)).thenReturn(organization);
    Mockito.when(fileShareServiceMock.uploadRt(request, organization, VALID_ACCESS_TOKEN)).thenReturn(validIngestionFlowId);

    // when
    Long response = receiptService.processReceivedReceipt(request);

    // then
    Assertions.assertEquals(validIngestionFlowId, response);
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken(Mockito.anyString());
    Mockito.verify(paForNodeRequestValidatorServiceMock, Mockito.times(1)).paSendRtRequestValidate(request, VALID_ACCESS_TOKEN);
    Mockito.verify(fileShareServiceMock, Mockito.times(1)).uploadRt(request, organization, VALID_ACCESS_TOKEN);
  }
}
