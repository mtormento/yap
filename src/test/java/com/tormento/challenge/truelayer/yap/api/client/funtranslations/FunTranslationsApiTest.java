package com.tormento.challenge.truelayer.yap.api.client.funtranslations;

import com.tormento.challenge.truelayer.yap.api.client.funtranslations.FunTranslationsApi;
import com.tormento.challenge.truelayer.yap.api.client.funtranslations.model.Response;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * API tests for FunTranslationsApi
 */
public class FunTranslationsApiTest {

    private final FunTranslationsApi api = new FunTranslationsApi();
/*
    @Test
    public void translateTest()  {
        String type = "yoda";
        String text = "this is a test";
        Response response = api.translate(type, text).block();
        assertNotNull(response);
        assertTrue(response.getSuccess().getTotal().compareTo(BigDecimal.ZERO) > 0);
        assertEquals("A test,  this is", response.getContents().getTranslated());
        assertEquals("yoda", response.getContents().getTranslation());
        assertEquals("this is a test", response.getContents().getText());
    }

    @Test
    public void emptyTextTest()  {
        String type = "yoda";
        String text = "";
        Response response = api.translate(type, text).block();
        assertNotNull(response);
        assertTrue(response.getSuccess().getTotal().compareTo(BigDecimal.ZERO) > 0);
        assertEquals("", response.getContents().getTranslated());
        assertEquals("yoda", response.getContents().getTranslation());
        assertEquals("", response.getContents().getText());
    }
*/
}
