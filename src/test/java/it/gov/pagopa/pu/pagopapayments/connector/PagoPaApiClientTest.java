package it.gov.pagopa.pu.pagopapayments.connector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


class PagoPaApiClientTest {

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private RestTemplateBuilder restTemplateBuilder;

  @InjectMocks
  private PagoPaApiClient pagoPaApiClient;

  private final String baseUrlGithub = "http://example.com";
  private final String taxonomyPathContext = "/taxonomies";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
    pagoPaApiClient = new PagoPaApiClient(baseUrlGithub, taxonomyPathContext, restTemplateBuilder);
  }

  @Test
  void testGetTaxonomiesSuccess() {
    // Given
    String expectedResponse = "taxonomies";
    when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

    // When
    String actualResponse = pagoPaApiClient.getTaxonomies();

    // Then
    assertEquals(expectedResponse, actualResponse);
    verify(restTemplate, times(1)).getForObject(baseUrlGithub + taxonomyPathContext, String.class);
  }

}
