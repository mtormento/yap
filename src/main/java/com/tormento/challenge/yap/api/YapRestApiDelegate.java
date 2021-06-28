package com.tormento.challenge.yap.api;

import com.tormento.challenge.yap.api.client.funtranslations.FunTranslationsApi;
import com.tormento.challenge.yap.api.client.funtranslations.model.Response;
import com.tormento.challenge.yap.api.client.pokeapi.PokemonSpeciesApi;
import com.tormento.challenge.yap.api.client.pokeapi.model.PokemonSpecies;
import com.tormento.challenge.yap.api.model.Pokemon;
import com.tormento.challenge.yap.exception.PokeApiException;
import com.tormento.challenge.yap.exception.PokemonDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Service
public class YapRestApiDelegate  implements PokemonApiDelegate {
    private static final String LANGUAGE_ENGLISH = "en";
    private static final String HABITAT_CAVE = "cave";
    private static final String TRANSLATION_LANGUAGE_YODA = "yoda";
    private static final String TRANSLATION_LANGUAGE_SHAKESPEARE = "shakespeare";
    private static final String METHOD_GET_POKEMON_INFO = "getPokemonInfo";
    private static final String METHOD_GET_TRANSLATED_POKEMON_INFO = "getTranslatedPokemonInfo";

    private Logger log = LoggerFactory.getLogger(YapRestApiDelegate.class);

    private PokemonSpeciesApi pokemonSpeciesApi;
    private FunTranslationsApi funTranslationsApi;

    @Autowired
    public YapRestApiDelegate(PokemonSpeciesApi pokemonSpeciesApi, FunTranslationsApi funTranslationsApi) {
        this.pokemonSpeciesApi = pokemonSpeciesApi;
        this.funTranslationsApi = funTranslationsApi;
    }

    @Override
    public Mono<ResponseEntity<Pokemon>> getPokemonInfo(String name, ServerWebExchange exchange) {
        log.info("getPokemonInfo invoked - name={}", name);
        return pokemonSpeciesApi.getPokemonSpecies(name)
                .onErrorMap(e -> handlePokeApiError(METHOD_GET_POKEMON_INFO, e, name))
                .map(species -> mapToPokemon(name, species))
                .map(pokemon -> mapToResponseEntity(METHOD_GET_POKEMON_INFO, pokemon));
    }

    @Override
    public Mono<ResponseEntity<Pokemon>> getTranslatedPokemonInfo(String name, ServerWebExchange exchange) {
        log.info("getTranslatedPokemonInfo invoked - name={}", name);
        return pokemonSpeciesApi.getPokemonSpecies(name)
                .onErrorMap(e -> handlePokeApiError(METHOD_GET_TRANSLATED_POKEMON_INFO, e, name))
                .map(species -> mapToPokemon(name, species))
                .flatMap(pokemon -> {
                    boolean useYodaTranslation = pokemon.getHabitat().equalsIgnoreCase(HABITAT_CAVE) || pokemon.getIsLegendary();
                    return funTranslationsApi.translate(useYodaTranslation ? TRANSLATION_LANGUAGE_YODA : TRANSLATION_LANGUAGE_SHAKESPEARE, pokemon.getDescription())
                            .onErrorResume(e -> handleFunTranslationsApiError(METHOD_GET_TRANSLATED_POKEMON_INFO, e, name))
                            .doOnNext(response -> {
                                if (response.getSuccess() != null &&
                                        response.getSuccess().getTotal().compareTo(BigDecimal.ZERO) > 0) {
                                    pokemon.setDescription(response.getContents().getTranslated());
                                }
                            })
                            .map(response -> mapToResponseEntity(METHOD_GET_TRANSLATED_POKEMON_INFO, pokemon));
                });
    }

    private ResponseEntity<Pokemon> mapToResponseEntity(String methodName, Pokemon pokemon) {
        log.info("{} success: {} info retrieved", methodName, pokemon.getName());
        log.debug("{} response: {}", methodName, pokemon.toString());
        return ResponseEntity.status(HttpStatus.OK).body(pokemon);
    }

    private Pokemon mapToPokemon(String name, PokemonSpecies species) {
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
                .ifPresent(f -> pokemon.setDescription(f.getFlavorText()
                        // Clean escape chars
                        .replaceAll("\n", " ")
                        .replaceAll("\f", " ")));
        return pokemon;
    }

    private Throwable handlePokeApiError(String methodName, Throwable e, String name) {
        if (e.getMessage().contains("404")) {
            // PokeApi responded with http status code 404, therefore a Pokemon with the specified name does not exist
            String message = String.format("pokemon '%s' does not exist", name);
            log.warn("{} failed: {}", methodName, message);
            return new PokemonDoesNotExistException(message, e);
        } else {
            log.error("{} error invoking Poke api: {}", methodName, e.getMessage());
            return new PokeApiException(e.getMessage(), e);
        }
    }

    private Mono<? extends Response> handleFunTranslationsApiError(String methodName, Throwable e, String name) {
        log.warn("{} error invoking FunTranslations api: {}", methodName, e.getMessage());
        // As per requirements, if the translation request fails we simply use the default description
        return Mono.just(new Response());
    }
}
