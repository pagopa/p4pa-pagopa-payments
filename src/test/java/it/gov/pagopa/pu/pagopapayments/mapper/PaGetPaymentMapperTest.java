package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMapEntry;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.dto.RetrievePaymentDTO;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.nio.charset.StandardCharsets;

@ExtendWith(MockitoExtension.class)
class PaGetPaymentMapperTest {

  private final PodamFactory podamFactory;

  PaGetPaymentMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
  }

  //region paPaGetPaymentV2Request2PaGetPaymentRequestDTO

  @Test
  void givenValidPaGetPaymentV2RequestWhenPaPaGetPaymentV2Request2RetrievePaymentDTOThenOk() {
    //given
    PaGetPaymentV2Request paGetPaymentV2Request = podamFactory.manufacturePojo(PaGetPaymentV2Request.class);

    //when
    RetrievePaymentDTO response = PaGetPaymentMapper.paPaGetPaymentV2Request2RetrievePaymentDTO(paGetPaymentV2Request);
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(paGetPaymentV2Request.getIdPA(), response.getIdPA());
    Assertions.assertEquals(paGetPaymentV2Request.getIdBrokerPA(), response.getIdBrokerPA());
    Assertions.assertEquals(paGetPaymentV2Request.getIdStation(), response.getIdStation());
    Assertions.assertEquals(paGetPaymentV2Request.getQrCode().getFiscalCode(), response.getFiscalCode());
    Assertions.assertEquals(paGetPaymentV2Request.getQrCode().getNoticeNumber(), response.getNoticeNumber());
    Assertions.assertEquals(paGetPaymentV2Request.getTransferType().equals(StTransferType.POSTAL), response.getPostalTransfer());
    TestUtils.checkNotNullFields(response);
  }

  //endregion

  //region installmentDto2PaGetPaymentV2Response

  @Test
  void givenValidInstallmentDTOWhenInstallmentDtoPagoPa2PaGetPaymentV2ResponseThenOk() {
    //given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    installmentDTO.getDebtor().setEntityType(StEntityUniqueIdentifierType.F.name());
    for(int idx = 0; idx<installmentDTO.getTransfers().size(); idx++){
      installmentDTO.getTransfers().get(idx).setTransferIndex(idx+1L);
    }
    //when
    PaGetPaymentV2Response responseV2 = PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(installmentDTO, organization, StTransferType.PAGOPA);
    //verify
    Assertions.assertNotNull(responseV2);
    Assertions.assertNotNull(responseV2.getData());
    Assertions.assertEquals(installmentDTO.getIuv(), responseV2.getData().getCreditorReferenceId());
    Assertions.assertEquals(ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getAmountCents()), responseV2.getData().getPaymentAmount());
    Assertions.assertEquals(ConversionUtils.toXMLGregorianCalendar(installmentDTO.getDueDate()), responseV2.getData().getDueDate());
    Assertions.assertTrue(responseV2.getData().isLastPayment());
    Assertions.assertEquals(installmentDTO.getHumanFriendlyRemittanceInformation(), responseV2.getData().getDescription());
    Assertions.assertEquals(organization.getOrgName(), responseV2.getData().getCompanyName());
    Assertions.assertNull(responseV2.getData().getOfficeName());
    TestUtils.checkNotNullFields(responseV2.getData(),"officeName");
    Assertions.assertNotNull(responseV2.getData().getDebtor());
    Assertions.assertEquals(installmentDTO.getDebtor().getLocation(), responseV2.getData().getDebtor().getCity());
    Assertions.assertEquals(installmentDTO.getDebtor().getAddress(), responseV2.getData().getDebtor().getStreetName());
    Assertions.assertEquals(installmentDTO.getDebtor().getCivic(), responseV2.getData().getDebtor().getCivicNumber());
    Assertions.assertEquals(installmentDTO.getDebtor().getPostalCode(), responseV2.getData().getDebtor().getPostalCode());
    Assertions.assertEquals(installmentDTO.getDebtor().getProvince(), responseV2.getData().getDebtor().getStateProvinceRegion());
    Assertions.assertEquals(installmentDTO.getDebtor().getNation(), responseV2.getData().getDebtor().getCountry());
    Assertions.assertEquals(installmentDTO.getDebtor().getFullName(), responseV2.getData().getDebtor().getFullName());
    Assertions.assertEquals(installmentDTO.getDebtor().getEmail(), responseV2.getData().getDebtor().getEMail());
    Assertions.assertEquals(installmentDTO.getDebtor().getFiscalCode(), responseV2.getData().getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue());
    Assertions.assertEquals(installmentDTO.getDebtor().getEntityType(), responseV2.getData().getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierType().value());
    TestUtils.checkNotNullFields(responseV2.getData().getDebtor());
    Assertions.assertNotNull(responseV2.getData().getMetadata());
    Assertions.assertNotNull(responseV2.getData().getMetadata().getMapEntries());
    Assertions.assertTrue(responseV2.getData().getMetadata().getMapEntries().stream().anyMatch(e -> e.getKey().equals("datiSpecificiRiscossione")));
    Assertions.assertEquals(installmentDTO.getLegacyPaymentMetadata(), responseV2.getData().getMetadata().getMapEntries()
      .stream().filter(e -> e.getKey().equals("datiSpecificiRiscossione")).findFirst().map(CtMapEntry::getValue).orElse(null));
    Assertions.assertEquals(installmentDTO.getTransfers().size(), responseV2.getData().getTransferList().getTransfers().size());
    for (int i = 0; i < responseV2.getData().getTransferList().getTransfers().size(); i++) {
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getTransferIndex(), responseV2.getData().getTransferList().getTransfers().get(i).getIdTransfer());
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getOrgFiscalCode(), responseV2.getData().getTransferList().getTransfers().get(i).getFiscalCodePA());
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getOrgName(), responseV2.getData().getTransferList().getTransfers().get(i).getCompanyName());
      Assertions.assertEquals(ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getTransfers().get(i).getAmountCents()), responseV2.getData().getTransferList().getTransfers().get(i).getTransferAmount());
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getCategory(), responseV2.getData().getTransferList().getTransfers().get(i).getTransferCategory());
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getRemittanceInformation(), responseV2.getData().getTransferList().getTransfers().get(i).getRemittanceInformation());
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getIban(), responseV2.getData().getTransferList().getTransfers().get(i).getIBAN());
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getStampType(), responseV2.getData().getTransferList().getTransfers().get(i).getRichiestaMarcaDaBollo().getTipoBollo());
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getStampHashDocument(),
        new String(responseV2.getData().getTransferList().getTransfers().get(i).getRichiestaMarcaDaBollo().getHashDocumento(), StandardCharsets.UTF_8));
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getStampProvincialResidence(), responseV2.getData().getTransferList().getTransfers().get(i).getRichiestaMarcaDaBollo().getProvinciaResidenza());
      TestUtils.checkNotNullFields(responseV2.getData().getTransferList().getTransfers().get(i), "metadata");
    }
  }

  @Test
  void givenValidInstallmentDTOPostalWhenInstallmentDto2PaGetPaymentV2ResponseThenOk() {
    //given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);

    installmentDTO.getDebtor().setEntityType(StEntityUniqueIdentifierType.F.name());
    for(int idx = 0; idx<installmentDTO.getTransfers().size(); idx++){
      installmentDTO.getTransfers().get(idx).setTransferIndex(idx+1L);
    }
    //when
    PaGetPaymentV2Response responseV2 = PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(installmentDTO, organization, StTransferType.POSTAL);
    //verify
    Assertions.assertNotNull(responseV2);
    Assertions.assertNotNull(responseV2.getData());
    TestUtils.checkNotNullFields(responseV2.getData(),"officeName");
    TestUtils.checkNotNullFields(responseV2.getData().getDebtor());
    Assertions.assertEquals(installmentDTO.getTransfers().size(), responseV2.getData().getTransferList().getTransfers().size());
    for (int i = 0; i < responseV2.getData().getTransferList().getTransfers().size(); i++) {
      Assertions.assertEquals(installmentDTO.getTransfers().get(i).getPostalIban(), responseV2.getData().getTransferList().getTransfers().get(i).getIBAN());
      TestUtils.checkNotNullFields(responseV2.getData().getTransferList().getTransfers().get(i), "metadata");
    }
  }

  //endregion

}
