package it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.pagopapayments.connector.pagopa.taxonomy.dto.PaTaxonomyDTO;
import it.gov.pagopa.pu.pagopapayments.exception.InvalidValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoPaTaxonomyApiClientTest {

  @Mock
  private RestTemplate restTemplateMock;

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  @Mock
  private ObjectMapper objectMapperMock;

  private PagoPaTaxonomyApiClient pagoPaTaxonomyApiClient;

  private final String baseUrl = "http://example.com";

  @BeforeEach
  void setUp() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    pagoPaTaxonomyApiClient = new PagoPaTaxonomyApiClient(baseUrl, true, restTemplateBuilderMock, objectMapperMock);
  }

  @Test
  void getTaxonomiesThrowsApplicationExceptionOnJsonProcessingException() throws JsonProcessingException {
    // Given
    when(restTemplateMock.getForObject(anyString(), eq(String.class))).thenReturn("invalid json");
    doThrow(new JsonProcessingException("Error") {}).when(objectMapperMock).readValue(anyString(), any(TypeReference.class));

    // When & Then
    assertThrows(InvalidValueException.class, () -> pagoPaTaxonomyApiClient.getTaxonomies());
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
    List<PaTaxonomyDTO> actualResponse = pagoPaTaxonomyApiClient.getTaxonomies();

    // Then
    assertEquals(expectedResponse, actualResponse);
    verify(restTemplateMock, times(1)).getForObject(baseUrl, String.class);
    verify(objectMapperMock, times(1)).readValue(eq(json), any(TypeReference.class));
  }

}
