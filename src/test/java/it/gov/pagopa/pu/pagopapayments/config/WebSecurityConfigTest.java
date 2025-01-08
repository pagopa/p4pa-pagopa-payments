package it.gov.pagopa.pu.pagopapayments.config;

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
  private static final String JWT_TOKEN = "eyJraWQiOiIyNWNhZDlkYi0wMDIyLTNiODctYTcwYS1mMmRhMjcyMTdjODgiLCJ0eXAiOiJhdCtKV1QiLCJhbGciOiJSUzUxMiJ9.eyJ0eXAiOiJiZWFyZXIiLCJpc3MiOiJBUFBMSUNBVElPTl9BVURJRU5DRSIsImp0aSI6IjA2ZWZmMzhjLTZhZDEtNGU5Ni1iYmYyLTUxYWVlMTFiNzZmYyIsInN1YiI6Ik1BUFBFRFVTRVJFWFRFUk5BTElEIiwiaWF0IjoxNzM2MDgwNTMzLCJleHAiOjI3MzYwODA1MzIsIm9yZ2FuaXphdGlvbklwYUNvZGUiOiJPUkdJUEFDT0RFIn0.qfcPvKVW6GOPC-Hb4QfqEpfT1zwrZ30QRbW2RPvrAlaBdYi51ZTmy6iWIcoy7YubkkctRp7xHDgcQuMRyRzGr2S-FayTA7kHXwa0y9UOnb7FXuZn9j0G6-4qVqlH6qo2KKTuDl_HykDAEmbI0AMJXilN8cM_ZkIQXCv6mDWsQCcxglsxcw89G0U9m5cZ5n9RxaAikMp8xRssiSqoFdhA67j-Iqs9P0vC-L0YvrIuqJ8CuJxoZQX_rPh-aLAzjPswctT_yaUk2tX5XpYG_1Yo0k9Mxy7CyyUa1JbRLRWbXkfOCPDbBOMn6KkXU_2w3pj4u6sIZsWuTNGT4d8zBye8JA";

  private final WebSecurityConfig securityConfig = new WebSecurityConfig();
  private final JwtDecoder jwtDecoder = securityConfig.jwtDecoder(PUBLIC_KEY);
  private final Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter = securityConfig.jwtAuthenticationConverter();

  WebSecurityConfigTest() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    SecurityContextHolder.clearContext();
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
}
