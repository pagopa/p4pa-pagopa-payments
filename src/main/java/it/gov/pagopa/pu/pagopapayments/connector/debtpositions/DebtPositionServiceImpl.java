package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
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
  public List<InstallmentDTO> getDebtPositionsByOrganizationIdAndNav(Long organizationId, String nav,
                                                                     List<DebtPositionDTO.DebtPositionOriginEnum> debtPositionOriginList,
                                                                     String accessToken){
    return client.getDebtPositionsByOrganizationIdAndNav(organizationId, nav, debtPositionOriginList, accessToken);
  }
}
