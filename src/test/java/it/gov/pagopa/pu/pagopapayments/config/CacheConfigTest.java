package it.gov.pagopa.pu.pagopapayments.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class CacheConfigTest {

  private final CacheConfig cacheConfig = new CacheConfig();

  private final Random random = new Random();

  @Test
  void testCacheConfig() throws IllegalAccessException {
    // Given
    Field[] fields = CacheConfig.class.getDeclaredFields();
    Set<String> expectedCacheNames = new HashSet<>(fields.length);
    for (Field f : fields) {
      expectedCacheNames.add(f.getName());
      f.setAccessible(true);
      CacheConfig.CacheConfigurationProperties config = new CacheConfig.CacheConfigurationProperties(random.nextInt(1, 100), random.nextInt(101, 200));
      f.set(cacheConfig, config);
    }

    // When
    CacheManager cacheManager = cacheConfig.localCacheManager();

    // Then
    Assertions.assertNotNull(cacheManager);
    Assertions.assertEquals(expectedCacheNames, new HashSet<>(cacheManager.getCacheNames()));
  }
}
