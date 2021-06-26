package com.tormento.challenge.truelayer.yap.config;

import com.tormento.challenge.truelayer.yap.api.client.funtranslations.ApiClient;
import com.tormento.challenge.truelayer.yap.api.client.funtranslations.FunTranslationsApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FunTranslationsIntegrationConfig {

    @Bean
    public FunTranslationsApi funTranslationsApi() {
        return new FunTranslationsApi(funTranslationsApiClient());
    }

    @Bean
    public ApiClient funTranslationsApiClient() {
        return new ApiClient();
    }
}