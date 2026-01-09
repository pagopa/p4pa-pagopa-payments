package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.config.CacheConfig;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client.DebtPositionClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DebtPositionServiceImpl implements DebtPositionService {

  public static final String HEADER_X_WORKFLOW_ID = "x-workflow-id";

  private final DebtPositionClient client;

  public DebtPositionServiceImpl(DebtPositionClient client) {
    this.client = client;
  }

  @Cacheable(cacheNames = CacheConfig.Fields.debtPositionTypeOrg, key = "#debtPositionTypeOrgId", unless="#result == null")
  public DebtPositionTypeOrg getDebtPositionTypeOrgById(Long debtPositionTypeOrgId, String accessToken) {
    return client.getDebtPositionTypeOrgById(debtPositionTypeOrgId, accessToken);
  }

  @Override
  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav,
                                                                     List<DebtPositionOrigin> debtPositionOriginList,
                                                                     String accessToken){
    return client.getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList, accessToken);
  }

  @Override
  public InstallmentDTO updateInstallmentNotificationFee(ActualizeAmountRequestDTO request, String accessToken) {
    return client.updateInstallmentNotificationFee(request, accessToken);
  }

  @Override
  public DebtPositionTypeOrg findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(
    Long organizationId,
    String nav, List<DebtPositionOrigin> debtPositionOriginList,
    String accessToken) {
    return client.findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(organizationId, nav, debtPositionOriginList, accessToken);
  }

  @Override
  public Pair<DebtPositionDTO, String> createDebtPosition(DebtPositionDTO debtPositionDTO, String accessToken) {
    ResponseEntity<DebtPositionDTO> responseEntity = client.createDebtPosition(debtPositionDTO, accessToken);
    return Pair.of(responseEntity.getBody(), responseEntity.getHeaders().getFirst(HEADER_X_WORKFLOW_ID));
  }


}
