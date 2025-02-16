package it.gov.pagopa.pu.pagopapayments.connector.fileshare;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.fileshare.client.FileShareClient;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import org.springframework.stereotype.Service;

@Service
public class FileShareServiceImpl implements FileShareService {

  private final FileShareClient client;

  public FileShareServiceImpl(FileShareClient client) {
    this.client = client;
  }

  @Override
  public Long uploadRt(PaSendRtDTO paSendRtDTO, Organization organization, String accessToken) {
    return client.uploadRt(paSendRtDTO, organization, accessToken);
  }

  @Override
  public Long uploadPaymentReporting(PaPaymentReportingDTO paPaymentReporingDTO, Long organizationId, String fileName, String accessToken) {
    return client.uploadPaymentReporting(paPaymentReporingDTO, organizationId, fileName, accessToken);
  }
}
