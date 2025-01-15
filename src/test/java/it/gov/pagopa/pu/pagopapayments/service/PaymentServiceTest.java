package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pagopa_api.pa.pafornode.*;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtRichiestaMarcaDaBollo;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.DebtPositionClient;
import it.gov.pagopa.pu.pagopapayments.connector.OrganizationClient;
import it.gov.pagopa.pu.pagopapayments.connector.auth.AuthnService;
import it.gov.pagopa.pu.pagopapayments.mapper.PaGetPaymentMapper;
import it.gov.pagopa.pu.pagopapayments.mapper.PaVerifyPaymentNoticeMapper;
import it.gov.pagopa.pu.pagopapayments.service.synchronouspayments.PaymentService;
import it.gov.pagopa.pu.pagopapayments.util.PagoPaNodeFaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock
  private DebtPositionClient debtPositionClientMock;
  @Mock
  private OrganizationClient organizationClientMock;
  @Mock
  private AuthnService authnServiceMock;

  @InjectMocks
  private PaymentService paymentService;

  private static final String VALID_BROKER_FISCAL_CODE = "VALID_BROKER";
  private static final Long VALID_BROKER_ID = 101L;
  private static final String VALID_ORG_FISCAL_CODE = "VALID_PA";
  private static final Long VALID_ORG_ID = 1L;
  private static final String VALID_ID_STATION = "VALID_STATION";
  private static final String VALID_NOTICE_NUMBER = "VALID_NOTICE_NUMBER";
  private static final String VALID_ACCEESS_TOKEN = "VALID_ACCESS_TOKEN";
  private static final Long VALID_INSTALLMENT_ID = 1001L;


  private InstallmentDTO validInstallmentDTO;

  private final Map<String, CtQrCode> ctQrCodeMap = new HashMap<>();
  private CtQrCode getQrCode(String fiscalCode, String noticeNumber) {
    return ctQrCodeMap.computeIfAbsent(fiscalCode + noticeNumber, k -> {
      CtQrCode qrCode = new CtQrCode();
      qrCode.setFiscalCode(fiscalCode);
      qrCode.setNoticeNumber(noticeNumber);
      return qrCode;
    });
  }

  @BeforeEach
  void setup(){
    validInstallmentDTO = new InstallmentDTO()
      .installmentId(VALID_INSTALLMENT_ID)
      .iuv("VALID_IUV")
      .remittanceInformation("VALID_REMITTANCE_INFORMATION")
      .amountCents(1234L)
      .status(PaymentService.PaymentStatus.UNPAID.name())
      .transfers(
        List.of(
          new TransferDTO()
            .amountCents(1234L)
            .orgFiscalCode(VALID_ORG_FISCAL_CODE)
            .category("CATEGORY")
            .iban("VALID_IBAN")
        )
      );

    Mockito.when(authnServiceMock.getAccessToken()).thenReturn(VALID_ACCEESS_TOKEN);
  }

  //region paVerifyPaymentNotice

  @Test
  void givenValidInstallmentWhenPaVerifyPaymentNoticeThenOk() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(validInstallmentDTO) );

    PaVerifyPaymentNoticeRes expectedResponse = new PaVerifyPaymentNoticeRes();

    try (MockedStatic<PaVerifyPaymentNoticeMapper> mapperMock = Mockito.mockStatic(PaVerifyPaymentNoticeMapper.class)) {
      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(
        Mockito.eq(validInstallmentDTO), Mockito.argThat(org -> Objects.equals(org.getOrganizationId(), VALID_ORG_ID)))).thenReturn(expectedResponse);

      // When
      PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

      // Then
      Assertions.assertTrue(new ReflectionEquals(expectedResponse).matches(response));
      Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
      Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN);
      Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN);
      mapperMock.verify(() -> PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(
        Mockito.eq(validInstallmentDTO), Mockito.argThat(org -> Objects.equals(org.getOrganizationId(), VALID_ORG_ID))), Mockito.times(1));
    }
  }

  @Test
  void givenValidInstallmentAndOtherInstallmentWhenPaVerifyPaymentNoticeThenOk() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(validInstallmentDTO, new InstallmentDTO().status(PaymentService.PaymentStatus.CANCELLED.name())) );

    PaVerifyPaymentNoticeRes expectedResponse = new PaVerifyPaymentNoticeRes();

    try (MockedStatic<PaVerifyPaymentNoticeMapper> mapperMock = Mockito.mockStatic(PaVerifyPaymentNoticeMapper.class)) {
      mapperMock.when(() -> PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(
        Mockito.eq(validInstallmentDTO), Mockito.argThat(org -> Objects.equals(org.getOrganizationId(), VALID_ORG_ID)))).thenReturn(expectedResponse);

      // When
      PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

      // Then
      Assertions.assertTrue(new ReflectionEquals(expectedResponse).matches(response));
      Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
      Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN);
      Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN);
      mapperMock.verify(() -> PaVerifyPaymentNoticeMapper.installmentDto2PaVerifyPaymentNoticeRes(
        Mockito.eq(validInstallmentDTO), Mockito.argThat(org -> Objects.equals(org.getOrganizationId(), VALID_ORG_ID))), Mockito.times(1));
    }
  }

  @Test
  void givenNotFoundOrgWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode("INVALID_ORG_FISCAL_CODE", VALID_NOTICE_NUMBER));
    PaVerifyPaymentNoticeRes expectedResponse = new PaVerifyPaymentNoticeRes();

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode("INVALID_ORG_FISCAL_CODE", VALID_ACCEESS_TOKEN)).thenReturn(null);

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenNotActiveOrgWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.DRAFT)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenNotFoundBrokerWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(102L)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(102L, VALID_ACCEESS_TOKEN)).thenReturn(null);

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenInvalidBrokerWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(102L)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(102L, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(102L)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode("INVALID_BROKER_FISCAL_CODE"));

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenInvalidStationWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId("INVALID_STATION")
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenNotFoundInstallmentWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of() );

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenDuplicatedInstallmentWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(validInstallmentDTO, validInstallmentDTO) );

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_DUPLICATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenExpiredInstallmentWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(
        new InstallmentDTO().status(PaymentService.PaymentStatus.CANCELLED.name())
        , new InstallmentDTO().status(PaymentService.PaymentStatus.EXPIRED.name())
        , new InstallmentDTO().status(PaymentService.PaymentStatus.PAID.name())
      ) );

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCADUTO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenPaidInstallmentWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(
        new InstallmentDTO().status(PaymentService.PaymentStatus.CANCELLED.name())
        , new InstallmentDTO().status(PaymentService.PaymentStatus.PAID.name())
      ) );

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenCancelledInstallmentWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(
        new InstallmentDTO().status(PaymentService.PaymentStatus.CANCELLED.name())
        , new InstallmentDTO().status(PaymentService.PaymentStatus.DRAFT.name())
      ) );

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_ANNULLATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenFallbackGenericStatusInstallmentWhenPaVerifyPaymentNoticeThenFault() {
    // Given
    PaVerifyPaymentNoticeReq request = new PaVerifyPaymentNoticeReq();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(
        new InstallmentDTO().status(PaymentService.PaymentStatus.DRAFT.name())
      ) );

    // When
    PaVerifyPaymentNoticeRes response = paymentService.paVerifyPaymentNotice(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), response.getFault().getFaultCode());
  }

  //endregion

  //region paGetPaymentV2

  @Test
  void givenValidInstallmentTransferPagoPAWhenPaGetPaymentV2ThenOk() {
    // Given
    PaGetPaymentV2Request request = new PaGetPaymentV2Request();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setTransferType(StTransferType.PAGOPA);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(validInstallmentDTO) );

    PaGetPaymentV2Response expectedResponse = new PaGetPaymentV2Response();

    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
        Mockito.eq(validInstallmentDTO), Mockito.argThat(org -> Objects.equals(org.getOrganizationId(), VALID_ORG_ID)), Mockito.eq(request.getTransferType()))).thenReturn(expectedResponse);

      // When
      PaGetPaymentV2Response response = paymentService.paGetPaymentV2(request);

      // Then
      Assertions.assertTrue(new ReflectionEquals(expectedResponse).matches(response));
      Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
      Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN);
      Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN);
      mapperMock.verify(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
        Mockito.eq(validInstallmentDTO), Mockito.argThat(org -> Objects.equals(org.getOrganizationId(), VALID_ORG_ID)), Mockito.eq(request.getTransferType())), Mockito.times(1));
    }
  }

  @Test
  void givenNotFoundOrgWhenPaGetPaymentV2ThenFault() {
    // Given
    PaGetPaymentV2Request request = new PaGetPaymentV2Request();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode("INVALID_ORG_FISCAL_CODE", VALID_NOTICE_NUMBER));
    PaVerifyPaymentNoticeRes expectedResponse = new PaVerifyPaymentNoticeRes();

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode("INVALID_ORG_FISCAL_CODE", VALID_ACCEESS_TOKEN)).thenReturn(null);

    // When
    PaGetPaymentV2Response response = paymentService.paGetPaymentV2(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenNotActiveOrgWhenPaGetPaymentV2ThenFault() {
    // Given
    PaGetPaymentV2Request request = new PaGetPaymentV2Request();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.DRAFT)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));

    // When
    PaGetPaymentV2Response response = paymentService.paGetPaymentV2(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenNotFoundBrokerWhenPaGetPaymentV2ThenFault() {
    // Given
    PaGetPaymentV2Request request = new PaGetPaymentV2Request();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(102L)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(102L, VALID_ACCEESS_TOKEN)).thenReturn(null);

    // When
    PaGetPaymentV2Response response = paymentService.paGetPaymentV2(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenInvalidBrokerWhenPaGetPaymentV2ThenFault() {
    // Given
    PaGetPaymentV2Request request = new PaGetPaymentV2Request();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(102L)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(102L, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(102L)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode("INVALID_BROKER_FISCAL_CODE"));

    // When
    PaGetPaymentV2Response response = paymentService.paGetPaymentV2(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_INTERMEDIARIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenInvalidStationWhenPaGetPaymentV2ThenFault() {
    // Given
    PaGetPaymentV2Request request = new PaGetPaymentV2Request();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId("INVALID_STATION")
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));

    // When
    PaGetPaymentV2Response response = paymentService.paGetPaymentV2(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_ID_DOMINIO_ERRATO.code(), response.getFault().getFaultCode());
  }

  @Test
  void givenNotFoundInstallmentWhenPaGetPaymentV2ThenFault() {
    // Given
    PaGetPaymentV2Request request = new PaGetPaymentV2Request();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of() );

    // When
    PaGetPaymentV2Response response = paymentService.paGetPaymentV2(request);

    // Then
    Assertions.assertEquals(StOutcome.KO, response.getOutcome());
    Assertions.assertEquals(PagoPaNodeFaults.PAA_PAGAMENTO_SCONOSCIUTO.code(), response.getFault().getFaultCode());
  }

  //endregion

  //region paGetPayment

  @Test
  void givenValidInstallmentTransferPagoPAWhenPaGetPaymentThenOk() {
    // Given
    PaGetPaymentReq requestV1 = new PaGetPaymentReq();
    PaGetPaymentV2Request request = new PaGetPaymentV2Request();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setTransferType(StTransferType.PAGOPA);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(validInstallmentDTO) );

    PaGetPaymentV2Response expectedResponse = new PaGetPaymentV2Response();
    expectedResponse.setData(new CtPaymentPAV2());
    expectedResponse.getData().setTransferList(new CtTransferListPAV2());
    expectedResponse.getData().getTransferList().getTransfers().add(new CtTransferPAV2());
    PaGetPaymentRes expectedResponseV1 = new PaGetPaymentRes();

    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
        Mockito.eq(validInstallmentDTO), Mockito.argThat(org -> Objects.equals(org.getOrganizationId(), VALID_ORG_ID)), Mockito.eq(request.getTransferType()))).thenReturn(expectedResponse);
      mapperMock.when(() -> PaGetPaymentMapper.paGetPaymentReq2V2(Mockito.eq(requestV1))).thenReturn(request);
      mapperMock.when(() -> PaGetPaymentMapper.paGetPaymentV2Response2V1(Mockito.eq(expectedResponse))).thenReturn(expectedResponseV1);

      // When
      PaGetPaymentRes response = paymentService.paGetPayment(requestV1);

      // Then
      Assertions.assertTrue(new ReflectionEquals(expectedResponseV1).matches(response));
      Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
      Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN);
      Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN);
      mapperMock.verify(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
        Mockito.eq(validInstallmentDTO), Mockito.argThat(org -> Objects.equals(org.getOrganizationId(), VALID_ORG_ID)), Mockito.eq(request.getTransferType())), Mockito.times(1));
      mapperMock.verify(() -> PaGetPaymentMapper.paGetPaymentReq2V2(Mockito.eq(requestV1)), Mockito.times(1));
      mapperMock.verify(() -> PaGetPaymentMapper.paGetPaymentV2Response2V1(Mockito.eq(expectedResponse)), Mockito.times(1));
    }
  }

  @Test
  void givenMarcaBolloInstallmentTransferPagoPAWhenPaGetPaymentThenFault() {
    // Given
    PaGetPaymentReq requestV1 = new PaGetPaymentReq();
    requestV1.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));
    PaGetPaymentV2Request request = new PaGetPaymentV2Request();
    request.setIdBrokerPA(VALID_BROKER_FISCAL_CODE);
    request.setIdPA(VALID_ORG_FISCAL_CODE);
    request.setIdStation(VALID_ID_STATION);
    request.setTransferType(StTransferType.PAGOPA);
    request.setQrCode(getQrCode(VALID_ORG_FISCAL_CODE, VALID_NOTICE_NUMBER));

    Mockito.when(organizationClientMock.getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN)).thenReturn(
      new Organization()
        .status(Organization.StatusEnum.ACTIVE)
        .organizationId(VALID_ORG_ID)
        .brokerId(VALID_BROKER_ID)
        .orgFiscalCode(VALID_ORG_FISCAL_CODE));
    Mockito.when(organizationClientMock.getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN)).thenReturn(
      new Broker()
        .brokerId(VALID_BROKER_ID)
        .stationId(VALID_ID_STATION)
        .brokerFiscalCode(VALID_BROKER_FISCAL_CODE));
    validInstallmentDTO.setTransfers(new ArrayList<>(validInstallmentDTO.getTransfers()));
    validInstallmentDTO.getTransfers().add(new TransferDTO().transferIndex(2L).stampHashDocument("STAMP_HASH"));
    Mockito.when(debtPositionClientMock.getDebtPositionsByOrganizationIdAndNav(VALID_ORG_ID, VALID_NOTICE_NUMBER, VALID_ACCEESS_TOKEN))
      .thenReturn( List.of(validInstallmentDTO) );

    PaGetPaymentV2Response expectedResponse = new PaGetPaymentV2Response();
    expectedResponse.setData(new CtPaymentPAV2());
    expectedResponse.getData().setTransferList(new CtTransferListPAV2());
    expectedResponse.getData().getTransferList().getTransfers().add(new CtTransferPAV2());
    CtTransferPAV2 transfer = new CtTransferPAV2();
    transfer.setRichiestaMarcaDaBollo(new CtRichiestaMarcaDaBollo());
    transfer.getRichiestaMarcaDaBollo().setHashDocumento("STAMP_HASH".getBytes(StandardCharsets.UTF_8));
    expectedResponse.getData().getTransferList().getTransfers().add(transfer);

    try (MockedStatic<PaGetPaymentMapper> mapperMock = Mockito.mockStatic(PaGetPaymentMapper.class)) {
      mapperMock.when(() -> PaGetPaymentMapper.installmentDto2PaGetPaymentV2Response(
        Mockito.eq(validInstallmentDTO), Mockito.argThat(org -> Objects.equals(org.getOrganizationId(), VALID_ORG_ID)), Mockito.eq(request.getTransferType()))).thenReturn(expectedResponse);
      mapperMock.when(() -> PaGetPaymentMapper.paGetPaymentReq2V2(Mockito.eq(requestV1))).thenReturn(request);
      mapperMock.when(() -> PaGetPaymentMapper.paGetPaymentV2Response2V1(Mockito.any())).thenAnswer(arg -> {
        PaGetPaymentV2Response resV2 = arg.getArgument(0);
        PaGetPaymentRes res = new PaGetPaymentRes();
        res.setFault(resV2.getFault());
        res.setOutcome(resV2.getOutcome());
        return res;
      });

      // When
      PaGetPaymentRes response = paymentService.paGetPayment(requestV1);

      // Then
      Assertions.assertEquals(StOutcome.KO, response.getOutcome());
      Assertions.assertEquals(PagoPaNodeFaults.PAA_SEMANTICA.code(), response.getFault().getFaultCode());

      Mockito.verify(authnServiceMock, Mockito.times(1)).getAccessToken();
      Mockito.verify(organizationClientMock, Mockito.times(1)).getOrganizationByFiscalCode(VALID_ORG_FISCAL_CODE, VALID_ACCEESS_TOKEN);
      Mockito.verify(organizationClientMock, Mockito.times(1)).getBrokerById(VALID_BROKER_ID, VALID_ACCEESS_TOKEN);
      mapperMock.verify(() -> PaGetPaymentMapper.paGetPaymentReq2V2(Mockito.eq(requestV1)), Mockito.times(1));
    }
  }

  //endregion
}
