package com.tormento.challenge.truelayer.yap.exception;

import com.tormento.challenge.truelayer.yap.api.model.Pokemon;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        setMessageReaders(configurer.getReaders());
        setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(
            ErrorAttributes errorAttributes) {

        return RouterFunctions.route(
                RequestPredicates.all(), request -> {
                    Throwable error = errorAttributes.getError(request);
                    Map<String, Object> errorPropertiesMap = getErrorAttributes(request,
                            ErrorAttributeOptions.defaults());

                    errorPropertiesMap.remove("status");
                    errorPropertiesMap.remove("timestamp");
                    errorPropertiesMap.remove("path");
                    errorPropertiesMap.remove("error");
                    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                    if (error instanceof PokemonDoesNotExistException) {
                       httpStatus = HttpStatus.BAD_REQUEST;
                    }
                    errorPropertiesMap.put("message", error.getMessage());

                    return ServerResponse.status(httpStatus)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(errorPropertiesMap));
                });
    }
}
