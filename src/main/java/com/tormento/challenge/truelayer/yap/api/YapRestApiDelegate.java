package com.tormento.challenge.truelayer.yap.api;

import com.tormento.challenge.truelayer.yap.api.client.funtranslations.FunTranslationsApi;
import com.tormento.challenge.truelayer.yap.api.client.pokeapi.PokemonSpeciesApi;
import com.tormento.challenge.truelayer.yap.api.model.Pokemon;
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
        pokemonSpeciesApi.getApiClient()
               .addDefaultHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(Duration.ofMinutes(60)).getHeaderValue());
    }

    @Override
    public Mono<ResponseEntity<Pokemon>> getPokemonInfo(String name, ServerWebExchange exchange) {
        log.info("getPokemonInfo invoked - name={}", name);
        return pokemonSpeciesApi.getPokemonSpecies(name).map(species -> {
            Pokemon pokemon = new Pokemon();
            pokemon.setName(name);
            pokemon.setHabitat(species.getHabitat().getName());
            pokemon.setIsLegendary(species.getIsLegendary());
            species.getFlavorTextEntries()
                    .stream()
                    .filter(f -> f.getLanguage().getName().equalsIgnoreCase("en"))
                    .findAny()
                    .ifPresent(f -> pokemon.setDescription(f.getFlavorText()));
            log.info("getPokemonInfo success!");
            log.debug("getPokemonInfo response: {}", pokemon.toString());
            return ResponseEntity.status(HttpStatus.OK).body(pokemon);
        });
    }

    @Override
    public Mono<ResponseEntity<Pokemon>> getTranslatedPokemonInfo(String name, ServerWebExchange exchange) {
        log.info("getTranslatedPokemonInfo invoked - name={}", name);
        return pokemonSpeciesApi.getPokemonSpecies(name).flatMap(species -> {
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
}
