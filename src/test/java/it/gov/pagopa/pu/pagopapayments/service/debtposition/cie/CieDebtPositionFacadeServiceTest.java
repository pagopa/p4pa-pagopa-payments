package it.gov.pagopa.pu.pagopapayments.service.debtposition.cie;

import it.gov.pagopa.pu.cie.dto.generated.DebtPositionCieOriginAllowedEnum;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.cie.CieDebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.DebtPositionService;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.SpontaneousFormService;
import it.gov.pagopa.pu.pagopapayments.service.JAXBTransformService;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import it.gov.spcoop.puntoaccessopsp.pagamentocie.PagamentoCIE;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CieDebtPositionFacadeServiceTest {

  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private JAXBTransformService jaxbTransformServiceMock;
  @Mock
  private CieDebtPositionService cieDebtPositionServiceMock;
  @Mock
  private SpontaneousFormService spontaneousFormServiceMock;
  @InjectMocks
  private CieDebtPositionFacadeService cieDebtPositionFacadeService;

  private final PodamFactory podamFactory;

  private static final String ACCESS_TOKEN = "access-token";

  public CieDebtPositionFacadeServiceTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionServiceMock,
      jaxbTransformServiceMock,
      cieDebtPositionServiceMock,
      spontaneousFormServiceMock
    );
  }

  @Test
  void whenCreateCieDebtPositionThenSuccess() {
    // Given
    byte[] request = "request".getBytes();
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    PagamentoCIE pagamentoCIE = podamFactory.manufacturePojo(PagamentoCIE.class);
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    SpontaneousFormField spontaneousFormField = podamFactory.manufacturePojo(SpontaneousFormField.class);
    spontaneousFormField.setName("sys_type");
    spontaneousForm.getStructure().setFields(List.of(spontaneousFormField));
    String workflowId = "wf-123";

    when(jaxbTransformServiceMock.unmarshalling(request, PagamentoCIE.class))
      .thenReturn(pagamentoCIE);
    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      pagamentoCIE.getCodiceCausale(), ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);
    when(spontaneousFormServiceMock.getSpontaneousForm(debtPositionTypeOrg.getSpontaneousFormId(),ACCESS_TOKEN))
      .thenReturn(spontaneousForm);

    when(cieDebtPositionServiceMock.createDebtPositionCie(argThat(dp->
      DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP.equals(dp.getOrigin())
        && debtPositionTypeOrg.getCode().equals(dp.getDebtPositionTypeOrgCode())
        && pagamentoCIE.getCodiceFiscaleComune().equals(dp.getOrgFiscalCode())
        && spontaneousFormField.getDefaultValue().equals(dp.getRemittanceInformation())
        && PersonEntityType.F.equals(dp.getDebtor().getEntityType())
        && pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario().equals(dp.getDebtor().getFiscalCode())
        && pagamentoCIE.getIntestatario().getDenominazioneIntestatario().equals(dp.getDebtor().getFullName())
    ), eq(organization.getIpaCode())))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));

    // When
    Pair<DebtPositionDTO,String> result = cieDebtPositionFacadeService.createCieDebtPosition(request,organization,ACCESS_TOKEN);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition, result.getLeft());
    assertEquals(workflowId, result.getRight());
  }

  @Test
  void givenNoMatchingFieldNameWhenCreateCieDebtPositionThenFallbackRemittanceInformation() {
    // Given
    byte[] request = "request".getBytes();
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    PagamentoCIE pagamentoCIE = podamFactory.manufacturePojo(PagamentoCIE.class);
    SpontaneousForm spontaneousForm = podamFactory.manufacturePojo(SpontaneousForm.class);
    SpontaneousFormField spontaneousFormField = podamFactory.manufacturePojo(SpontaneousFormField.class);
    spontaneousFormField.setName("wrong_sys_type");
    spontaneousForm.getStructure().setFields(List.of(spontaneousFormField));
    String workflowId = "wf-123";

    when(jaxbTransformServiceMock.unmarshalling(request, PagamentoCIE.class))
      .thenReturn(pagamentoCIE);
    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      pagamentoCIE.getCodiceCausale(), ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);
    when(spontaneousFormServiceMock.getSpontaneousForm(debtPositionTypeOrg.getSpontaneousFormId(),ACCESS_TOKEN))
      .thenReturn(spontaneousForm);

    when(cieDebtPositionServiceMock.createDebtPositionCie(argThat(dp->
      DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP.equals(dp.getOrigin())
        && debtPositionTypeOrg.getCode().equals(dp.getDebtPositionTypeOrgCode())
        && pagamentoCIE.getCodiceFiscaleComune().equals(dp.getOrgFiscalCode())
        && buildFallbackRemittanceInformation(debtPositionTypeOrg).equals(dp.getRemittanceInformation())
        && PersonEntityType.F.equals(dp.getDebtor().getEntityType())
        && pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario().equals(dp.getDebtor().getFiscalCode())
        && pagamentoCIE.getIntestatario().getDenominazioneIntestatario().equals(dp.getDebtor().getFullName())
    ), eq(organization.getIpaCode())))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));

    // When
    Pair<DebtPositionDTO,String> result = cieDebtPositionFacadeService.createCieDebtPosition(request,organization,ACCESS_TOKEN);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition, result.getLeft());
    assertEquals(workflowId, result.getRight());
  }

  @Test
  void givenNoSpontaneousFormWhenCreateCieDebtPositionThenFallbackRemittanceInformation() {
    // Given
    byte[] request = "request".getBytes();
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    PagamentoCIE pagamentoCIE = podamFactory.manufacturePojo(PagamentoCIE.class);
    String workflowId = "wf-123";

    when(jaxbTransformServiceMock.unmarshalling(request, PagamentoCIE.class))
      .thenReturn(pagamentoCIE);
    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      pagamentoCIE.getCodiceCausale(), ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);
    when(spontaneousFormServiceMock.getSpontaneousForm(debtPositionTypeOrg.getSpontaneousFormId(),ACCESS_TOKEN))
      .thenReturn(null);

    when(cieDebtPositionServiceMock.createDebtPositionCie(argThat(dp->
      DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP.equals(dp.getOrigin())
        && debtPositionTypeOrg.getCode().equals(dp.getDebtPositionTypeOrgCode())
        && pagamentoCIE.getCodiceFiscaleComune().equals(dp.getOrgFiscalCode())
        && buildFallbackRemittanceInformation(debtPositionTypeOrg).equals(dp.getRemittanceInformation())
        && PersonEntityType.F.equals(dp.getDebtor().getEntityType())
        && pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario().equals(dp.getDebtor().getFiscalCode())
        && pagamentoCIE.getIntestatario().getDenominazioneIntestatario().equals(dp.getDebtor().getFullName())
    ), eq(organization.getIpaCode())))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));


    // When
    Pair<DebtPositionDTO,String> result = cieDebtPositionFacadeService.createCieDebtPosition(request,organization,ACCESS_TOKEN);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition, result.getLeft());
    assertEquals(workflowId, result.getRight());
  }

  @Test
  void givenNoSpontaneousFormIdWhenCreateCieDebtPositionThenFallbackRemittanceInformation() {
    // Given
    byte[] request = "request".getBytes();
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    DebtPositionDTO createdDebtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setCode(Constants.SPONTANEOUS_PSP_DP_TYPE_ORG_CODE);
    debtPositionTypeOrg.setSpontaneousFormId(null);
    PagamentoCIE pagamentoCIE = podamFactory.manufacturePojo(PagamentoCIE.class);
    String workflowId = "wf-123";

    when(jaxbTransformServiceMock.unmarshalling(request, PagamentoCIE.class))
      .thenReturn(pagamentoCIE);
    when(debtPositionServiceMock.findDebtPositionTypeOrgByOrgIdAndCode(organization.getOrganizationId(),
      pagamentoCIE.getCodiceCausale(), ACCESS_TOKEN))
      .thenReturn(debtPositionTypeOrg);

    when(cieDebtPositionServiceMock.createDebtPositionCie(argThat(dp->
      DebtPositionCieOriginAllowedEnum.SPONTANEOUS_PSP.equals(dp.getOrigin())
        && debtPositionTypeOrg.getCode().equals(dp.getDebtPositionTypeOrgCode())
        && pagamentoCIE.getCodiceFiscaleComune().equals(dp.getOrgFiscalCode())
        && buildFallbackRemittanceInformation(debtPositionTypeOrg).equals(dp.getRemittanceInformation())
        && PersonEntityType.F.equals(dp.getDebtor().getEntityType())
        && pagamentoCIE.getIntestatario().getCodiceFiscaleIntestatario().equals(dp.getDebtor().getFiscalCode())
        && pagamentoCIE.getIntestatario().getDenominazioneIntestatario().equals(dp.getDebtor().getFullName())
    ), eq(organization.getIpaCode())))
      .thenReturn(Pair.of(createdDebtPosition, workflowId));

    // When
    Pair<DebtPositionDTO,String> result = cieDebtPositionFacadeService.createCieDebtPosition(request,organization,ACCESS_TOKEN);

    // Then
    assertNotNull(result);
    assertEquals(createdDebtPosition, result.getLeft());
    assertEquals(workflowId, result.getRight());
  }

  private String buildFallbackRemittanceInformation(DebtPositionTypeOrg debtPositionTypeOrg) {
    return debtPositionTypeOrg.getCode() + " " + debtPositionTypeOrg.getDescription();
  }
}
