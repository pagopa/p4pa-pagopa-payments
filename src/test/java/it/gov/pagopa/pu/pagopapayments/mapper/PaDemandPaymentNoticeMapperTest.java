package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaDemandPaymentNoticeResponse;
import it.gov.pagopa.pagopa_api.pa.pafornode.StAmountOption;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaDemandPaymentNoticeMapperTest {

  private final PodamFactory podamFactory;

  PaDemandPaymentNoticeMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  @Test
  void givenValidInstallmentDTOWhenDebtPositionDto2PaVerifyPaymentNoticeResThenOk() {
    //given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    PaymentOptionDTO paymentOptionDTO = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    paymentOptionDTO.setInstallments(List.of(installmentDTO));
    DebtPositionDTO debtPositionDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPositionDTO.setPaymentOptions(List.of(paymentOptionDTO));
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);

    //when
    PaDemandPaymentNoticeResponse response =  PaDemandPaymentNoticeMapper.debtPositionDto2PaVerifyPaymentNoticeRes(debtPositionDTO, organization, broker);

    //verify
    assertNotNull(response);
    assertNull(response.getFault());
    assertEquals(organization.getOrgFiscalCode(), response.getFiscalCodePA());
    assertEquals(organization.getOrgName(), response.getCompanyName());
    assertEquals(installmentDTO.getRemittanceInformation(), response.getPaymentDescription());
    assertEquals(StOutcome.OK, response.getOutcome());

    TestUtils.checkNotNullFields(response,"officeName", "fault");
    assertNotNull(response.getPaymentList());
    assertNotNull(response.getPaymentList().getPaymentOptionDescription());
    TestUtils.checkNotNullFields(response.getPaymentList().getPaymentOptionDescription(),"detailDescription");

    assertEquals(StAmountOption.EQ, response.getPaymentList().getPaymentOptionDescription().getOptions());
    assertEquals(ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getAmountCents()), response.getPaymentList().getPaymentOptionDescription().getAmount());
    assertEquals(ConversionUtils.toXMLGregorianCalendar(ConversionUtils.localDate2RomeMaxTime(installmentDTO.getDueDate())), response.getPaymentList().getPaymentOptionDescription().getDueDate());
    assertTrue(response.getPaymentList().getPaymentOptionDescription().isAllCCP());
  }

  @Test
  void givenTransferWithNoPostalIbanWhenDebtPositionDto2PaVerifyPaymentNoticeResThenAllCCPFalse() {
    //given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.getTransfers().getFirst().setPostalIban(null);
    PaymentOptionDTO paymentOptionDTO = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    paymentOptionDTO.setInstallments(List.of(installmentDTO));
    DebtPositionDTO debtPositionDTO = podamFactory.manufacturePojo(DebtPositionDTO.class);
    debtPositionDTO.setPaymentOptions(List.of(paymentOptionDTO));
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);

    //when
    PaDemandPaymentNoticeResponse response =  PaDemandPaymentNoticeMapper.debtPositionDto2PaVerifyPaymentNoticeRes(debtPositionDTO, organization, broker);

    //verify
    assertNotNull(response);
    assertNull(response.getFault());
    assertEquals(organization.getOrgFiscalCode(), response.getFiscalCodePA());
    assertEquals(organization.getOrgName(), response.getCompanyName());
    assertEquals(installmentDTO.getRemittanceInformation(), response.getPaymentDescription());
    assertEquals(StOutcome.OK, response.getOutcome());

    TestUtils.checkNotNullFields(response,"officeName", "fault");
    assertNotNull(response.getPaymentList());
    assertNotNull(response.getPaymentList().getPaymentOptionDescription());
    TestUtils.checkNotNullFields(response.getPaymentList().getPaymentOptionDescription(),"detailDescription");

    assertEquals(StAmountOption.EQ, response.getPaymentList().getPaymentOptionDescription().getOptions());
    assertEquals(ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getAmountCents()), response.getPaymentList().getPaymentOptionDescription().getAmount());
    assertEquals(ConversionUtils.toXMLGregorianCalendar(ConversionUtils.localDate2RomeMaxTime(installmentDTO.getDueDate())), response.getPaymentList().getPaymentOptionDescription().getDueDate());
    assertFalse(response.getPaymentList().getPaymentOptionDescription().isAllCCP());
  }
}
