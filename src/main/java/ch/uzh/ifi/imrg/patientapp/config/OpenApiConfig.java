package ch.uzh.ifi.imrg.patientapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = "X-Coach-Key"))
@SecurityScheme(name = "X-Coach-Key", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-Coach-Key", description = "Your coach’s API key")
public class OpenApiConfig {

    @Value("${SWAGGER_SERVER_URL:}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI oas = new OpenAPI();
        if (!swaggerServerUrl.isBlank()) {
            oas.addServersItem(new Server().url(swaggerServerUrl));
        }
        return oas;
    }

}