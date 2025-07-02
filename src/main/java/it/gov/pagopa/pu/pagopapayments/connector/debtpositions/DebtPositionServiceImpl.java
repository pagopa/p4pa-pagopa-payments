package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.pagopapayments.config.CacheConfig;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client.DebtPositionClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DebtPositionServiceImpl implements DebtPositionService {

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
  public InstallmentDTO updateInstallmentNotificationFee(Long organizationId,
    String nav,
    Long newFeeCents, String accessToken) {
    return client.updateInstallmentNotificationFee(organizationId, nav, newFeeCents, accessToken);
  }

  @Override
  public List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIuv(Long organizationId, String iuv,
    List<DebtPositionOrigin> debtPositionOriginList, String accessToken) {
    return client.getDebtPositionsByOrganizationIdAndIuv(organizationId, iuv, debtPositionOriginList, accessToken);
  }

}
