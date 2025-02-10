package it.gov.pagopa.pu.pagopapayments.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoPaApiClientTest {

  @Mock
  private RestTemplate restTemplateMock;

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  @Mock
  private ObjectMapper objectMapperMock;

  @InjectMocks
  private PagoPaApiClient pagoPaApiClient;

  private final String baseUrl = "http://example.com";

  @BeforeEach
  void setUp() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    pagoPaApiClient = new PagoPaApiClient(baseUrl, restTemplateBuilderMock, objectMapperMock);
  }

  @Test
  void getTaxonomiesThrowsApplicationExceptionOnJsonProcessingException() throws JsonProcessingException {
    // Given
    when(restTemplateMock.getForObject(anyString(), eq(String.class))).thenReturn("invalid json");
    doThrow(new JsonProcessingException("Error") {}).when(objectMapperMock).readValue(anyString(), any(TypeReference.class));

    // When & Then
    assertThrows(ApplicationException.class, () -> pagoPaApiClient.getTaxonomies());
    verify(restTemplateMock, times(1)).getForObject(baseUrl, String.class);
  }

  @Test
  void getTaxonomiesReturnsListOfTaxonomies() throws JsonProcessingException {
    // Given
    String json = "[{\"id\":1}]";
    PaTaxonomyDTO paTaxonomyDTO = PaTaxonomyDTO.builder().id(1L).build();
    List<PaTaxonomyDTO> expectedResponse = List.of(paTaxonomyDTO);
    when(restTemplateMock.getForObject(anyString(), eq(String.class))).thenReturn(json);
    when(objectMapperMock.readValue(eq(json), any(TypeReference.class))).thenReturn(expectedResponse);

    // When
    List<PaTaxonomyDTO> actualResponse = pagoPaApiClient.getTaxonomies();

    // Then
    assertEquals(expectedResponse, actualResponse);
    verify(restTemplateMock, times(1)).getForObject(baseUrl, String.class);
    verify(objectMapperMock, times(1)).readValue(eq(json), any(TypeReference.class));
  }

}
