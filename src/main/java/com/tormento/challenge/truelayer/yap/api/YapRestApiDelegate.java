package com.tormento.challenge.truelayer.yap.api;

import com.tormento.challenge.truelayer.yap.api.model.Pokemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class YapRestApiDelegate  implements PokemonApiDelegate {
    private Logger log = LoggerFactory.getLogger(YapRestApiDelegate.class);
    private WebClient pokemonApiWebClient;

    public YapRestApiDelegate() {
        WebClient client = WebClient.builder()
                .baseUrl("https://pokeapi.co/api/v2")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CACHE_CONTROL, CacheControl.maxAge(Duration.ofMinutes(60)).getHeaderValue())
                .build();
    }
}
