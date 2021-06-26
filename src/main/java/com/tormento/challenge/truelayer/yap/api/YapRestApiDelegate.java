package com.tormento.challenge.truelayer.yap.api;

import com.tormento.challenge.truelayer.yap.api.client.funtranslations.FunTranslationsApi;
import com.tormento.challenge.truelayer.yap.api.client.pokeapi.PokeApi;
import com.tormento.challenge.truelayer.yap.api.client.pokeapi.model.FlavorTextEntry;
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
    private Logger log = LoggerFactory.getLogger(YapRestApiDelegate.class);
    private PokeApi pokeApi;
    private FunTranslationsApi funTranslationsApi;

    @Autowired
    public YapRestApiDelegate(PokeApi pokeApi, FunTranslationsApi funTranslationsApi) {
        this.pokeApi = pokeApi;
        this.funTranslationsApi = funTranslationsApi;
        pokeApi.getApiClient()
               .addDefaultHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(Duration.ofMinutes(60)).getHeaderValue());
    }

    @Override
    public Mono<ResponseEntity<Pokemon>> getPokemonInfo(String name, ServerWebExchange exchange) {
        return pokeApi.getPokemonSpecies(name).map(species -> {
            Pokemon pokemon = new Pokemon();
            pokemon.setName(name);
            pokemon.setHabitat(species.getHabitat().getName());
            pokemon.setIsLegendary(species.getIsLegendary());
            species.getFlavorTextEntries()
                    .stream()
                    .filter(f -> f.getLanguage().getName().equalsIgnoreCase("en"))
                    .findAny()
                    .ifPresent(f -> pokemon.setDescription(f.getFlavorText()));
            return ResponseEntity.status(HttpStatus.OK).body(pokemon);
        });
    }

    @Override
    public Mono<ResponseEntity<Pokemon>> getTranslatedPokemonInfo(String name, ServerWebExchange exchange) {
        return pokeApi.getPokemonSpecies(name).flatMap(species -> {
            String habitat = species.getHabitat().getName();
            boolean isLegendary = species.getIsLegendary();
            Pokemon pokemon = new Pokemon();
            pokemon.setName(name);
            pokemon.setHabitat(habitat);
            pokemon.setIsLegendary(isLegendary);
            species.getFlavorTextEntries()
                    .stream()
                    .filter(f -> f.getLanguage().getName().equalsIgnoreCase("en"))
                    .findAny()
                    .ifPresent(f -> pokemon.setDescription(f.getFlavorText()));
            boolean useYodaTranslation = habitat.equalsIgnoreCase("cave") || isLegendary;
            return funTranslationsApi.funTranslate(useYodaTranslation ? "yoda" : "shakespeare", pokemon.getDescription())
                    .map(response -> {
                        if (response.getSuccess().getTotal().compareTo(BigDecimal.ZERO) > 0) {
                            pokemon.setDescription(response.getContents().getTranslated());
                        }
                        return ResponseEntity.status(HttpStatus.OK).body(pokemon);
                    });
        });
    }
}
