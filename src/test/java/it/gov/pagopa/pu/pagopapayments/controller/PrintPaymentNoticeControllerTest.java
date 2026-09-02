package it.gov.pagopa.pu.pagopapayments.controller;

import io.micrometer.tracing.Tracer;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.pagopapayments.dto.NoticeDataDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.GeneratedNoticeMassiveFolderDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.NoticeRequestMassiveDTO;
import it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO;
import it.gov.pagopa.pu.pagopapayments.service.printpaymentnotice.GenerateNoticeService;
import it.gov.pagopa.pu.pagopapayments.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import uk.co.jemos.podam.api.PodamFactory;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrintPaymentNoticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class PrintPaymentNoticeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JsonMapper jsonMapper;

  @MockitoBean
  private GenerateNoticeService generateNoticeService;
  @MockitoBean
  private Tracer tracerMock;

  private static final Long ORG_ID = 1L;
  private static final String NAV = "NAV123";
  private final PodamFactory podamFactory;
  private static final String FOLDER_ID = "folder-id";

  PrintPaymentNoticeControllerTest() {
    this.podamFactory = TestUtils.getPodamFactory();
  }

  @Test
  void givenValidInputWhenGenerateNoticeThenReturnFile() throws Exception {
    // Given
    DebtPositionDTO debtPosition = podamFactory.manufacturePojo(DebtPositionDTO.class);
    byte[] expectedResult = "PDF-DATA".getBytes();
    NoticeDataDTO noticeData = NoticeDataDTO.builder()
      .notice(expectedResult)
      .fileName("notice.pdf")
        .build();

    when(generateNoticeService.generateNotice(
      Mockito.eq(NAV),
      Mockito.any(DebtPositionDTO.class),
      Mockito.anyString())
    ).thenReturn(noticeData);

    TestUtils.setFakeAccessTokenInContext();

    // When & Then
    mockMvc.perform(post("/printpaymentnotice/generate")
        .param("nav", NAV)
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(debtPosition)))
      .andExpect(status().isOk())
      .andExpect(content().bytes(expectedResult));

    verify(generateNoticeService).generateNotice(
      Mockito.eq(NAV), Mockito.any(), Mockito.anyString()
    );
  }

  @Test
  void givenValidInputWhenGenerateMassiveThenOk() throws Exception {
    // Given
    NoticeRequestMassiveDTO request = podamFactory.manufacturePojo(NoticeRequestMassiveDTO.class);
    GeneratedNoticeMassiveFolderDTO response = GeneratedNoticeMassiveFolderDTO.builder()
      .folderId("folderId")
        .build();

    when(generateNoticeService.generateNoticeMassive(
        Mockito.any(NoticeRequestMassiveDTO.class),
        Mockito.anyString()))
      .thenReturn(response);

    TestUtils.setFakeAccessTokenInContext();

    // When & Then
    mockMvc.perform(post("/printpaymentnotice/generateMassive")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn();

    verify(generateNoticeService).generateNoticeMassive(
      Mockito.any(NoticeRequestMassiveDTO.class), Mockito.anyString()
    );
  }

  @Test
  void givenValidInputWhenGetSignedUrlThenOk() throws Exception {
    // Given
    SignedUrlResultDTO response = SignedUrlResultDTO.builder()
      .signedUrl("url")
        .build();

    when(generateNoticeService.getNoticeMassiveZip(
        Mockito.eq(ORG_ID),
        Mockito.anyString(),
        Mockito.anyString()))
      .thenReturn(response);

    TestUtils.setFakeAccessTokenInContext();

    // When & Then
    mockMvc.perform(get("/printpaymentnotice/{organizationId}/{folderId}/getSignedUrl", ORG_ID, FOLDER_ID)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andReturn();

    verify(generateNoticeService).getNoticeMassiveZip(
      Mockito.eq(ORG_ID), Mockito.anyString(), Mockito.anyString()
    );
  }

  @Test
  void givenValidInputWhenGetSignedUrlThen204() throws Exception {
    // Given
    when(generateNoticeService.getNoticeMassiveZip(
        Mockito.eq(ORG_ID),
        Mockito.anyString(),
        Mockito.anyString()))
      .thenReturn(null);

    TestUtils.setFakeAccessTokenInContext();

    // When & Then
    mockMvc.perform(get("/printpaymentnotice/{organizationId}/{folderId}/getSignedUrl", ORG_ID, FOLDER_ID)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNoContent())
      .andReturn();

    verify(generateNoticeService).getNoticeMassiveZip(
      Mockito.eq(ORG_ID), Mockito.anyString(), Mockito.anyString()
    );
  }
}
