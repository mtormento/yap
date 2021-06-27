package com.tormento.challenge.truelayer.yap.exception;

public class PokemonDoesNotExistException extends Exception {
    public PokemonDoesNotExistException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
