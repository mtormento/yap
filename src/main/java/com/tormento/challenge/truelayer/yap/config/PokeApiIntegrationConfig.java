package com.tormento.challenge.truelayer.yap.config;

import com.tormento.challenge.truelayer.yap.api.client.pokeapi.ApiClient;
import com.tormento.challenge.truelayer.yap.api.client.pokeapi.PokeApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PokeApiIntegrationConfig {

    @Bean
    public PokeApi pokeApi() {
        return new PokeApi(pokeApiClient());
    }

    @Bean
    public ApiClient pokeApiClient() {
        return new ApiClient();
    }
}