package com.ishita.urlshortener.util;

public class Base62Encoder {

    private static final String CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int BASE = 62;

    public static String encode(long id) {
        if (id < 0) throw new IllegalArgumentException("ID must be non-negative: " + id);
        if (id == 0) return String.valueOf(CHARS.charAt(0));

        StringBuilder sb = new StringBuilder();

        while (id > 0) {
            sb.append(CHARS.charAt((int) (id % BASE)));
            id /= BASE;
        }

        return sb.reverse().toString();
    }
}