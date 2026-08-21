package com.hinchmart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HinchMart B2B Marketplace API")
                        .description("REST API documentation for HinchMart B2B Marketplace - Auth, Users, Product Catalog, Bulk Pricing, and RFQ Platform.")
                        .version("1.0.0")
                        .contact(new Contact().name("HinchMart Engineering Team").email("dev@hinchmart.com"))
                        .license(new License().name("Proprietary").url("https://hinchmart.com")));
    }
}
