package com.rinko.infra.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XssFilterTest {

    @Test
    void sanitizeJsonStringValues_shouldEscapeHtmlInJsonValues() {
        String json = "{\"name\":\"<script>alert('xss')</script>\",\"age\":30}";
        String result = XssFilter.sanitizeJsonStringValues(json);
        assertTrue(result.contains("&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;"));
        assertFalse(result.contains("<script>"));
    }

    @Test
    void sanitizeJsonStringValues_shouldNotTouchJsonStructure() {
        String json = "{\"key\":\"value\",\"list\":[1,2,3]}";
        String result = XssFilter.sanitizeJsonStringValues(json);
        assertEquals(json, result);
    }

    @Test
    void sanitizeJsonStringValues_shouldHandleEmptyJson() {
        assertEquals("{}", XssFilter.sanitizeJsonStringValues("{}"));
        assertEquals("", XssFilter.sanitizeJsonStringValues(""));
        assertNull(XssFilter.sanitizeJsonStringValues(null));
    }

    @Test
    void sanitizeJsonStringValues_shouldHandleMultipleStrings() {
        String json = "{\"a\":\"<b>\",\"c\":\"&d;\"}";
        String result = XssFilter.sanitizeJsonStringValues(json);
        assertTrue(result.contains("&lt;b&gt;"));
        assertTrue(result.contains("&amp;d;"));
    }

    @Test
    void sanitizeJsonStringValues_shouldHandleEscapedQuotes() {
        String json = "{\"msg\":\"hello \\\"world\\\"\"}";
        String result = XssFilter.sanitizeJsonStringValues(json);
        assertTrue(result.contains("hello \\\"world\\\""));
    }

    @Test
    void sanitizeJsonStringValues_shouldHandleNestedObjects() {
        String json = "{\"user\":{\"name\":\"<evil>\",\"id\":1}}";
        String result = XssFilter.sanitizeJsonStringValues(json);
        assertTrue(result.contains("&lt;evil&gt;"));
        assertFalse(result.contains("<evil>"));
    }

    @Test
    void sanitizeJsonStringValues_shouldHandleUnterminatedStrings() {
        String json = "{\"bad\":\"unterminated}";
        String result = XssFilter.sanitizeJsonStringValues(json);
        assertNotNull(result);
    }
}
