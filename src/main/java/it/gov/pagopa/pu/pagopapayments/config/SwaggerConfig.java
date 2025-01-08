package it.gov.pagopa.pu.pagopapayments.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * The Class SwaggerConfig.
 */
@Configuration
@OpenAPIDefinition(
  info = @Info(
    title = "${spring.application.name}",
    version = "${spring.application.version}",
    description = "Api and Models"
  ),
  security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(
  name = "BearerAuth",
  type = SecuritySchemeType.HTTP,
  bearerFormat = "JWT",
  scheme = "bearer"
)
public class SwaggerConfig {
}
