package com.tormento.challenge.truelayer.yap.api;

import com.tormento.challenge.truelayer.yap.YapApplication;
import com.tormento.challenge.truelayer.yap.api.model.ErrorResponse;
import com.tormento.challenge.truelayer.yap.api.model.Pokemon;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.main.web-application-type=reactive")
@ContextConfiguration(classes = YapApplication.class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:test.properties")
public class YapRestApiTest {
    // Sleep time to avoid 429 too many requests when invoking external services
    private static final int SLEEP_TIME = 1;

    @Autowired
    WebTestClient webClient;

    @BeforeAll
    void setUp() {
    }

    @Test
    public void getStandardPokemonInfo() throws InterruptedException {
        TimeUnit.SECONDS.sleep(SLEEP_TIME);

        Flux<Pokemon> resp = this.webClient.get().uri("/pokemon/{name}", "charizard")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Pokemon.class)
                .getResponseBody();

        Pokemon pokemon = resp.blockFirst();
        assertNotNull(pokemon);
        assertEquals("charizard", pokemon.getName());
        assertFalse(pokemon.getIsLegendary());
        assertEquals("mountain", pokemon.getHabitat());
        assertEquals("Spits fire that is hot enough to melt boulders. Known to cause forest fires unintentionally.",
                pokemon.getDescription());
    }

    @Test
    public void getShakespearianPokemonInfo() throws InterruptedException {
        TimeUnit.SECONDS.sleep(SLEEP_TIME);

        Flux<Pokemon> resp = this.webClient.get().uri("/pokemon/translated/{name}", "charizard")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Pokemon.class)
                .getResponseBody();

        Pokemon pokemon = resp.blockFirst();
        assertNotNull(pokemon);
        assertEquals("charizard", pokemon.getName());
        assertFalse(pokemon.getIsLegendary());
        assertEquals("mountain", pokemon.getHabitat());
        assertEquals("Spits fire yond is hot enow to melt boulders. Known to cause forest fires unintentionally.",
                pokemon.getDescription());
    }

    @Test
    public void getYodaesquePokemonInfo() throws InterruptedException {
        TimeUnit.SECONDS.sleep(SLEEP_TIME);

        Flux<Pokemon> resp = this.webClient.get().uri("/pokemon/translated/{name}", "mewtwo")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Pokemon.class)
                .getResponseBody();

        Pokemon pokemon = resp.blockFirst();
        assertNotNull(pokemon);
        assertEquals("mewtwo", pokemon.getName());
        assertTrue(pokemon.getIsLegendary());
        assertEquals("rare", pokemon.getHabitat());
        assertEquals("Created by a scientist after years of horrific gene splicing and dna engineering experiments,  it was.",
                pokemon.getDescription());
    }

    @Test
    public void getNonExistentPokemonInfo() throws InterruptedException {
        TimeUnit.SECONDS.sleep(SLEEP_TIME);

        Flux<ErrorResponse> resp = this.webClient.get().uri("/pokemon/{name}", "nonexistent")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(ErrorResponse.class)
                .getResponseBody();

        ErrorResponse errorResponse = resp.blockFirst();
        assertNotNull(errorResponse);
        assertEquals("pokemon 'nonexistent' does not exist", errorResponse.getMessage());
    }
}