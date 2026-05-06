package it.gov.pagopa.pu.pagopapayments.service.sync;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerApiKeys;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.enums.Operation;
import it.gov.pagopa.pu.pagopapayments.service.broker.BrokerRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public abstract class BaseSyncOperationService<M> {

  private final BrokerRetrieverService brokerRetrieverService;

  protected BaseSyncOperationService(BrokerRetrieverService brokerRetrieverService) {
    this.brokerRetrieverService = brokerRetrieverService;
  }

  public abstract void sync(String iud, DebtPositionDTO debtPosition, Boolean forceSpontaneous, String accessToken);

  protected void invokeCreatePositionImpl(String iud, DebtPositionDTO debtPositionDTO, String accessToken) {
    BrokerForNodoPaDTO brokerForNodoPaDTO =
      brokerRetrieverService.getBrokerForNodoPaDTOByOrganizationId(debtPositionDTO.getOrganizationId(), accessToken);

    Organization organization = brokerForNodoPaDTO.getOrganization();
    Broker broker = brokerForNodoPaDTO.getBroker();

    boolean isDelegate = Boolean.TRUE.equals(broker.getFlagDelegate());
    TransferDTO transferOwner = isDelegate ? getTransferOwner(debtPositionDTO) : null;

    String orgName = isDelegate ? transferOwner.getOrgName() : organization.getOrgName();
    String orgFiscalCode = isDelegate ? transferOwner.getOrgFiscalCode() : organization.getOrgFiscalCode();

    Pair<Operation, M> mapped = mapToPaymentPositionModel(iud, debtPositionDTO, orgName);
    M model = mapped.getRight();
    Operation operation = mapped.getLeft();

    log.debug("invoking {} with operation [{}] for installment[{}/{}]",
      getServiceName(), operation.name(), extractIuv(model), iud);

    String apiKey = getApiKey(brokerForNodoPaDTO.getBrokerApiKeys());

    executeOperation(operation, apiKey, orgFiscalCode, model);
  }

  private TransferDTO getTransferOwner(DebtPositionDTO debtPositionDTO) {
    return debtPositionDTO.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .flatMap(i -> i.getTransfers().stream())
      .filter(t -> Boolean.TRUE.equals(t.getFlagOwner()))
      .findFirst()
      .orElse(null);
  }

  protected abstract String getServiceName();
  protected abstract String getApiKey(BrokerApiKeys keys);
  protected abstract Pair<Operation, M> mapToPaymentPositionModel(String iud, DebtPositionDTO dto, String orgName);
  protected abstract String extractIuv(M model);
  protected abstract void executeOperation(Operation operation, String apiKey, String orgFiscalCode, M model);

}
