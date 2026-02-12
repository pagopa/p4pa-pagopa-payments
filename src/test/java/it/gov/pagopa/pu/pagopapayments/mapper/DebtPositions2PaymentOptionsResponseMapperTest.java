package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.fororgs.dto.generated.*;
import it.gov.pagopa.pu.fororgs.dto.generated.PaymentOption;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.util.ConversionUtils;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.common.AttributeStrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class DebtPositions2PaymentOptionsResponseMapperTest {

  @InjectMocks
  private DebtPositions2PaymentOptionsResponseMapper mapperMock;

  private final PodamFactory podamFactory;

  private Organization organization;

  DebtPositions2PaymentOptionsResponseMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
    podamFactory.getStrategy().setDefaultNumberOfCollectionElements(3);
    podamFactory.getStrategy().addOrReplaceAttributeStrategy(
      InstallmentDTO.class,
      "amountCents",
      (AttributeStrategy<Long>) (attrType, attrAnnotations) -> 123L
    );
    podamFactory.getStrategy().addOrReplaceAttributeStrategy(
      PaymentOptionDTO.class,
      "totalAmountCents",
      (AttributeStrategy<Long>) (attrType, attrAnnotations) -> 999L
    );
  }

  @BeforeEach
  void init() {
    organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode("ORG_FISCAL_CODE");
    organization.setOrgName("ORG_NAME");
  }

  @Test
  void givenNullDebtPositionsWhenMapToResponseThenEmptyResponse() {
    // when
    PaymentOptionsResponse response = mapperMock.mapToResponse(null, organization);

    // verify
    Assertions.assertNotNull(response);
    TestUtils.checkNotNullFields(response, "organizationFiscalCode", "companyName", "officeName", "standin");

    Assertions.assertNull(response.getOrganizationFiscalCode());
    Assertions.assertNull(response.getCompanyName());
    Assertions.assertNotNull(response.getPaymentOptions());
    Assertions.assertTrue(response.getPaymentOptions().isEmpty());
  }

  @Test
  void givenEmptyDebtPositionsWhenMapToResponseThenEmptyResponse() {
    // when
    PaymentOptionsResponse response = mapperMock.mapToResponse(List.of(), organization);

    // verify
    Assertions.assertNotNull(response);
    TestUtils.checkNotNullFields(response, "organizationFiscalCode", "companyName", "officeName", "standin");

    Assertions.assertNull(response.getOrganizationFiscalCode());
    Assertions.assertNull(response.getCompanyName());
    Assertions.assertNotNull(response.getPaymentOptions());
    Assertions.assertTrue(response.getPaymentOptions().isEmpty());
  }

  @Test
  void givenValidDebtPositionsWhenMapToResponseThenOkAndFlattenPaymentOptions() {
    // given
    List<DebtPositionDTO> debtPositions = List.of(
      podamFactory.manufacturePojo(DebtPositionDTO.class),
      podamFactory.manufacturePojo(DebtPositionDTO.class)
    );

    for (DebtPositionDTO dp : debtPositions) {
      dp.getPaymentOptions().forEach(po -> {
        po.setStatus(PaymentOptionStatus.UNPAID);
        po.setDescription("PO_DESC");
        po.setTotalAmountCents(999L);

        if (po.getInstallments().isEmpty()) {
          po.setInstallments(List.of(podamFactory.manufacturePojo(InstallmentDTO.class)));
        }

        LocalDate min = LocalDate.now().plusDays(5);
        LocalDate max = LocalDate.now().plusDays(20);

        for (int i = 0; i < po.getInstallments().size(); i++) {
          InstallmentDTO inst = po.getInstallments().get(i);
          inst.setNav("NAV_" + i);
          inst.setIuv("IUV_" + i);
          inst.setRemittanceInformation("REM_" + i);
          inst.setAmountCents(123L);
          inst.setStatus(InstallmentStatus.UNPAID);
          inst.setDueDate(i == 0 ? min : max);
        }
      });
    }

    int expectedPaymentOptions =
      debtPositions.stream().mapToInt(dp -> dp.getPaymentOptions().size()).sum();

    // when
    PaymentOptionsResponse response = mapperMock.mapToResponse(debtPositions, organization);

    // verify header
    Assertions.assertNotNull(response);
    Assertions.assertEquals("ORG_FISCAL_CODE", response.getOrganizationFiscalCode());
    Assertions.assertEquals("ORG_NAME", response.getCompanyName());
    Assertions.assertNull(response.getOfficeName());
    Assertions.assertNotEquals(Boolean.TRUE, response.getStandin());

    // verify flatten
    Assertions.assertNotNull(response.getPaymentOptions());
    Assertions.assertEquals(expectedPaymentOptions, response.getPaymentOptions().size());
    TestUtils.checkNotNullFields(response, "officeName");

    // verify mapping
    PaymentOption mappedPo = response.getPaymentOptions().getFirst();
    TestUtils.checkNotNullFields(mappedPo, "statusReason", "dueDate");
    Assertions.assertNotNull(mappedPo);

    Assertions.assertEquals("PO_DESC", mappedPo.getDescription());
    Assertions.assertEquals(999L, mappedPo.getAmount());
    Assertions.assertEquals(EnumPo.PO_UNPAID, mappedPo.getStatus());
    Assertions.assertNull(mappedPo.getStatusReason());
    Assertions.assertNotEquals(Boolean.TRUE, mappedPo.getAllCCP());

    Assertions.assertNotNull(mappedPo.getValidFrom());
    Assertions.assertDoesNotThrow(() -> LocalDateTime.parse(mappedPo.getValidFrom(), DateTimeFormatter.ISO_DATE_TIME));

    Assertions.assertNotNull(mappedPo.getInstallments());
    Assertions.assertFalse(mappedPo.getInstallments().isEmpty());
    Assertions.assertEquals(mappedPo.getInstallments().size(), mappedPo.getNumberOfInstallments());

    Installment mappedInstallment = mappedPo.getInstallments().getFirst();
    Assertions.assertNotNull(mappedInstallment);
    TestUtils.checkNotNullFields(mappedInstallment, "statusReason");
    Assertions.assertEquals(EnumInstallment.POI_UNPAID, mappedInstallment.getStatus());
    Assertions.assertNull(mappedInstallment.getStatusReason());
    Assertions.assertNotNull(mappedInstallment.getValidFrom());
    Assertions.assertDoesNotThrow(() -> LocalDateTime.parse(mappedInstallment.getValidFrom(), DateTimeFormatter.ISO_DATE_TIME));
  }

  @Test
  void givenInstallmentsWithSomeNullDueDatesWhenMapToResponseThenDueDateIsMaxNonNullEndOfDay() {
    // given
    DebtPositionDTO dp = podamFactory.manufacturePojo(DebtPositionDTO.class);
    PaymentOptionDTO po = dp.getPaymentOptions().getFirst();

    po.setStatus(PaymentOptionStatus.PAID);
    po.setTotalAmountCents(1000L);
    po.setDescription("DESC");

    LocalDate d1 = LocalDate.now().plusDays(1);
    LocalDate d2 = LocalDate.now().plusDays(10);

    InstallmentDTO i1 = podamFactory.manufacturePojo(InstallmentDTO.class);
    i1.setStatus(InstallmentStatus.REPORTED);
    i1.setDueDate(null);
    i1.setNav("NAV_1");
    i1.setIuv("IUV_1");
    i1.setRemittanceInformation("REM_1");
    i1.setAmountCents(10L);

    InstallmentDTO i2 = podamFactory.manufacturePojo(InstallmentDTO.class);
    i2.setStatus(InstallmentStatus.PAID);
    i2.setDueDate(d1);
    i2.setNav("NAV_2");
    i2.setIuv("IUV_2");
    i2.setRemittanceInformation("REM_2");
    i2.setAmountCents(20L);

    InstallmentDTO i3 = podamFactory.manufacturePojo(InstallmentDTO.class);
    i3.setStatus(InstallmentStatus.EXPIRED);
    i3.setDueDate(d2); // max
    i3.setNav("NAV_3");
    i3.setIuv("IUV_3");
    i3.setRemittanceInformation("REM_3");
    i3.setAmountCents(30L);

    po.setInstallments(List.of(i1, i2, i3));

    // when
    PaymentOptionsResponse response = mapperMock.mapToResponse(List.of(dp), organization);

    // verify
    PaymentOption mapped = response.getPaymentOptions().getFirst();
    TestUtils.checkNotNullFields(mapped, "statusReason");
    Assertions.assertEquals(EnumPo.PO_PAID, mapped.getStatus());

    String expectedMaxDueDate = Objects.requireNonNull(ConversionUtils.atEndOfDay(d2)).toString();
    Assertions.assertEquals(expectedMaxDueDate, mapped.getDueDate());

    Installment mappedI1 = mapped.getInstallments().get(0);
    Assertions.assertNull(mappedI1.getDueDate());
    TestUtils.checkNotNullFields(mappedI1, "dueDate", "statusReason");
    Assertions.assertEquals(EnumInstallment.POI_PAID, mappedI1.getStatus());

    Installment mappedI3 = mapped.getInstallments().get(2);
    TestUtils.checkNotNullFields(mappedI3, "statusReason");
    Assertions.assertEquals(
      Objects.requireNonNull(ConversionUtils.atEndOfDay(d2)).toString(),
      mappedI3.getDueDate()
    );
    Assertions.assertEquals(EnumInstallment.POI_EXPIRED_NOT_PAYABLE, mappedI3.getStatus());
  }

  @Test
  void givenStatusesInvalidCancelledToSyncUnpayableDraftWhenMapToResponseThenMappedToInvalid() {
    // given
    DebtPositionDTO dp = podamFactory.manufacturePojo(DebtPositionDTO.class);
    PaymentOptionDTO po = dp.getPaymentOptions().getFirst();
    po.setTotalAmountCents(1L);
    po.setDescription("DESC");

    po.setStatus(PaymentOptionStatus.INVALID);

    InstallmentDTO inst = podamFactory.manufacturePojo(InstallmentDTO.class);
    inst.setStatus(InstallmentStatus.CANCELLED);
    inst.setDueDate(LocalDate.now().plusDays(2));
    inst.setNav("NAV");
    inst.setIuv("IUV");
    inst.setRemittanceInformation("REM");
    inst.setAmountCents(1L);

    po.setInstallments(List.of(inst));

    // when
    PaymentOptionsResponse response = mapperMock.mapToResponse(List.of(dp), organization);

    // verify
    PaymentOption mappedPo = response.getPaymentOptions().getFirst();
    TestUtils.checkNotNullFields(mappedPo, "statusReason", "dueDate");
    Assertions.assertEquals(EnumPo.PO_INVALID, mappedPo.getStatus());

    Installment mappedInst = mappedPo.getInstallments().getFirst();
    TestUtils.checkNotNullFields(mappedInst, "statusReason");
    Assertions.assertEquals(EnumInstallment.POI_INVALID, mappedInst.getStatus());
  }
}
