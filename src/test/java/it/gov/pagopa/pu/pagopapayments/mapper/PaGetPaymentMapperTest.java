package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pagopa_api.pa.pafornode.CtTransferPAV2;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaGetPaymentV2Request;
import it.gov.pagopa.pagopa_api.pa.pafornode.PaGetPaymentV2Response;
import it.gov.pagopa.pagopa_api.pa.pafornode.StTransferType;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMapEntry;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    assertNotNull(response);
    assertEquals(paGetPaymentV2Request.getIdPA(), response.getIdPA());
    assertEquals(paGetPaymentV2Request.getIdBrokerPA(), response.getIdBrokerPA());
    assertEquals(paGetPaymentV2Request.getIdStation(), response.getIdStation());
    assertEquals(paGetPaymentV2Request.getQrCode().getFiscalCode(), response.getFiscalCode());
    assertEquals(paGetPaymentV2Request.getQrCode().getNoticeNumber(), response.getNoticeNumber());
    assertEquals(paGetPaymentV2Request.getTransferType().equals(StTransferType.POSTAL), response.getPostalTransfer());
    TestUtils.checkNotNullFields(response);
  }

  //endregion

  //region installmentDto2PaGetPaymentV2Response

  @Test
  void givenValidInstallmentDTOWhenInstallmentDtoPagoPa2PaGetPaymentV2ResponseThenOk() {
    //given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);

    installmentDTO.getDebtor().setEntityType(PersonEntityType.F);
    for (int idx = 0; idx < installmentDTO.getTransfers().size(); idx++) {
      installmentDTO.getTransfers().get(idx).setTransferIndex(idx + 1);
      if (idx == 0) {
        installmentDTO.getTransfers().get(idx).setAmountCents(0L);
      }
    }

    //when
    PaGetPaymentV2Response responseV2 = PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
      installmentDTO, organization, broker, StTransferType.PAGOPA);

    //verify
    assertNotNull(responseV2);
    assertNotNull(responseV2.getData());
    assertEquals(installmentDTO.getIuv(), responseV2.getData().getCreditorReferenceId());
    assertEquals(ConversionUtils.centsAmountToBigDecimalEuroAmount(installmentDTO.getAmountCents()), responseV2.getData().getPaymentAmount());
    assertEquals(ConversionUtils.toXMLGregorianCalendar(ConversionUtils.localDate2RomeMaxTime(installmentDTO.getDueDate())), responseV2.getData().getDueDate());
    assertTrue(responseV2.getData().isLastPayment());
    assertEquals(installmentDTO.getRemittanceInformation(), responseV2.getData().getDescription());
    assertEquals(organization.getOrgName(), responseV2.getData().getCompanyName());
    assertNull(responseV2.getData().getOfficeName());

    TestUtils.checkNotNullFields(responseV2.getData(), "officeName");
    assertNotNull(responseV2.getData().getDebtor());
    assertEquals(installmentDTO.getDebtor().getLocation(), responseV2.getData().getDebtor().getCity());
    assertEquals(installmentDTO.getDebtor().getAddress(), responseV2.getData().getDebtor().getStreetName());
    assertEquals(installmentDTO.getDebtor().getCivic(), responseV2.getData().getDebtor().getCivicNumber());
    assertEquals(installmentDTO.getDebtor().getPostalCode(), responseV2.getData().getDebtor().getPostalCode());
    assertEquals(installmentDTO.getDebtor().getProvince(), responseV2.getData().getDebtor().getStateProvinceRegion());
    assertEquals(installmentDTO.getDebtor().getNation(), responseV2.getData().getDebtor().getCountry());
    assertEquals(installmentDTO.getDebtor().getFullName(), responseV2.getData().getDebtor().getFullName());
    assertEquals(installmentDTO.getDebtor().getEmail(), responseV2.getData().getDebtor().getEMail());
    assertEquals(installmentDTO.getDebtor().getFiscalCode(), responseV2.getData().getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue());
    assertEquals(installmentDTO.getDebtor().getEntityType().getValue(), responseV2.getData().getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierType().value());
    TestUtils.checkNotNullFields(responseV2.getData().getDebtor());

    assertNotNull(responseV2.getData().getMetadata());
    assertNotNull(responseV2.getData().getMetadata().getMapEntries());
    assertTrue(responseV2.getData().getMetadata().getMapEntries().stream()
      .anyMatch(e -> e.getKey().equals("datiSpecificiRiscossione")));
    assertEquals(installmentDTO.getLegacyPaymentMetadata(), responseV2.getData().getMetadata().getMapEntries()
      .stream().filter(e -> e.getKey().equals("datiSpecificiRiscossione"))
      .findFirst().map(CtMapEntry::getValue).orElse(null));

    List<TransferDTO> expectedTransfers = installmentDTO.getTransfers().stream()
      .filter(t -> t.getAmountCents() > 0)
      .toList();

    assertEquals(expectedTransfers.size(), responseV2.getData().getTransferList().getTransfers().size());

    for (int i = 0; i < responseV2.getData().getTransferList().getTransfers().size(); i++) {
      TransferDTO expected = expectedTransfers.get(i);
      CtTransferPAV2 actual = responseV2.getData().getTransferList().getTransfers().get(i);

      assertEquals(expected.getTransferIndex(), actual.getIdTransfer());
      assertEquals(expected.getOrgFiscalCode(), actual.getFiscalCodePA());
      assertEquals(expected.getOrgName(), actual.getCompanyName());
      assertEquals(ConversionUtils.centsAmountToBigDecimalEuroAmount(expected.getAmountCents()), actual.getTransferAmount());
      assertEquals(expected.getCategory(), actual.getTransferCategory());
      assertEquals(expected.getRemittanceInformation(), actual.getRemittanceInformation());
      assertEquals(expected.getIban(), actual.getIBAN());

      if (expected.getStampHashDocument() != null) {
        assertNotNull(actual.getRichiestaMarcaDaBollo());
        assertEquals(expected.getStampType(), actual.getRichiestaMarcaDaBollo().getTipoBollo());
        assertEquals(expected.getStampHashDocument(),
          new String(actual.getRichiestaMarcaDaBollo().getHashDocumento(), StandardCharsets.UTF_8));
        assertEquals(expected.getStampProvincialResidence(), actual.getRichiestaMarcaDaBollo().getProvinciaResidenza());
      } else {
        assertNull(actual.getRichiestaMarcaDaBollo());
      }

      // AGGIUNTO: verifica IBANAPPOGGIO per PAGOPA
      if (StringUtils.isNotBlank(expected.getPostalIban())) {
        assertNotNull(actual.getMetadata());
        assertTrue(actual.getMetadata().getMapEntries().stream()
          .anyMatch(e -> "IBANAPPOGGIO".equals(e.getKey())
            && expected.getPostalIban().equals(e.getValue())));
      } else {
        boolean hasIbanAppoggio = actual.getMetadata() != null &&
          actual.getMetadata().getMapEntries().stream()
            .anyMatch(e -> "IBANAPPOGGIO".equals(e.getKey()));
        assertFalse(hasIbanAppoggio);
      }
    }
  }

  @ParameterizedTest
  @CsvSource({
    "legacyPaymentMetadata, IT60X1111111101000000123456",
    "legacyPaymentMetadata, ''",
    "'   ', IT60X1111111101000000123456",
    ", IT60X1111111101000000123456"
  })
  void givenValidInstallmentDTOPostalWhenInstallmentDto2PaGetPaymentV2ResponseThenOk(
    String legacyPaymentMetadata, String postalIban) {
    //given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setLegacyPaymentMetadata(legacyPaymentMetadata);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);

    installmentDTO.getDebtor().setEntityType(PersonEntityType.F);
    for (int idx = 0; idx < installmentDTO.getTransfers().size(); idx++) {
      installmentDTO.getTransfers().get(idx).setTransferIndex(idx + 1);
      installmentDTO.getTransfers().get(idx).setPostalIban(postalIban);
    }

    //when
    PaGetPaymentV2Response responseV2 = PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
      installmentDTO, organization, broker, StTransferType.POSTAL);

    //verify
    assertNotNull(responseV2);
    assertNotNull(responseV2.getData());
    TestUtils.checkNotNullFields(responseV2.getData(), "officeName", "metadata");

    if (StringUtils.isNotBlank(legacyPaymentMetadata)) {
      assertNotNull(responseV2.getData().getMetadata());
      assertNotNull(responseV2.getData().getMetadata().getMapEntries());
      assertTrue(responseV2.getData().getMetadata().getMapEntries().stream()
        .anyMatch(e -> e.getKey().equals("datiSpecificiRiscossione")));
      assertEquals(legacyPaymentMetadata, responseV2.getData().getMetadata().getMapEntries()
        .stream().filter(e -> e.getKey().equals("datiSpecificiRiscossione"))
        .findFirst().map(CtMapEntry::getValue).orElse(null));
    } else {
      assertNull(responseV2.getData().getMetadata());
    }

    TestUtils.checkNotNullFields(responseV2.getData().getDebtor());

    List<TransferDTO> expectedTransfers = installmentDTO.getTransfers().stream()
      .filter(t -> t.getAmountCents() > 0)
      .toList();

    assertEquals(expectedTransfers.size(), responseV2.getData().getTransferList().getTransfers().size());

    for (int i = 0; i < responseV2.getData().getTransferList().getTransfers().size(); i++) {
      TransferDTO expected = expectedTransfers.get(i);
      CtTransferPAV2 actual = responseV2.getData().getTransferList().getTransfers().get(i);

      String expectedIban = StringUtils.isNotBlank(expected.getPostalIban())
        ? expected.getPostalIban()
        : expected.getIban();
      assertEquals(expectedIban, actual.getIBAN());
      TestUtils.checkNotNullFields(actual, "metadata");
    }
  }

  @Test
  void givenNullTransfersWhenInstallmentDto2PaGetPaymentV2ResponseThenEmptyTransferList() {
    // given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setLegacyPaymentMetadata(null);
    installmentDTO.setTransfers(null);
    installmentDTO.getDebtor().setEntityType(PersonEntityType.F);
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(false);

    // when
    PaGetPaymentV2Response response = PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
      installmentDTO, organization, broker, StTransferType.PAGOPA);

    // then
    assertNotNull(response);
    assertNotNull(response.getData().getTransferList());
    assertTrue(response.getData().getTransferList().getTransfers().isEmpty());
  }

  @Test
  void givenDelegateBrokerAndOwnerTransferWithOrgNameWhenInstallmentDto2PaGetPaymentV2ResponseThenCompanyNameIsOwnerOrgName() {
    // given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setLegacyPaymentMetadata(null);
    installmentDTO.getDebtor().setEntityType(PersonEntityType.F);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgName("ORG_NAME");

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(true);

    TransferDTO owner = podamFactory.manufacturePojo(TransferDTO.class);
    owner.setFlagOwner(true);
    owner.setOrgName("OWNER_ORG_NAME");
    owner.setAmountCents(100L);
    owner.setTransferIndex(1);

    installmentDTO.setTransfers(List.of(owner));

    // when
    PaGetPaymentV2Response response = PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
      installmentDTO, organization, broker, StTransferType.PAGOPA);

    // then
    assertNotNull(response);
    assertNotNull(response.getData());

    TestUtils.checkNotNullFields(response.getData(), "officeName", "metadata");
    TestUtils.checkNotNullFields(response.getData().getDebtor());
    TestUtils.checkNotNullFields(response.getData().getTransferList().getTransfers().getFirst(), "metadata");

    assertEquals("OWNER_ORG_NAME", response.getData().getCompanyName());
  }

  @Test
  void givenDelegateBrokerAndNoOwnerTransferWhenInstallmentDto2PaGetPaymentV2ResponseThenCompanyNameIsOrganizationOrgName() {
    // given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setLegacyPaymentMetadata(null);
    installmentDTO.getDebtor().setEntityType(PersonEntityType.F);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgName("ORG_NAME");

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(true);

    TransferDTO t1 = podamFactory.manufacturePojo(TransferDTO.class);
    t1.setFlagOwner(false);
    t1.setAmountCents(100L);
    t1.setTransferIndex(1);

    installmentDTO.setTransfers(List.of(t1));

    // when
    PaGetPaymentV2Response response = PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
      installmentDTO, organization, broker, StTransferType.PAGOPA);

    // then
    assertNotNull(response);
    assertNotNull(response.getData());

    TestUtils.checkNotNullFields(response.getData(), "officeName", "metadata");
    TestUtils.checkNotNullFields(response.getData().getDebtor());
    TestUtils.checkNotNullFields(response.getData().getTransferList().getTransfers().getFirst(), "metadata");

    assertEquals("ORG_NAME", response.getData().getCompanyName());
  }

  @Test
  void givenDelegateBrokerAndOwnerTransferWithBlankOrgNameWhenInstallmentDto2PaGetPaymentV2ResponseThenCompanyNameIsOrganizationOrgName() {
    // given
    InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
    installmentDTO.setLegacyPaymentMetadata(null);
    installmentDTO.getDebtor().setEntityType(PersonEntityType.F);

    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgName("ORG_NAME");

    Broker broker = podamFactory.manufacturePojo(Broker.class);
    broker.setFlagDelegate(true);

    TransferDTO owner = podamFactory.manufacturePojo(TransferDTO.class);
    owner.setFlagOwner(true);
    owner.setOrgName("   ");
    owner.setAmountCents(100L);
    owner.setTransferIndex(1);

    installmentDTO.setTransfers(List.of(owner));

    // when
    PaGetPaymentV2Response response = PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
      installmentDTO, organization, broker, StTransferType.PAGOPA);

    // then
    assertNotNull(response);
    assertNotNull(response.getData());

    TestUtils.checkNotNullFields(response.getData(), "officeName", "metadata");
    TestUtils.checkNotNullFields(response.getData().getDebtor());
    TestUtils.checkNotNullFields(response.getData().getTransferList().getTransfers().getFirst(), "metadata");

    assertEquals("ORG_NAME", response.getData().getCompanyName());
  }

  //endregion

}
