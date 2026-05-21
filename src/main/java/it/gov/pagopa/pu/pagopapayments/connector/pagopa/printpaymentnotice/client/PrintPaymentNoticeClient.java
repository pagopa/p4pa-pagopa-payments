package it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.client;

import it.gov.pagopa.pu.organization.dto.generated.OrganizationApiKeyType;
import it.gov.pagopa.pu.pagopapayments.connector.organization.OrganizationService;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.printpaymentnotice.config.PagopaPrintPaymentNoticeApisHolder;
import it.gov.pagopa.pu.printpaymentnotice.connector.printpaymentnotice.generated.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PrintPaymentNoticeClient {

  private final PagopaPrintPaymentNoticeApisHolder apisHolder;
  private final OrganizationService organizationService;

  public PrintPaymentNoticeClient(PagopaPrintPaymentNoticeApisHolder apisHolder, OrganizationService organizationService) {
    this.apisHolder = apisHolder;
    this.organizationService = organizationService;
  }

  private String getApiKeyFromOrganizationOrBroker(Long organizationId, String accessToken) {
    String orgOrBrokerApiKey = organizationService.getOrganizationApiKey(organizationId, OrganizationApiKeyType.GENERATE_NOTICE, accessToken);
    if(!StringUtils.hasText(orgOrBrokerApiKey)){
      throw new IllegalStateException("Organization " + organizationId + " has not GENERATE_NOTICE apiKey configured!");
    }
    return orgOrBrokerApiKey;
  }

  public byte[] generateNotice(Long organizationId, NoticeGenerationRequestItemDTO noticeGenerationRequestItemDTO, String accessToken) {
    String apiKey = getApiKeyFromOrganizationOrBroker(organizationId, accessToken);
    return apisHolder.getNoticeGenerationRequestApi(apiKey)
      .generateNotice(noticeGenerationRequestItemDTO, null, null);
  }

  public NoticeGenerationMassiveResourceDTO generateNoticeMassive(Long organizationId, String idempotencyKey, NoticeGenerationMassiveRequestDTO noticeMassive, String accessToken) {
    String apiKey = getApiKeyFromOrganizationOrBroker(organizationId, accessToken);
    return apisHolder.getNoticeGenerationRequestApi(apiKey)
      .generateNoticeMassiveRequest(idempotencyKey, noticeMassive, null);
  }

  public GetGenerationRequestStatusResourceDTO getFolderStatus(Long organizationId, String folderId, String accessToken) {
    String apiKey = getApiKeyFromOrganizationOrBroker(organizationId, accessToken);
    return apisHolder.getNoticeGenerationRequestApi(apiKey)
      .getFolderStatus(folderId, null);
  }

  public GetSignedUrlResourceDTO getFolderSignedUrlResource(Long organizationId, String folderId, String accessToken) {
    String apiKey = getApiKeyFromOrganizationOrBroker(organizationId, accessToken);
    return apisHolder.getNoticeGenerationRequestApi(apiKey)
      .getFolderSignedUrlResource(folderId, null);
  }
}
