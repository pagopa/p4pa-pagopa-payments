package it.gov.pagopa.pu.pagopapayments.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import it.gov.pagopa.pu.pagopapayments.util.CertUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

import static com.nimbusds.jose.JOSEObjectType.JWT;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  public static final String SOAP_WS_BASE_PATH = "/soap";

  @Bean
  public JwtDecoder jwtDecoder(
    @Value("${jwt.access-token.public-key}") String publicKey
  )
    throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

    RSAPublicKey rsaPublicKey = CertUtils.pemPub2PublicKey(publicKey);
    return NimbusJwtDecoder
      .withPublicKey(rsaPublicKey)
      .signatureAlgorithm(SignatureAlgorithm.RS512)
      .jwtProcessorCustomizer(processor -> processor
        .setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(JWT, new JOSEObjectType("at+jwt"))))
      .build();
  }

  public Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter(){
    return source -> {
      String userExternalId = source.getSubject();
      MDC.put("externalUserId", userExternalId);
      return new JwtAuthenticationToken(source, null, userExternalId);
    };
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(requests -> requests
        // Swagger endpoints
        .requestMatchers(
          "/swagger-ui.html",
          "/swagger-ui/**",
          "/v3/api-docs/**"
        ).permitAll()

        // Actuator endpoints
        .requestMatchers(
          "/actuator",
          "/actuator/**"
        ).permitAll()

        // WebMVC
        .requestMatchers(
          "/favicon.ico", "/error"
        ).permitAll()

        // WsSoap
        .requestMatchers(
          SOAP_WS_BASE_PATH+"/**"
        ).permitAll()

        .anyRequest().authenticated()
      )
      .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .oauth2ResourceServer(c -> c
        .jwt(jwt -> jwt
          .decoder(jwtDecoder)
          .jwtAuthenticationConverter(jwtAuthenticationConverter()))
      );

    return http.build();
  }
}
