package it.gov.pagopa.pu.pagopapayments.mapper;

import it.gov.pagopa.nodo.pacreateposition.dto.generated.NewDebtPositionRequest;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.pagopapayments.connector.DebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.dto.generated.*;
import it.gov.pagopa.pu.pagopapayments.util.Constants;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AcaDebtPositionMapperTest {
  @Mock
  private DebtPositionClient debtPositionClientMock;

  @InjectMocks
  private AcaDebtPositionMapper acaDebtPositionMapper;

  private DebtPositionDTO debtPosition;
  private static final OffsetDateTime DUE_DATE = OffsetDateTime.now();
  private static final Long TYPE_ORG_ID_EXPIRING = 1L;
  private static final Long TYPE_ORG_ID_NON_EXPIRING = 2L;
  private static final DebtPositionTypeOrg TYPE_ORG_EXPIRING = new DebtPositionTypeOrg()
    .debtPositionTypeOrgId(TYPE_ORG_ID_EXPIRING)
    .flagMandatoryDueDate(true);
  private static final DebtPositionTypeOrg TYPE_ORG_NON_EXPIRING = new DebtPositionTypeOrg()
    .debtPositionTypeOrgId(TYPE_ORG_ID_NON_EXPIRING)
    .flagMandatoryDueDate(false);

  @BeforeEach
  void setUp() {
    PersonDTO debtor = PersonDTO.builder()
      .fiscalCode("CF_DEBTOR")
      .entityType("F")
      .fullName("Debtor name")
      .build();
    debtPosition = DebtPositionDTO.builder()
      .debtPositionId(1L)
      .description("description")
      .debtPositionTypeOrgId(TYPE_ORG_ID_EXPIRING)
      .paymentOptions(List.of(
        PaymentOptionDTO.builder()
          .paymentOptionId(10L)
          .installments(List.of(
            InstallmentDTO.builder()
              .installmentId(100L)
              .status(AcaDebtPositionMapper.STATUS_INSTALLMENT_TO_SYNCH)
              .amountCents(1234L)
              .debtor(debtor)
              .dueDate(DUE_DATE)
              .transfers(List.of(
                TransferDTO.builder().amountCents(1234L).build()
              ))
              .build()
            , InstallmentDTO.builder()
              .installmentId(101L)
              .status(AcaDebtPositionMapper.STATUS_INSTALLMENT_UNPAID)
              .amountCents(3456L)
              .debtor(debtor)
              .dueDate(DUE_DATE)
              .build()
            , InstallmentDTO.builder()
              .installmentId(102L)
              .status(AcaDebtPositionMapper.STATUS_INSTALLMENT_TO_SYNCH)
              .amountCents(5678L)
              .debtor(debtor)
              .dueDate(DUE_DATE)
              .transfers(List.of(
                TransferDTO.builder().amountCents(1000L).build()
                ,TransferDTO.builder().amountCents(4678L).build()
              ))
              .build()
          ))
          .build()
        , PaymentOptionDTO.builder()
          .paymentOptionId(11L)
          .installments(List.of(
            InstallmentDTO.builder()
              .installmentId(104L)
              .status(AcaDebtPositionMapper.STATUS_INSTALLMENT_TO_SYNCH)
              .amountCents(7890L)
              .debtor(debtor)
              .dueDate(DUE_DATE)
              .transfers(List.of(
                TransferDTO.builder().amountCents(7890L).build()
              ))
              .build()
          ))
          .build()
      ))
      .build();
  }

  //region mapToNewDebtPositionRequest

  @Test
  void givenValidDebtPositionExpiringWhenMapToNewDebtPositionRequestThenOk() {
    //given
    Mockito.when(debtPositionClientMock.getDebtPositionTypeOrgById(TYPE_ORG_ID_EXPIRING, TestUtils.getFakeAccessToken())).thenReturn(TYPE_ORG_EXPIRING);
    //when
    List<NewDebtPositionRequest> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(debtPosition, TestUtils.getFakeAccessToken());
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(2, response.size());
    Assertions.assertEquals(1234, response.get(0).getAmount());
    Assertions.assertEquals(DUE_DATE, response.get(0).getExpirationDate());
    Assertions.assertEquals(DUE_DATE, response.get(1).getExpirationDate());
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionTypeOrgById(TYPE_ORG_ID_EXPIRING, TestUtils.getFakeAccessToken());
  }

  @Test
  void givenValidDebtPositionNonExpiringWhenMapToNewDebtPositionRequestThenOk() {
    //given
    debtPosition.setDebtPositionTypeOrgId(TYPE_ORG_ID_NON_EXPIRING);
    Mockito.when(debtPositionClientMock.getDebtPositionTypeOrgById(TYPE_ORG_ID_NON_EXPIRING, TestUtils.getFakeAccessToken())).thenReturn(TYPE_ORG_NON_EXPIRING);
    //when
    List<NewDebtPositionRequest> response = acaDebtPositionMapper.mapToNewDebtPositionRequest(debtPosition, TestUtils.getFakeAccessToken());
    //verify
    Assertions.assertNotNull(response);
    Assertions.assertEquals(2, response.size());
    Assertions.assertEquals(1234, response.get(0).getAmount());
    Assertions.assertEquals(Constants.MAX_EXPIRATION_DATE, response.get(0).getExpirationDate());
    Assertions.assertEquals(Constants.MAX_EXPIRATION_DATE, response.get(1).getExpirationDate());
    Mockito.verify(debtPositionClientMock, Mockito.times(1)).getDebtPositionTypeOrgById(TYPE_ORG_ID_NON_EXPIRING, TestUtils.getFakeAccessToken());
  }

  //endregion

}
