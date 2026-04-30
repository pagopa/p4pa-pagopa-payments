package it.gov.pagopa.pu.pagopapayments.config;

import it.gov.pagopa.pu.pagopapayments.util.SecurityUtils;
import it.gov.pagopa.pu.pagopapayments.util.SecurityUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

  // public key and JWT token sample obtained through p4pa-auth test it.gov.pagopa.payhub.auth.service.AccessTokenBuilderServiceTest
  private static final String PUBLIC_KEY = """
    -----BEGIN PUBLIC KEY-----
    MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2ovm/rd3g69dq9PisinQ
    6mWy8ZttT8D+GKXCsHZycsGnN7b74TPyYy+4+h+9cgJeizp8RDRrufHjiBrqi/2r
    eOk/rD7ZHbpfQvHK8MYfgIVdtTxYMX/GGdOrX6/5TV2b8e2aCG6GmxF0UuEvxY9o
    TmcZUxnIeDtl/ixz4DQ754eS363qWfEA92opW+jcYzr07sbQtR86e+Z/s/CUeX6W
    1PHNvBqdlAgp2ecr/1DOLq1D9hEANBPSwbt+FM6FNe4vLphi7GTwiB0yaAuy+jE8
    odND6HPvvvmgbK1/2qTHn/HJjWUm11LUC73BszR32BKbdEEhxPQnnwswVekWzPi1
    IwIDAQAB
    -----END PUBLIC KEY-----
    """;
  private static final String JWT_TOKEN_USERID = "MAPPEDUSEREXTERNALID";
  private static final String JWT_TOKEN = "eyJraWQiOiIyNWNhZDlkYi0wMDIyLTNiODctYTcwYS1mMmRhMjcyMTdjODgiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJ0eXAiOiJiZWFyZXIiLCJpc3MiOiJBUFBMSUNBVElPTl9BVURJRU5DRSIsImp0aSI6IjA2ZWZmMzhjLTZhZDEtNGU5Ni1iYmYyLTUxYWVlMTFiNzZmYyIsInN1YiI6Ik1BUFBFRFVTRVJFWFRFUk5BTElEIiwiaWF0IjoxNzM2MDgwNTMzLCJleHAiOjI3MzYwODA1MzIsIm9yZ2FuaXphdGlvbklwYUNvZGUiOiJPUkdJUEFDT0RFIn0.WFdVG5oaGU-fQnSFvuXjls95pvpeLblDOBtFHDj2nmYxce3tLlY3xayuZ42bGr-3phFA4GQtevySteTU-xJOaUDxA-i1TgbaSXIJ7SohQoC4uhrP3uCrmd8B_NRVohNZyIzgQHh_8JaNrb_TsPhHcS88hQqtDf4XwG-o10dZUQaiAD3MQrNiPVWsvCIIzh9uMifvjGpu98b-qdiYF7O_638Sy-Rvy2XltNGn_60ZmhZl7tXJZHI4iZmVXhjLf0-924ONpMt68uQB7kBPzs_2rkT_A2oa1qncpVkRLf5m1JI4pDkmo5BrshtR4zxoEbxK--YRxJeFk25zYzc6xPRE8w";
  private static final String JWT_TOKEN_SYSTEM_USER = "eyJraWQiOiIyNWNhZDlkYi0wMDIyLTNiODctYTcwYS1mMmRhMjcyMTdjODgiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJ0eXAiOiJiZWFyZXIiLCJpc3MiOiJBUFBMSUNBVElPTl9BVURJRU5DRSIsImp0aSI6IjA2ZWZmMzhjLTZhZDEtNGU5Ni1iYmYyLTUxYWVlMTFiNzZmYyIsInN1YiI6IldTX1VTRVItcGlhdHRhZm9ybWEtdW5pdGFyaWFfIiwiaWF0IjoxNzM2MDgwNTMzLCJleHAiOjI3MzYwODA1MzIsIm9yZ2FuaXphdGlvbklwYUNvZGUiOiJPUkdJUEFDT0RFIn0.oeXlZahH5cJjJhbklWGtXj0stXg-k1BdO1rm8Q7Cnia5sxdj3j6upFnReG1Sry8S1RDBv7AIr_3i1sW5BItPW-LHLDP-j4FvzZBMVfVNkBy-6GET0ktEjILzv7Wrz_dwLvdQSvmBF6BQW1gr3gyJ-uCSy5d3hRRDk_4himGEhWFn4DDRyqgUzvKHjugpc8NLwsEsdH2v9AQo8RTXjowavLPLyf6tp3-GQQPJkAur1-HZUYfixSENSUj0H2UOYakX6aDbrwYGW3bOUz-K8YCoG9k15C6Voc9zjwQ4sUPHMTnJh2T-YECxmokQ95MaZGF1C_pxVsGo6Ox2ql8SfFB-3g";

  private final WebSecurityConfig securityConfig = new WebSecurityConfig();
  private final JwtDecoder jwtDecoder = securityConfig.jwtDecoder(PUBLIC_KEY);
  private final Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter = securityConfig.jwtAuthenticationConverter();

  WebSecurityConfigTest() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    SecurityContextHolder.clearContext();
    RequestContextHolder.resetRequestAttributes();
    MDC.clear();
  }

  @Test
  void givenNotValidTokenWhenJwtDecoderDecodeThenBadJwtException() {
    Assertions.assertThrows(BadJwtException.class, () -> jwtDecoder.decode("INVALID"));
  }

  @Test
  void givenValidTokenWhenJwtDecoderDecodeAndJwtAuthenticationConverterConvertThenMdcConfigured() {
    Jwt jwt = jwtDecoder.decode(JWT_TOKEN);
    jwtAuthenticationConverter.convert(jwt);

    Assertions.assertEquals(JWT_TOKEN_USERID, MDC.get("externalUserId"));
  }

  @Test
  void givenSystemUserTokenAndNotUserIdWhenJwtDecoderDecodeAndJwtAuthenticationConverterConvertThenMdcConfiguredWithJustSystemUser() {
    Jwt jwt = jwtDecoder.decode(JWT_TOKEN_SYSTEM_USER);
    jwtAuthenticationConverter.convert(jwt);

    Assertions.assertEquals(SecurityUtils.SYSTEM_USERID_PREFIX, MDC.get("externalUserId"));
  }

  @Test
  void givenSystemUserTokenAndUserIdWhenJwtDecoderDecodeAndJwtAuthenticationConverterConvertThenMdcConfiguredWithJustSystemUser() {
    Jwt jwt = jwtDecoder.decode(JWT_TOKEN_SYSTEM_USER);
    SecurityUtilsTest.configureXUserIdHeader(JWT_TOKEN_USERID);
    jwtAuthenticationConverter.convert(jwt);

    Assertions.assertEquals(SecurityUtils.SYSTEM_USERID_PREFIX+"]["+JWT_TOKEN_USERID, MDC.get("externalUserId"));
  }
}
