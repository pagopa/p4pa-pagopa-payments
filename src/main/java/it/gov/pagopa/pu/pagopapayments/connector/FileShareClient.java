package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReporingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;

public interface FileShareClient {

  String uploadRt(PaSendRtDTO paSendRtDTO, Organization organization, String accessToken);
  String uploadPaymentReporting(PaPaymentReporingDTO paPaymentReporingDTO, Long organizationId, String accessToken);
}
