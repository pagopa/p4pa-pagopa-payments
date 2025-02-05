package it.gov.pagopa.pu.pagopapayments.connector;

import it.gov.pagopa.pu.fileshare.controller.ApiClient;
import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.dto.generated.UploadIngestionFlowFileResponseDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaSendRtDTO;
import it.gov.pagopa.pu.pagopapayments.util.RestUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class FileShareClientImpl implements FileShareClient {

  private final IngestionFlowFileApi ingestionFlowFileApi;
  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public FileShareClientImpl(@Value("${rest.fileshare.base-url}") String fileShareBaseUrl,
                             RestTemplateBuilder restTemplateBuilder){
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate)
      .setBasePath(fileShareBaseUrl);
    apiClient.setBearerToken(bearerTokenHolder::get);
    this.ingestionFlowFileApi = new IngestionFlowFileApi(apiClient);
  }

  @PreDestroy
  public void unload(){
    bearerTokenHolder.remove();
  }

  @Override
  public String uploadRt(PaSendRtDTO paSendRtDTO, Organization organization, String accessToken) {
    bearerTokenHolder.set(accessToken);
    UploadIngestionFlowFileResponseDTO response = RestUtil.handleRestException(
      () -> ingestionFlowFileApi.uploadIngestionFlowFile(organization.getOrganizationId(), IngestionFlowFileType.RECEIPT_PAGOPA, FileOrigin.PAGOPA,
        new ByteArrayResource(paSendRtDTO.getReceiptBytes()) ),
      "upload receipt[%s/%s]".formatted(paSendRtDTO.getFiscalCode(), paSendRtDTO.getNoticeNumber()), true
    );
    log.info("Receipt [{}/{}] uploaded with id: {}", paSendRtDTO.getFiscalCode(), paSendRtDTO.getNoticeNumber(), response.getIngestionFlowFileId());
    return response.getIngestionFlowFileId();
  }

  @Override
  public String fetchPaymentReporting(PaPaymentReportingDTO paPaymentReporingDTO, Long organizationId, String accessToken) {
    //todo manage fileName after resolution of P4ADEV-2107

    bearerTokenHolder.set(accessToken);
    UploadIngestionFlowFileResponseDTO response = RestUtil.handleRestException(
      () -> ingestionFlowFileApi.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.PAYMENTS_REPORTING_PAGOPA, FileOrigin.PAGOPA,
        new ByteArrayResource(paPaymentReporingDTO.getPaymentReportingBytes()) ),
      "upload payment reporting[%s/%s]".formatted(paPaymentReporingDTO.getFiscalCode(), paPaymentReporingDTO.getNoticeNumber()), true
    );
    log.info("Payment reporting [{}/{}] uploaded with id: {}", paPaymentReporingDTO.getFiscalCode(), paPaymentReporingDTO.getNoticeNumber(), response.getIngestionFlowFileId());
    return response.getIngestionFlowFileId();
  }

}
