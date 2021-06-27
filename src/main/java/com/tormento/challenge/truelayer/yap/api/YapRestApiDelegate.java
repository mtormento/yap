package com.tormento.challenge.truelayer.yap.api;

import com.tormento.challenge.truelayer.yap.api.client.funtranslations.FunTranslationsApi;
import com.tormento.challenge.truelayer.yap.api.client.funtranslations.model.Response;
import com.tormento.challenge.truelayer.yap.api.client.pokeapi.PokemonSpeciesApi;
import com.tormento.challenge.truelayer.yap.api.client.pokeapi.model.PokemonSpecies;
import com.tormento.challenge.truelayer.yap.api.model.Pokemon;
import com.tormento.challenge.truelayer.yap.exception.PokeApiException;
import com.tormento.challenge.truelayer.yap.exception.PokemonDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class YapRestApiDelegate  implements PokemonApiDelegate {
    private static final String LANGUAGE_ENGLISH = "en";
    private static final String HABITAT_CAVE = "cave";
    private static final String TRANSLATION_LANGUAGE_YODA = "yoda";
    private static final String TRANSLATION_LANGUAGE_SHAKESPEARE = "shakespeare";

    private Logger log = LoggerFactory.getLogger(YapRestApiDelegate.class);

    private PokemonSpeciesApi pokemonSpeciesApi;
    private FunTranslationsApi funTranslationsApi;

    @Autowired
    public YapRestApiDelegate(PokemonSpeciesApi pokemonSpeciesApi, FunTranslationsApi funTranslationsApi) {
        this.pokemonSpeciesApi = pokemonSpeciesApi;
        this.funTranslationsApi = funTranslationsApi;
        this.pokemonSpeciesApi.getApiClient()
               .addDefaultHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(Duration.ofMinutes(60)).getHeaderValue());
        this.funTranslationsApi.getApiClient()
                .addDefaultHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(Duration.ofMinutes(60)).getHeaderValue());
    }

    @Override
    public Mono<ResponseEntity<Pokemon>> getPokemonInfo(String name, ServerWebExchange exchange) {
        log.info("getPokemonInfo invoked - name={}", name);
        return pokemonSpeciesApi.getPokemonSpecies(name)
                .onErrorResume(e -> handlePokeApiError("getPokemonInfo", e, name))
                .map(species -> {
                    Pokemon pokemon = new Pokemon();
                    pokemon.setName(name);
                    pokemon.setHabitat(species.getHabitat().getName());
                    pokemon.setIsLegendary(species.getIsLegendary());
                    species.getFlavorTextEntries()
                            .stream()
                            .filter(f -> f.getLanguage().getName().equalsIgnoreCase("en"))
                            .findAny()
                            .ifPresent(f -> pokemon.setDescription(f.getFlavorText()
                                    .replaceAll("\n", " ")
                                    .replaceAll("\f", " ")));
                    log.info("getPokemonInfo success!");
                    log.debug("getPokemonInfo response: {}", pokemon.toString());
                    return ResponseEntity.status(HttpStatus.OK).body(pokemon);
                });
    }

    @Override
    public Mono<ResponseEntity<Pokemon>> getTranslatedPokemonInfo(String name, ServerWebExchange exchange) {
        log.info("getTranslatedPokemonInfo invoked - name={}", name);
        return pokemonSpeciesApi.getPokemonSpecies(name)
                .onErrorResume(e -> handlePokeApiError("getTranslatedPokemonInfo", e, name))
                .flatMap(species -> {
                    String habitat = species.getHabitat().getName();
                    boolean isLegendary = species.getIsLegendary();
                    Pokemon pokemon = new Pokemon();
                    pokemon.setName(name);
                    pokemon.setHabitat(habitat);
                    pokemon.setIsLegendary(isLegendary);
                    species.getFlavorTextEntries()
                            .stream()
                            .filter(f -> f.getLanguage().getName().equalsIgnoreCase(LANGUAGE_ENGLISH))
                            .findAny()
                            .ifPresent(f -> pokemon.setDescription(f.getFlavorText()));
                    boolean useYodaTranslation = habitat.equalsIgnoreCase(HABITAT_CAVE) || isLegendary;
                    return funTranslationsApi.translate(useYodaTranslation ? TRANSLATION_LANGUAGE_YODA : TRANSLATION_LANGUAGE_SHAKESPEARE, pokemon.getDescription())
                            .onErrorResume(e -> handleFunTranslationsApiError("getTranslatedPokemonInfo", e, name))
                            .map(response -> {
                                if (response.getSuccess().getTotal().compareTo(BigDecimal.ZERO) > 0) {
                                    pokemon.setDescription(response.getContents().getTranslated());
                                }
                                log.info("getTranslatedPokemonInfo success!");
                                log.debug("getTranslatedPokemonInfo response: {}", pokemon.toString());
                                return ResponseEntity.status(HttpStatus.OK).body(pokemon);
                            });
                });
    }

    private Mono<? extends PokemonSpecies> handlePokeApiError(String method, Throwable e, String name) {
        if (e.getMessage().contains("404")) {
            String message = String.format("pokemon '%s' does not exist", name);
            log.warn("{} failed: {}", method, message);
            return Mono.error(new PokemonDoesNotExistException(message, e));
        } else {
            log.error("{} error invoking Poke api: {}", method, e.getMessage());
            return Mono.error(new PokeApiException(e.getMessage(), e));
        }
    }

    private Mono<? extends Response> handleFunTranslationsApiError(String method, Throwable e, String name) {
        log.error("{} error invoking FunTranslations api: {}", method, e.getMessage());
        return Mono.error(new PokeApiException(e.getMessage(), e));
    }
}
