package it.gov.pagopa.pu.pagopapayments.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.gov.pagopa.pu.pagopapayments.controller.AcaController;
import it.gov.pagopa.pu.pagopapayments.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.service.aca.AcaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest
@Import({AcaController.class})
class ControllerExceptionHandlerTest {

  @MockitoBean
  private AcaService acaServiceMock;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void test() throws Exception{
    //given
    DebtPositionDTO invalidDebtPosition = DebtPositionDTO.builder()
      .debtPositionId(9L)
      .build();
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
    String requestJson = ow.writeValueAsString(invalidDebtPosition);
    String errorMessage = "debt position [%s]".formatted(invalidDebtPosition.getDebtPositionId());
    Mockito.doThrow(new NotFoundException(errorMessage)).when(acaServiceMock).create(invalidDebtPosition);
    //when
    mockMvc.perform(
        MockMvcRequestBuilders.post("/aca/create")
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestJson))
    //verify
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.message")
        .value("resource not found: %s".formatted(errorMessage)))
      .andReturn();

    Mockito.verify(acaServiceMock, Mockito.times(1)).create(invalidDebtPosition);
  }

}
