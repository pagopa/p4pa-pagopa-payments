package it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca;

import it.gov.pagopa.pu.aca.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.aca.client.AcaClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AcaServiceTest {

  @Mock
  private AcaClient acaClientMock;

  private AcaService acaService;

  private static final String API_KEY = "testApiKey";
  private static final String ORGANIZATION_FISCAL_CODE = "testOrganizationFiscalCode";
  private static final String IUPD = "testIupd";
  private static final PaymentPositionModel PAYMENT_POSITION_MODEL = new PaymentPositionModel();

  @BeforeEach
  void init() {
    acaService = new AcaServiceImpl(acaClientMock);
  }

  @Test
  void testPaCreatePosition() {
    acaService.paCreatePosition(API_KEY, ORGANIZATION_FISCAL_CODE, PAYMENT_POSITION_MODEL);
    verify(acaClientMock, times(1)).createPosition(API_KEY, ORGANIZATION_FISCAL_CODE, PAYMENT_POSITION_MODEL);
  }

  @Test
  void testPaUpdatePosition() {
    acaService.paUpdatePosition(API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, PAYMENT_POSITION_MODEL);
    verify(acaClientMock, times(1)).updatePosition(API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, PAYMENT_POSITION_MODEL);
  }

  @Test
  void testPaDeletePosition() {
    acaService.paDeletePosition(API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, PAYMENT_POSITION_MODEL);
    verify(acaClientMock, times(1)).deletePosition(API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, PAYMENT_POSITION_MODEL);
  }
}
