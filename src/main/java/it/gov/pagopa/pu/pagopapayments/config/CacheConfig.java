package it.gov.pagopa.pu.pagopapayments.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "cache")
@EnableCaching
@Data
@FieldNameConstants
public class CacheConfig {

    @NestedConfigurationProperty
    private CacheConfigurationProperties brokerApiKeyAndSegregationCodes;
    @NestedConfigurationProperty
    private CacheConfigurationProperties brokerApiKeyAndFiscalCode;
    @NestedConfigurationProperty
    private CacheConfigurationProperties debtPositionTypeOrg;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheConfigurationProperties {
        private long size;
        private long expireIn;
    }

    @Bean
    public CacheManager localCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(Fields.brokerApiKeyAndSegregationCodes, buildCache(brokerApiKeyAndSegregationCodes));
        cacheManager.registerCustomCache(Fields.brokerApiKeyAndFiscalCode, buildCache(brokerApiKeyAndFiscalCode));
        cacheManager.registerCustomCache(Fields.debtPositionTypeOrg, buildCache(debtPositionTypeOrg));
        return cacheManager;
    }

    private Cache<Object, Object> buildCache(CacheConfigurationProperties cacheConfig) {
        return Caffeine.newBuilder()
                .maximumSize(cacheConfig.size)
                .expireAfterAccess(cacheConfig.expireIn, TimeUnit.MINUTES)
                .build();
    }
}
