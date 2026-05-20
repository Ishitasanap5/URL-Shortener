package com.ishita.urlshortener.util;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {

    private static final AtomicLong counter =
            new AtomicLong(System.currentTimeMillis());

    public static long generateId() {

        return counter.incrementAndGet();
    }
}