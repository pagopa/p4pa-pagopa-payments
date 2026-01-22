package it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting;

import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.pagopa.nodo.fdrorganization.dto.generated.*;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.paymentsreporting.client.PaymentsReportingClient;
import it.gov.pagopa.pu.pagopapayments.dto.BrokerForNodoPaDTO;
import it.gov.pagopa.pu.pagopapayments.dto.PaPaymentReportingDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.pagopapayments.mapper.PaymentsReportingMapper;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingServiceImplTest {

  public static final String ORG_FISCAL_CODE = "123456789";
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();
  @Mock
  private PaymentsReportingClient paymentsReportingClient;
  @Mock
  private PaymentsReportingMapper paymentsReportingMapper;

  @InjectMocks
  private PaymentsReportingServiceImpl nodePaymentsReportingService;

  @AfterEach
  void tearDown() {
    Mockito.verifyNoMoreInteractions(
      paymentsReportingClient,
      paymentsReportingMapper
    );
  }

  @Test
  void fetchPaymentReportingIdList() {
    //given
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    OffsetDateTime latestFlowDate = OffsetDateTime.now();

    List<FlowByPSP> flowByPSPList = List.of(new FlowByPSP());
    List<PaymentsReportingIdDTO> expectedResult = List.of(new PaymentsReportingIdDTO());

    Mockito.when(paymentsReportingClient.fetchIdList(brokerForNodoPaDTO, latestFlowDate))
      .thenReturn(flowByPSPList);
    Mockito.when(paymentsReportingMapper.mapToIdDtoList(flowByPSPList))
      .thenReturn(expectedResult);

    //when
    List<PaymentsReportingIdDTO> actualResult = nodePaymentsReportingService.fetchPaymentReportingIdList(brokerForNodoPaDTO, latestFlowDate);

    //then
    Assertions.assertEquals(expectedResult, actualResult);
  }

  @Test
  void fetchPaymentReporting() {
    //given
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    String paymentsReportingId = "paymentsReportingId";
    long revision = 1L;
    String pspId = "pspId";

    SingleFlowResponse singleFlowResponse = new SingleFlowResponse();
    List<Payment> paymentsResponse = List.of(new Payment());
    FlussoRiversamento paymentsReporting = new FlussoRiversamento();
    PaPaymentReportingDTO expectedResult = new PaPaymentReportingDTO();

    Mockito.when(paymentsReportingClient.fetchPaymentReportingFlow(brokerForNodoPaDTO, paymentsReportingId, revision, pspId))
      .thenReturn(singleFlowResponse);
    Mockito.when(paymentsReportingClient.fetchAllPaymentsForPaymentReportingFlow(brokerForNodoPaDTO, paymentsReportingId, revision, pspId, singleFlowResponse))
      .thenReturn(paymentsResponse);
    Mockito.when(paymentsReportingMapper.mapPaymentsReporting(singleFlowResponse, paymentsResponse))
      .thenReturn(paymentsReporting);
    Mockito.when(paymentsReportingMapper.mapPaPaymentsReportingDTO(brokerForNodoPaDTO, paymentsReporting))
      .thenReturn(expectedResult);

    //when
    PaPaymentReportingDTO actualResult = nodePaymentsReportingService.fetchPaymentReporting(brokerForNodoPaDTO, paymentsReportingId, revision, pspId);

    //then
    Assertions.assertEquals(expectedResult, actualResult);
  }

  @Test
  void givenPaymentsReportingClientThrowsBadRequestWhenFetchPaymentReportingIdListThenReturnEmptyList() {
    //given
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(ORG_FISCAL_CODE);
    brokerForNodoPaDTO.setOrganization(organization);
    OffsetDateTime latestFlowDate = OffsetDateTime.now();

    ErrorResponse errorResponse = ErrorResponse.builder()
      .appErrorCode("FDR-2008")
      .errors(
        List.of(
          ErrorMessage.builder()
            .message("Creditor institution with ID [%s] is invalid or unknown.".formatted(ORG_FISCAL_CODE))
            .build()
        )
      ).build();
    HttpClientErrorException badRequest =
      HttpClientErrorException.BadRequest.create(
        HttpStatusCode.valueOf(400),
        "Bad request", null, null, null
      );
    badRequest.setBodyConvertFunction(t -> errorResponse);
    Mockito.when(paymentsReportingClient.fetchIdList(brokerForNodoPaDTO, latestFlowDate))
      .thenThrow(badRequest);

    //when
    List<PaymentsReportingIdDTO> actualResult = nodePaymentsReportingService.fetchPaymentReportingIdList(brokerForNodoPaDTO, latestFlowDate);

    //then
    Assertions.assertEquals(Collections.emptyList(), actualResult);
  }

  @ParameterizedTest
  @MethodSource("provideErrorResponseScenarios")
  void givenPaymentsReportingClientThrowsBadRequestWhenFetchPaymentReportingIdListThenThrowException(ErrorResponse errorResponse) {
    //given
    BrokerForNodoPaDTO brokerForNodoPaDTO = new BrokerForNodoPaDTO();
    Organization organization = podamFactory.manufacturePojo(Organization.class);
    organization.setOrgFiscalCode(ORG_FISCAL_CODE);
    brokerForNodoPaDTO.setOrganization(organization);
    OffsetDateTime latestFlowDate = OffsetDateTime.now();

    HttpClientErrorException badRequest =
      HttpClientErrorException.BadRequest.create(
        HttpStatusCode.valueOf(400),
        "Bad request", null, null, null
      );
    badRequest.setBodyConvertFunction(t -> errorResponse);
    Mockito.when(paymentsReportingClient.fetchIdList(brokerForNodoPaDTO, latestFlowDate))
      .thenThrow(badRequest);

    //when
    Assertions.assertThrows(
      HttpClientErrorException.BadRequest.class,
      () -> nodePaymentsReportingService.fetchPaymentReportingIdList(brokerForNodoPaDTO, latestFlowDate)
    );
  }

  private static  Stream<ErrorResponse> provideErrorResponseScenarios() {
    return Stream.of(
      null,
      ErrorResponse.builder().appErrorCode("COD-0000").build()
    );
  }

}
