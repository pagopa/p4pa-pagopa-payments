package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;

public interface FileShareClient {

  Long uploadRt(PaSendRtDTO paSendRtDTO, Organization organization, String accessToken);
  Long uploadPaymentReporting(PaPaymentReportingDTO paPaymentReporingDTO, Long organizationId, String fileName, String accessToken);
}
