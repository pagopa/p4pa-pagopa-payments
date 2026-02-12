package it.gov.pagopa.pu.pagopapayments.service.receipt;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.connector.fileshare.FileShareService;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.service.PaForNodeRequestValidatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReceiptService {

  private final PaForNodeRequestValidatorService paForNodeRequestValidatorService;
  private final FileShareService fileShareService;
  private final AuthnService authnService;

  public ReceiptService(PaForNodeRequestValidatorService paForNodeRequestValidatorService, FileShareService fileShareService, AuthnService authnService) {
    this.paForNodeRequestValidatorService = paForNodeRequestValidatorService;
    this.fileShareService = fileShareService;
    this.authnService = authnService;
  }

  public Long processReceivedReceipt(PaSendRtDTO request) {
    String accessToken = authnService.getAccessToken();
    Organization organization = paForNodeRequestValidatorService.paSendRtRequestValidate(request, accessToken);
    //for file share we need an organization-specific access token
    String accessTokenOrg = authnService.getAccessToken(organization.getIpaCode());
    return fileShareService.uploadRt(request, organization, accessTokenOrg);
  }
}
