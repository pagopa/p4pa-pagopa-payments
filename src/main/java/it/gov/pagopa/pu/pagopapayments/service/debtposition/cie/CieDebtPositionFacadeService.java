package it.gov.pagopa.pu.pagopapayments.service.debtposition.cie;

import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieOriginAllowedEnum;
import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.cie.CieDebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.SpontaneousFormService;
import it.gov.pagopa.pu.pagopapayments.enums.PagoPaNodeFaults;
import it.gov.pagopa.pu.pagopapayments.exception.PagoPaNodeFaultException;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.spcoop.puntoaccessopsp.pagamentocie.PagamentoCIE;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CieDebtPositionFacadeService {
  public static final String SPONTANEOUS_FORM_FIELD_NAME_REMITTANCE_INFORMATION = "sys_type";
  private final JAXBTransformService jaxbTransformService;
  private final CieDebtPositionService cieDebtPositionService;
  private final SpontaneousFormService spontaneousFormService;
  private final DebtPositionService debtPositionService;

  public CieDebtPositionFacadeService(JAXBTransformService jaxbTransformService, CieDebtPositionService cieDebtPositionService, SpontaneousFormService spontaneousFormService, DebtPositionService debtPositionService) {
    this.jaxbTransformService = jaxbTransformService;
    this.cieDebtPositionService = cieDebtPositionService;
    this.spontaneousFormService = spontaneousFormService;
    this.debtPositionService = debtPositionService;
  }

  public Pair<DebtPositionDTO, String> createCieDebtPosition(byte[] request, Organization organization, String accessToken) {
    DebtPositionCieRequestDTO debtPositionCieRequestDTO = buildDebtPositionCieRequestDTO(
      organization.getOrganizationId(),
      jaxbTransformService.unmarshalling(request, PagamentoCIE.class),
      accessToken);
    return cieDebtPositionService.createDebtPositionCie(
      debtPositionCieRequestDTO,
      organization.getIpaCode());
  }

  private  DebtPositionCieRequestDTO buildDebtPositionCieRequestDTO(Long organizationId, PagamentoCIE pagamentoCIE, String accessToken) {
    DebtPositionCieRequestDTO debtPositionCieRequestDTO = new DebtPositionCieRequestDTO();
    debtPositionCieRequestDTO.setOrigin(DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP);
    DebtPositionTypeOrg debtPositionTypeOrg = getDebtPositionTypeOrg(organizationId, pagamentoCIE.getCodiceCausale(), accessToken);
    debtPositionCieRequestDTO.setDebtPositionTypeOrgCode(debtPositionTypeOrg.getCode());
    debtPositionCieRequestDTO.setOrgFiscalCode(pagamentoCIE.getCodiceFiscaleComune());
    debtPositionCieRequestDTO.setRemittanceInformation(buildRemittanceInformation(debtPositionTypeOrg,accessToken));
    debtPositionCieRequestDTO.setDebtor(
      PersonDTO.builder()
        .entityType(PersonEntityType.F)
        .fiscalCode(pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario())
        .fullName(pagamentoCIE.getIntestatario().getDenominazioneIntestatario())
        .build());
    return debtPositionCieRequestDTO;
  }

  private String buildRemittanceInformation(DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    SpontaneousForm spontaneousForm = null;
    if (debtPositionTypeOrg.getSpontaneousFormId() != null) {
      spontaneousForm = spontaneousFormService.getSpontaneousForm(debtPositionTypeOrg.getSpontaneousFormId(), accessToken);
    }
    if (spontaneousForm == null) {
      return handleFallbackRemittance(debtPositionTypeOrg);
    }

    return Optional.ofNullable(spontaneousForm.getStructure().getFields())
      .flatMap(fields -> fields.stream()
        .filter(f -> SPONTANEOUS_FORM_FIELD_NAME_REMITTANCE_INFORMATION.equals(f.getName()))
        .findFirst())
      .map(SpontaneousFormField::getDefaultValue)
      .orElseGet(() -> handleFallbackRemittance(debtPositionTypeOrg));
  }

  private String handleFallbackRemittance(DebtPositionTypeOrg debtPositionTypeOrg) {
    log.warn("DebtPositionTypeOrg having debtPositionTypeOrgId {} and spontaneousFormId {} has no default remittance information", debtPositionTypeOrg.getDebtPositionTypeOrgId(), debtPositionTypeOrg.getSpontaneousFormId());
    return debtPositionTypeOrg.getCode() + " " + debtPositionTypeOrg.getDescription();
  }

  private DebtPositionTypeOrg getDebtPositionTypeOrg(Long organizationId, String debtPositionTypeOrgCode, String accessToken) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionService.findDebtPositionTypeOrgByOrgIdAndCode(organizationId, debtPositionTypeOrgCode, accessToken);
    if(debtPositionTypeOrg == null) {
      throw new PagoPaNodeFaultException(PagoPaNodeFaults.PAA_SYSTEM_ERROR, "DebtPositionTypeOrg with code "+debtPositionTypeOrgCode+" not found");
    }
    return debtPositionTypeOrg;
  }
}
