package com.tormento.challenge.yap.exception;

public class PokemonDoesNotExistException extends Exception {
    public PokemonDoesNotExistException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
