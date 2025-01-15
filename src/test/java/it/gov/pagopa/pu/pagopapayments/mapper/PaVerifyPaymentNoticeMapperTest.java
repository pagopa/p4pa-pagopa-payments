package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.PaVerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.pa.pafornode.StAmountOption;
import it.gov.pagopa.pagopa_api.pa.pafornode.StEntityUniqueIdentifierType;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class PaVerifyPaymentNoticeMapperTest {

  private final PodamFactory podamFactory;

  PaVerifyPaymentNoticeMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  //region installmentDto2PaVerifyPaymentNoticeRes

  @Test
  void givenValidInstallmentDTOWhenInstallmentDto2PaVerifyPaymentNoticeResThenOk() {
    //given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    installmentDTO.getDebtor().setEntityType(StEntityUniqueIdentifierType.F.name());
    for(int idx = 0; idx<installmentDTO.getTransfers().size(); idx++){
      installmentDTO.getTransfers().get(idx).setTransferIndex(idx+1L);
    }
    //when
    PaVerifyPaymentNoticeRes response = PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(installmentDTO, organization);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(organization.getOrgFiscalCode(), response.getFiscalCodePA());
    Assertions.assertEquals(organization.getOrgName(), response.getCompanyName());
    Assertions.assertEquals(installmentDTO.getHumanFriendlyRemittanceInformation(), response.getPaymentDescription());
    Assertions.assertEquals(StOutcome.OK, response.getOutcome());
    Assertions.assertNotNull(response.getPaymentList());
    Assertions.assertNotNull(response.getPaymentList().getPaymentOptionDescription());
    Assertions.assertEquals(StAmountOption.EQ, response.getPaymentList().getPaymentOptionDescription().getOptions());
    Assertions.assertEquals(ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getAmountCents()), response.getPaymentList().getPaymentOptionDescription().getAmount());
    Assertions.assertEquals(ConversionUtils.toXMLGregorianCalendar(installmentDTO.getDueDate()), response.getPaymentList().getPaymentOptionDescription().getDueDate());
    boolean postalPayment = installmentDTO.getTransfers().stream().noneMatch(t -> StringUtils.isBlank(t.getPostalIban()));
    Assertions.assertEquals(postalPayment, response.getPaymentList().getPaymentOptionDescription().isAllCCP());
  }

  //endregion

}
