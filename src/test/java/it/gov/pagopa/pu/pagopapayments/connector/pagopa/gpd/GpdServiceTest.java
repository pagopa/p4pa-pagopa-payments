package it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd;

import it.gov.pagopa.nodo.gpd.dto.generated.PaymentPositionModel;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.gpd.client.GpdClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GpdServiceTest {

  @Mock
  private GpdClient gpdClientMock;

  private GpdService gpdService;

  private static final String API_KEY = "testApiKey";
  private static final String ORGANIZATION_FISCAL_CODE = "testOrganizationFiscalCode";
  private static final PaymentPositionModel PAYMENT_POSITION_MODEL = new PaymentPositionModel();
  private static final String IUPD = "testIupd";

  @BeforeEach
  void init() {
    gpdService = new GpdServiceImpl(gpdClientMock);
  }

  @Test
  void testPaCreatePosition() {
    gpdService.paCreatePosition(API_KEY, ORGANIZATION_FISCAL_CODE, PAYMENT_POSITION_MODEL);
    verify(gpdClientMock, times(1)).createPosition(API_KEY, ORGANIZATION_FISCAL_CODE, PAYMENT_POSITION_MODEL);
  }

  @Test
  void testPaUpdatePosition() {
    gpdService.paUpdatePosition(API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, PAYMENT_POSITION_MODEL);
    verify(gpdClientMock, times(1)).updatePosition(API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, PAYMENT_POSITION_MODEL);
  }

  @Test
  void testPaDeletePosition() {
    gpdService.paDeletePosition(API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, PAYMENT_POSITION_MODEL);
    verify(gpdClientMock, times(1)).deletePosition(API_KEY, ORGANIZATION_FISCAL_CODE, IUPD, PAYMENT_POSITION_MODEL);
  }
}
