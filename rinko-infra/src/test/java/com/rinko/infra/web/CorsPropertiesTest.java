package com.rinko.infra.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CorsPropertiesTest {

    @Test
    void defaultAllowCredentialsShouldBeFalse() {
        CorsProperties props = new CorsProperties();
        assertFalse(props.isAllowCredentials(), "allowCredentials should default to false");
    }

    @Test
    void defaultAllowedOriginsShouldBeEmpty() {
        CorsProperties props = new CorsProperties();
        assertTrue(props.getAllowedOrigins().isEmpty(), "allowedOrigins should default to empty list");
    }

    @Test
    void defaultAllowedHeadersShouldBeEmpty() {
        CorsProperties props = new CorsProperties();
        assertTrue(props.getAllowedHeaders().isEmpty(), "allowedHeaders should default to empty list");
    }

    @Test
    void defaultEnabledShouldBeFalse() {
        CorsProperties props = new CorsProperties();
        assertFalse(props.isEnabled(), "CORS should be disabled by default");
    }

    @Test
    void settersShouldOverrideDefaults() {
        CorsProperties props = new CorsProperties();
        props.setEnabled(true);
        props.setAllowedOrigins(java.util.List.of("https://example.com"));
        props.setAllowCredentials(true);

        assertTrue(props.isEnabled());
        assertEquals(1, props.getAllowedOrigins().size());
        assertTrue(props.isAllowCredentials());
    }
}
