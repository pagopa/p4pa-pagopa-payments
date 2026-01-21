package it.gov.pagopa.pu.pagopapayments.service.aca;

import it.gov.pagopa.pu.aca.gpd.v1.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.AcaService;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.mapper.AcaDebtPositionMapper;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class AcaFacadeService {

  private final AcaService acaService;
  private final AcaDebtPositionMapper acaDebtPositionMapper;
  private final BrokerRetrieverService brokerRetrieverService;

  private static final String SERVICE_NAME = "ACA";

  private static final Set<DebtPositionOrigin> ACA_EXCLUDED_ORIGINS = Set.of(
    DebtPositionOrigin.SPONTANEOUS,
    DebtPositionOrigin.SPONTANEOUS_SIL,
    DebtPositionOrigin.SPONTANEOUS_MIXED,
    DebtPositionOrigin.SPONTANEOUS_PSP
  );

  public AcaFacadeService(AcaService acaService, AcaDebtPositionMapper acaDebtPositionMapper, BrokerRetrieverService brokerRetrieverService) {
    this.acaService = acaService;
    this.acaDebtPositionMapper = acaDebtPositionMapper;
    this.brokerRetrieverService = brokerRetrieverService;
  }

  public void sync(String iud, DebtPositionDTO debtPosition, String accessToken) {
    if (ACA_EXCLUDED_ORIGINS.contains(debtPosition.getDebtPositionOrigin())) {
      log.info("Skipping ACA sync for debtPosition [{}] having origin [{}]",
        debtPosition.getDebtPositionId(),
        debtPosition.getDebtPositionOrigin());
      return;
    }
    invokeCreatePositionImpl(iud, debtPosition, accessToken);
  }

  private void invokeCreatePositionImpl(String iud, DebtPositionDTO debtPositionDTO, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO =
      brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(debtPositionDTO.getOrganizationId(), accessToken);

    Organization organization = brokerForNodoPaDTO.getOrganization();

    Pair<Operation, PaymentPositionModel> debtPositionToSendGPD =
      acaDebtPositionMapper.mapToNewPaymentPositionModel(iud, debtPositionDTO, organization);

    PaymentPositionModel newPaymentPositionModel = debtPositionToSendGPD.getRight();
    Operation operation = debtPositionToSendGPD.getLeft();

    log.info("invoking {} with operation [{}] for installment[{}/{}]",
      SERVICE_NAME,
      operation.name(),
      newPaymentPositionModel.getPaymentOption().getFirst().getIuv(),
      iud
    );

    String apiKey = getApiKey(brokerForNodoPaDTO.getBrokerApiKeys());
    String orgFiscalCode = organization.getOrgFiscalCode();

    switch (operation) {
      case DELETE:
        acaService.paDeletePosition(apiKey, orgFiscalCode, newPaymentPositionModel.getIupd(), newPaymentPositionModel);
        break;
      case UPDATE:
        acaService.paUpdatePosition(apiKey, orgFiscalCode, newPaymentPositionModel.getIupd(), newPaymentPositionModel);
        break;
      case CREATE:
        acaService.paCreatePosition(apiKey, orgFiscalCode, newPaymentPositionModel);
        break;
    }
  }

  private String getApiKey(BrokerApiKeys keys) {
    return keys.getAcaKey();
  }
}
