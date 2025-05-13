package it.gov.pagopa.pu.pagopapayments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.jemos.podam.api.PodamFactory;

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
  private ObjectMapper objectMapper;

  @MockitoBean
  private GenerateNoticeService generateNoticeService;

  private static final Long ORG_ID = 1L;
  private static final String IUV = "IUV123";
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

    Mockito.when(generateNoticeService.generateNotice(
      Mockito.eq(IUV),
      Mockito.any(DebtPositionDTO.class),
      Mockito.anyString())
    ).thenReturn(noticeData);

    TestUtils.setFakeAccessTokenInContext();

    // When & Then
    mockMvc.perform(post("/printpaymentnotice/generate")
        .param("iuv", IUV)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(debtPosition)))
      .andExpect(status().isOk())
      .andExpect(content().bytes(expectedResult));

    Mockito.verify(generateNoticeService, Mockito.times(1)).generateNotice(
      Mockito.eq(IUV), Mockito.any(), Mockito.anyString()
    );
  }

  @Test
  void givenValidInputWhenGenerateMassiveThenOk() throws Exception {
    // Given
    NoticeRequestMassiveDTO request = podamFactory.manufacturePojo(NoticeRequestMassiveDTO.class);
    GeneratedNoticeMassiveFolderDTO response = GeneratedNoticeMassiveFolderDTO.builder()
      .folderId("folderId")
        .build();

    Mockito.when(generateNoticeService.generateNoticeMassive(
        Mockito.any(NoticeRequestMassiveDTO.class),
        Mockito.anyString()))
      .thenReturn(response);

    TestUtils.setFakeAccessTokenInContext();

    // When & Then
    mockMvc.perform(post("/printpaymentnotice/generateMassive")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn();

    Mockito.verify(generateNoticeService, Mockito.times(1)).generateNoticeMassive(
      Mockito.any(NoticeRequestMassiveDTO.class), Mockito.anyString()
    );
  }

  @Test
  void givenValidInputWhenGetSignedUrlThenOk() throws Exception {
    // Given
    SignedUrlResultDTO response = SignedUrlResultDTO.builder()
      .signedUrl("url")
        .build();

    Mockito.when(generateNoticeService.getNoticeMassiveZip(
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

    Mockito.verify(generateNoticeService, Mockito.times(1)).getNoticeMassiveZip(
      Mockito.eq(ORG_ID), Mockito.anyString(), Mockito.anyString()
    );
  }

  @Test
  void givenValidInputWhenGetSignedUrlThen204() throws Exception {
    // Given
    Mockito.when(generateNoticeService.getNoticeMassiveZip(
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

    Mockito.verify(generateNoticeService, Mockito.times(1)).getNoticeMassiveZip(
      Mockito.eq(ORG_ID), Mockito.anyString(), Mockito.anyString()
    );
  }
}
