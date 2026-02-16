package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.orgfornode.dto.generated.*;
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
import java.util.List;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class DebtPositions2PaymentOptionsNodeResponseMapperTest {

  @InjectMocks
  private DebtPositions2PaymentOptionsNodeResponseMapper mapper;

  private final PodamFactory podamFactory;
  private Organization organization;

  DebtPositions2PaymentOptionsNodeResponseMapperTest() {
    podamFactory = TestUtils.getPodamFactory();
    podamFactory.getStrategy().setDefaultNumberOfCollectionElements(3);
    podamFactory.getStrategy().addOrReplaceAttributeStrategy(InstallmentDTO.class, "amountCents", (AttributeStrategy<Long>) (attrType, attrAnnotations) -> 123L);
    podamFactory.getStrategy().addOrReplaceAttributeStrategy(PaymentOptionDTO.class, "totalAmountCents", (AttributeStrategy<Long>) (attrType, attrAnnotations) -> 999L);
  }

  @BeforeEach
  void init() {
    organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode("ORG_FISCAL_CODE");
    organization.setOrgName("ORG_NAME");
  }

  @Test
  void givenNullDpWhenMapToResponseThenHeaderIsFilledAndNoPaymentOptions() {
    PaymentOptionsResponseForNode response = mapper.mapToResponse(null, organization);

    Assertions.assertNotNull(response);
    TestUtils.checkNotNullFields(response, "officeName");

    Assertions.assertEquals("ORG_FISCAL_CODE", response.getOrganizationFiscalCode());
    Assertions.assertEquals("ORG_NAME", response.getCompanyName());
    Assertions.assertNotNull(response.getPaymentOptions());
    Assertions.assertTrue(response.getPaymentOptions().isEmpty());
  }

  @Test
  void givenDpWithMixedPoStatusesWhenMapToResponseThenOnlyUnpaidAndPartiallyPaidReturned() {
    DebtPositionDTO dp = podamFactory.manufacturePojo(DebtPositionDTO.class);

    PaymentOptionDTO poUnpaid = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    poUnpaid.setStatus(PaymentOptionStatus.UNPAID);
    poUnpaid.setDescription("PO_UNPAID");
    poUnpaid.setTotalAmountCents(999L);
    poUnpaid.setInstallments(List.of(newInstallment("NAV1", InstallmentStatus.UNPAID, LocalDate.now().plusDays(1))));

    PaymentOptionDTO poPart = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    poPart.setStatus(PaymentOptionStatus.PARTIALLY_PAID);
    poPart.setDescription("PO_PART");
    poPart.setTotalAmountCents(999L);
    poPart.setInstallments(List.of(newInstallment("NAV2", InstallmentStatus.UNPAID, LocalDate.now().plusDays(2))));

    PaymentOptionDTO poCancelled = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    poCancelled.setStatus(PaymentOptionStatus.CANCELLED);
    poCancelled.setInstallments(List.of(newInstallment("NAV3", InstallmentStatus.UNPAID, LocalDate.now().plusDays(3))));

    dp.setPaymentOptions(List.of(poUnpaid, poPart, poCancelled));

    PaymentOptionsResponseForNode response = mapper.mapToResponse(dp, organization);

    Assertions.assertNotNull(response);
    TestUtils.checkNotNullFields(response, "officeName");

    Assertions.assertEquals(2, response.getPaymentOptions().size());

    PaymentOptionForNode first = response.getPaymentOptions().getFirst();
    Assertions.assertNotNull(first);
    TestUtils.checkNotNullFields(first, "statusReason", "dueDate", "validFrom");

    Assertions.assertTrue(response.getPaymentOptions().stream()
      .allMatch(po -> po.getStatus() == EnumPoForNode.PO_UNPAID || po.getStatus() == EnumPoForNode.PO_PARTIALLY_PAID));
  }

  @Test
  void givenPoWithPaidAndReportedInstallmentsWhenMapToResponseThenTheyAreFilteredOut() {
    DebtPositionDTO dp = podamFactory.manufacturePojo(DebtPositionDTO.class);

    InstallmentDTO paid = newInstallment("NAV", InstallmentStatus.PAID, LocalDate.now().plusDays(10));
    InstallmentDTO reported = newInstallment("NAV", InstallmentStatus.REPORTED, LocalDate.now().plusDays(11));
    InstallmentDTO unpaid = newInstallment("NAV", InstallmentStatus.UNPAID, LocalDate.now().plusDays(1));

    PaymentOptionDTO po = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    po.setStatus(PaymentOptionStatus.UNPAID);
    po.setDescription("PO");
    po.setTotalAmountCents(999L);
    po.setInstallments(List.of(paid, reported, unpaid));

    dp.setPaymentOptions(List.of(po));

    PaymentOptionsResponseForNode response = mapper.mapToResponse(dp, organization);

    TestUtils.checkNotNullFields(response, "officeName");
    Assertions.assertEquals(1, response.getPaymentOptions().size());

    PaymentOptionForNode mappedPo = response.getPaymentOptions().getFirst();
    TestUtils.checkNotNullFields(mappedPo, "statusReason", "dueDate", "validFrom");

    Assertions.assertNotNull(mappedPo.getInstallments());
    Assertions.assertEquals(1, mappedPo.getInstallments().size());

    InstallmentForNode mappedInst = mappedPo.getInstallments().getFirst();
    TestUtils.checkNotNullFields(mappedInst, "statusReason", "dueDate", "validFrom");
    Assertions.assertEquals(EnumInstallmentForNode.POI_UNPAID, mappedInst.getStatus());
  }

  @Test
  void givenInstallmentsWithNullDueDatesWhenMapToResponseThenDueDateIsMaxNonNullEndOfDay() {
    DebtPositionDTO dp = podamFactory.manufacturePojo(DebtPositionDTO.class);

    PaymentOptionDTO po = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    po.setStatus(PaymentOptionStatus.UNPAID);
    po.setTotalAmountCents(1000L);
    po.setDescription("DESC");

    LocalDate d1 = LocalDate.now().plusDays(1);
    LocalDate d2 = LocalDate.now().plusDays(10);

    InstallmentDTO i1 = newInstallment("NAV_1", InstallmentStatus.UNPAID, null);
    InstallmentDTO i2 = newInstallment("NAV_2", InstallmentStatus.UNPAID, d1);
    InstallmentDTO i3 = newInstallment("NAV_3", InstallmentStatus.UNPAID, d2);

    po.setInstallments(List.of(i1, i2, i3));
    dp.setPaymentOptions(List.of(po));

    PaymentOptionsResponseForNode response = mapper.mapToResponse(dp, organization);

    Assertions.assertNotNull(response);
    TestUtils.checkNotNullFields(response, "officeName");

    PaymentOptionForNode mapped = response.getPaymentOptions().getFirst();
    Assertions.assertNotNull(mapped);
    TestUtils.checkNotNullFields(mapped, "statusReason", "validFrom");

    String expectedMaxDueDate = Objects.requireNonNull(ConversionUtils.atEndOfDay(d2)).toString();
    Assertions.assertEquals(expectedMaxDueDate, mapped.getDueDate());
  }

  @Test
  void givenInstallmentCancelledWhenMapToResponseThenMappedToInvalid() {
    DebtPositionDTO dp = podamFactory.manufacturePojo(DebtPositionDTO.class);

    PaymentOptionDTO po = podamFactory.manufacturePojo(PaymentOptionDTO.class);
    po.setStatus(PaymentOptionStatus.UNPAID);
    po.setDescription("DESC");
    po.setTotalAmountCents(1L);

    InstallmentDTO inst = newInstallment("NAV", InstallmentStatus.CANCELLED, LocalDate.now().plusDays(2));
    po.setInstallments(List.of(inst));
    dp.setPaymentOptions(List.of(po));

    PaymentOptionsResponseForNode response = mapper.mapToResponse(dp, organization);

    Assertions.assertNotNull(response);
    TestUtils.checkNotNullFields(response, "officeName");

    PaymentOptionForNode mappedPo = response.getPaymentOptions().getFirst();
    Assertions.assertNotNull(mappedPo);
    TestUtils.checkNotNullFields(mappedPo, "statusReason", "validFrom");

    InstallmentForNode mappedInst = mappedPo.getInstallments().getFirst();
    Assertions.assertNotNull(mappedInst);
    TestUtils.checkNotNullFields(mappedInst, "statusReason", "validFrom");

    Assertions.assertEquals(EnumInstallmentForNode.POI_INVALID, mappedInst.getStatus());
  }

  private InstallmentDTO newInstallment(String nav, InstallmentStatus status, LocalDate dueDate) {
    InstallmentDTO inst = podamFactory.manufacturePojo(InstallmentDTO.class);
    inst.setNav(nav);
    inst.setIuv("IUV_" + nav);
    inst.setRemittanceInformation("REM_" + nav);
    inst.setAmountCents(123L);
    inst.setStatus(status);
    inst.setDueDate(dueDate);
    return inst;
  }
}
