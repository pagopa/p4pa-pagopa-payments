package it.gov.pagopa.pu.pagopapayments.service.receipt;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.FileShareClient;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.service.PaForNodeRequestValidatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReceiptService {

  private final PaForNodeRequestValidatorService paForNodeRequestValidatorService;
  private final FileShareClient fileShareClient;
  private final AuthnService authnService;

  public ReceiptService(PaForNodeRequestValidatorService paForNodeRequestValidatorService, FileShareClient fileShareClient, AuthnService authnService) {
    this.paForNodeRequestValidatorService = paForNodeRequestValidatorService;
    this.fileShareClient = fileShareClient;
    this.authnService = authnService;
  }

  public String processReceivedReceipt(PaSendRtDTO request) {
    String accessToken = authnService.getAccessToken();
    Organization organization = paForNodeRequestValidatorService.paForNodeRequestValidate(request, accessToken);
    return fileShareClient.uploadRt(request, organization, accessToken);
  }
}
