package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.pagopapayments.connector.DebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PersonDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.TransferDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class AcaDebtPositionMapper {

  private final DebtPositionClient debtPositionClient;

  public AcaDebtPositionMapper(DebtPositionClient debtPositionClient) {
    this.debtPositionClient = debtPositionClient;
  }

  public final static OffsetDateTime MAX_DATE = LocalDateTime.of(2099,12,31,23,59,59).atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime();

  public List<NewDebtPositionRequest> mapToNewDebtPositionRequest(DebtPositionDTO debtPosition, Set<String> filterInstallmentStatus, String accessToken) {
    List<NewDebtPositionRequest> response = new ArrayList<>();
    final AtomicReference<DebtPositionTypeOrg> debtPositionTypeOrg = new AtomicReference<>();

    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().forEach(installment -> {
        if(!filterInstallmentStatus.contains(installment.getStatus())){
          //skip installment whose status is not in the filterInstallmentStatus
          return;
        }

        if(installment.getTransfers().size()!=1) {
          //if installment has multiple transfer ("multibeneficiario"), is not supported on ACA due to ACA API limitations: ignore it
          log.info("ACA mapToNewDebtPositionRequest: ignoring installment [{}/{}] beacuse has multiple transfer[{}]",
            debtPosition.getOrganizationId(), installment.getIuv(), installment.getTransfers().size());
          return;
        }
        TransferDTO transfer = installment.getTransfers().getFirst();
        PersonDTO debtor = installment.getDebtor();
        if(debtPositionTypeOrg.get()==null) {
          debtPositionTypeOrg.set(debtPositionClient.getDebtPositionTypeOrgById(debtPosition.getDebtPositionTypeOrgId(), accessToken));
        }
        OffsetDateTime expirationDate = BooleanUtils.isTrue(debtPositionTypeOrg.get().getFlagMandatoryDueDate()) ?
          installment.getDueDate() : MAX_DATE;
        response.add(new NewDebtPositionRequest()
          //.nav() TODO set nav from installment
          .iuv(installment.getIuv())
          .paFiscalCode(transfer.getOrgFiscalCode())
          .iban(transfer.getIban())
          .postalIban(transfer.getPostalIban())
          .entityType(NewDebtPositionRequest.EntityTypeEnum.valueOf(debtor.getEntityType()))
          .entityFiscalCode(debtor.getFiscalCode())
          .entityFullName(debtor.getFullName())
          .description(installment.getRemittanceInformation())
          .amount(installment.getAmountCents().intValue())
          .expirationDate(expirationDate)
          .switchToExpired(debtPositionTypeOrg.get().getFlagMandatoryDueDate())
          .payStandIn(true) //TODO to verify
        );
      })
    );

    return response;
  }
}
