package com.ishita.urlshortener.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {

    @Test
    void encodesZeroAsFirstChar() {
        assertEquals("a", Base62Encoder.encode(0));
    }

    @Test
    void encodesSmallPositiveId() {
        // 61 == 'Z' + 1 wraps back to base 62's second digit
        assertEquals("b", Base62Encoder.encode(1));
        assertEquals("9", Base62Encoder.encode(61));
        assertEquals("ba", Base62Encoder.encode(62));
    }

    @Test
    void isReversibleForARangeOfIds() {
        for (long id : new long[]{0, 1, 61, 62, 999, 123_456_789L, Long.MAX_VALUE / 2}) {
            String encoded = Base62Encoder.encode(id);
            assertEquals(id, decode(encoded), "round-trip failed for id=" + id);
        }
    }

    @Test
    void rejectsNegativeIds() {
        assertThrows(IllegalArgumentException.class, () -> Base62Encoder.encode(-1));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 1000L, 1_000_000_000L, Long.MAX_VALUE})
    void neverProducesEmptyOrNullOutput(long id) {
        String encoded = Base62Encoder.encode(id);
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    private static final String CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private long decode(String s) {
        long result = 0;
        for (char c : s.toCharArray()) {
            result = result * 62 + CHARS.indexOf(c);
        }
        return result;
    }
}
