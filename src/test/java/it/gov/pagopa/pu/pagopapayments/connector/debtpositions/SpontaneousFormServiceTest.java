package it.gov.pagopa.pu.pagopapayments.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.SpontaneousForm;
import it.gov.pagopa.pu.pagopapayments.connector.debtpositions.client.SpontaneousFormClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpontaneousFormServiceTest {

  @Mock
  private SpontaneousFormClient spontaneousFormClientMock;

  private SpontaneousFormService spontaneousFormService;

  @BeforeEach
  void init(){
    spontaneousFormService = new SpontaneousFormServiceImpl(spontaneousFormClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(spontaneousFormClientMock);
  }

  @Test
  void whenGetSpontaneousFormThenInvokeClient(){
    Long spontaneousFormId = 1L;
    String accessToken = "ACCESSTOKEN";
    SpontaneousForm expectedResult = new SpontaneousForm();

    Mockito.when(spontaneousFormClientMock.getSpontaneousForm(spontaneousFormId, accessToken))
      .thenReturn(expectedResult);

    SpontaneousForm result = spontaneousFormService.getSpontaneousForm(spontaneousFormId, accessToken);

    Assertions.assertSame(expectedResult, result);
  }
}
