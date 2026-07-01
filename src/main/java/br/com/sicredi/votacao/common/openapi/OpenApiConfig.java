package br.com.sicredi.votacao.common.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI votingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sicredi Voting API")
                        .description("REST API para gerenciamento de pautas, sessoes de votacao, votos e resultado da votacao.")
                        .version("v1"));
    }

    @Bean
    public GroupedOpenApi v1OpenApi() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
