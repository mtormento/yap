package com.tormento.challenge.yap.config;

import com.tormento.challenge.yap.api.client.pokeapi.ApiClient;
import com.tormento.challenge.yap.api.client.pokeapi.PokemonSpeciesApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PokeApiIntegrationConfig {

    @Bean
    public PokemonSpeciesApi pokemonSpeciesApi() {
        return new PokemonSpeciesApi(pokemonSpeciesApiClient());
    }

    @Bean
    public ApiClient pokemonSpeciesApiClient() {
        return new ApiClient();
    }
}