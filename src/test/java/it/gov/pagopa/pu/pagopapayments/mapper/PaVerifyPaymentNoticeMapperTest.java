package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaVerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaVerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.pa.pafornode.StAmountOption;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaVerifyPaymentNoticeMapperTest {

  private final PodamFactory podamFactory;

  PaVerifyPaymentNoticeMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  //region paVerifyPaymentNoticeReq2PaForNodeRequestDTO

  @Test
  void givenValidPaVerifyPaymentNoticeReqWhenPaVerifyPaymentNoticeReq2RetrievePaymentDTOThenOk() {
    //given
    PaVerifyPaymentNoticeReq paVerifyPaymentNoticeReq = podamFactory.manufacturePojo(PaVerifyPaymentNoticeReq.class);

    //when
    RetrievePaymentDTO response = PaVerifyPaymentNoticeMapper.paVerifyPaymentNoticeReq2RetrievePaymentDTO(paVerifyPaymentNoticeReq);
    //verify
    assertNotNull(response);
    assertEquals(paVerifyPaymentNoticeReq.getIdPA(), response.getIdPA());
    assertEquals(paVerifyPaymentNoticeReq.getIdBrokerPA(), response.getIdBrokerPA());
    assertEquals(paVerifyPaymentNoticeReq.getIdStation(), response.getIdStation());
    assertEquals(paVerifyPaymentNoticeReq.getQrCode().getFiscalCode(), response.getFiscalCode());
    assertEquals(paVerifyPaymentNoticeReq.getQrCode().getNoticeNumber(), response.getNoticeNumber());
    TestUtils.checkNotNullFields(response,"postalTransfer");
  }

  //endregion


  //region installmentDto2PaVerifyPaymentNoticeRes

  @Test
  void givenValidInstallmentDTOWhenInstallmentDto2PaVerifyPaymentNoticeResThenOk() {
    //given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);

    installmentDTO.getDebtor().setEntityType(PersonEntityType.F);
    for(int idx = 0; idx<installmentDTO.getTransfers().size(); idx++){
      installmentDTO.getTransfers().get(idx).setTransferIndex(idx+1);
    }

    //when
    PaVerifyPaymentNoticeRes response =  PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(installmentDTO, organization, broker);

    //verify
    assertNotNull(response);
    assertNull(response.getFault());
    assertEquals(organization.getOrgFiscalCode(), response.getFiscalCodePA());
    assertEquals(organization.getOrgName(), response.getCompanyName());
    assertEquals(installmentDTO.getRemittanceInformation(), response.getPaymentDescription());
    assertEquals(StOutcome.OK, response.getOutcome());

    TestUtils.checkNotNullFields(response,"fault", "officeName");
    assertNotNull(response.getPaymentList());
    assertNotNull(response.getPaymentList().getPaymentOptionDescription());
    TestUtils.checkNotNullFields(response.getPaymentList().getPaymentOptionDescription(),"detailDescription");

    assertEquals(StAmountOption.EQ, response.getPaymentList().getPaymentOptionDescription().getOptions());
    assertEquals(ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getAmountCents()), response.getPaymentList().getPaymentOptionDescription().getAmount());
    assertEquals(ConversionUtils.toXMLGregorianCalendar(ConversionUtils.localDate2RomeMaxTime(installmentDTO.getDueDate())), response.getPaymentList().getPaymentOptionDescription().getDueDate());
    boolean postalPayment = Optional.ofNullable(installmentDTO.getTransfers()).orElse(List.of()).stream()
      .filter(t -> t.getAmountCents() > 0)
      .map(TransferDTO::getPostalIban)
      .noneMatch(StringUtils::isBlank);
    assertEquals(postalPayment, response.getPaymentList().getPaymentOptionDescription().isAllCCP());
  }

  @Test
  void givenDelegateBrokerAndOwnerTransferWhenInstallmentDto2PaVerifyPaymentNoticeResThenOverrideOrganizationData() {
    // given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.getDebtor().setEntityType(PersonEntityType.F);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode("ORG_FISCAL_CODE");
    organization.setOrgName("ORG_NAME");

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(true);

    TransferDTO owner = podamFactory.manufacturePojo(TransferDTO.class);
    owner.setFlagOwner(true);
    owner.setOrgFiscalCode("OWNER_FISCAL_CODE");
    owner.setOrgName("OWNER_NAME");
    owner.setAmountCents(100L);
    owner.setPostalIban("IT00P0123456789012345678901");

    TransferDTO other = podamFactory.manufacturePojo(TransferDTO.class);
    other.setFlagOwner(false);
    other.setAmountCents(200L);
    other.setPostalIban("IT00P1123456789012345678901");

    installmentDTO.setTransfers(List.of(other, owner));

    // when
    PaVerifyPaymentNoticeRes response =
      PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(installmentDTO, organization, broker);

    // then
    assertNotNull(response);
    assertNull(response.getFault());
    assertEquals(StOutcome.OK, response.getOutcome());

    assertEquals("OWNER_FISCAL_CODE", response.getFiscalCodePA());
    assertEquals("OWNER_NAME", response.getCompanyName());

    assertEquals(installmentDTO.getRemittanceInformation(), response.getPaymentDescription());

    TestUtils.checkNotNullFields(response, "fault", "officeName");
    assertNotNull(response.getPaymentList());
    assertNotNull(response.getPaymentList().getPaymentOptionDescription());
    TestUtils.checkNotNullFields(response.getPaymentList().getPaymentOptionDescription(), "detailDescription");

    assertEquals(StAmountOption.EQ, response.getPaymentList().getPaymentOptionDescription().getOptions());
    assertEquals(
      ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getAmountCents()),
      response.getPaymentList().getPaymentOptionDescription().getAmount()
    );
    assertEquals(
      ConversionUtils.toXMLGregorianCalendar(ConversionUtils.localDate2RomeMaxTime(installmentDTO.getDueDate())),
      response.getPaymentList().getPaymentOptionDescription().getDueDate()
    );

    boolean expectedAllCCP = installmentDTO.getTransfers().stream()
      .filter(t -> t.getAmountCents() > 0)
      .map(TransferDTO::getPostalIban)
      .noneMatch(StringUtils::isBlank);

    assertEquals(expectedAllCCP, response.getPaymentList().getPaymentOptionDescription().isAllCCP());
  }

  //endregion

}
