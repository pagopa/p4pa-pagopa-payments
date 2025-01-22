package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.FileShareClient;
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
  private FileShareClient fileShareClientMock;
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
    String validIngestionFlowId = "validIngestionFlowId";
    PaSendRtDTO request = podamFactory.manufacturePojo(PaSendRtDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    Mockito.when(authnServiceMock.getAccessToken()).thenReturn(VALID_ACCESS_TOKEN);
    Mockito.when(paForNodeRequestValidatorServiceMock.paForNodeRequestValidate(request, VALID_ACCESS_TOKEN)).thenReturn(organization);
    Mockito.when(fileShareClientMock.uploadRt(request, organization, VALID_ACCESS_TOKEN)).thenReturn(validIngestionFlowId);

    // when
    String response = receiptService.processReceivedReceipt(request);

    // then
    Assertions.assertEquals(validIngestionFlowId, response);
    Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
    Mockito.verify(paForNodeRequestValidatorServiceMock, Mockito.times(1)).paForNodeRequestValidate(request, VALID_ACCESS_TOKEN);
    Mockito.verify(fileShareClientMock, Mockito.times(1)).uploadRt(request, organization, VALID_ACCESS_TOKEN);
  }
}
