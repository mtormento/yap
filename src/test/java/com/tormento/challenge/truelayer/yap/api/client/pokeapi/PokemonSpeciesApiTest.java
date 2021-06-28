package com.tormento.challenge.truelayer.yap.api.client.pokeapi;

import com.tormento.challenge.truelayer.yap.api.client.pokeapi.model.PokemonSpecies;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.junit.Assert.*;

/**
 * API tests for PokemonSpeciesApi
 */
public class PokemonSpeciesApiTest {
    private final PokemonSpeciesApi api = new PokemonSpeciesApi();

    @Test
    public void getPokemonSpeciesTest() throws InterruptedException {
        String name = "charizard";
        PokemonSpecies response = api.getPokemonSpecies(name).block();
        assertNotNull(response);
        assertEquals("mountain", response.getHabitat().getName());
        assertTrue(response.getFlavorTextEntries().size() > 0);
        assertFalse(response.getIsLegendary());
    }

    @Test
    public void getInvalidPokemonSpeciesTest() throws InterruptedException {
        String name = "invalid";
        assertThrows(WebClientResponseException.class, () -> {
            api.getPokemonSpecies(name).block();
        });
    }
}
