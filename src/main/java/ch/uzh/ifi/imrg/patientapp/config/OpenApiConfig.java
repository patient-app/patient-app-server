package ch.uzh.ifi.imrg.patientapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = "X-Coach-Key"))
@SecurityScheme(name = "X-Coach-Key", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-Coach-Key", description = "Your coach’s API key")
public class OpenApiConfig {

}