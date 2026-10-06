package com.ishita.urlshortener.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void generatesStrictlyIncreasingIdsSingleThreaded() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1);

        long previous = -1;
        for (int i = 0; i < 10_000; i++) {
            long id = generator.generateId();
            assertTrue(id > previous, "ids must be strictly increasing");
            previous = id;
        }
    }

    @Test
    void idsAreUniqueUnderConcurrentGeneration() throws InterruptedException, ExecutionException {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1);
        int threads = 8;
        int perThread = 5_000;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            var futures = new java.util.ArrayList<Future<long[]>>();
            for (int t = 0; t < threads; t++) {
                futures.add(pool.submit(() -> {
                    long[] ids = new long[perThread];
                    for (int i = 0; i < perThread; i++) {
                        ids[i] = generator.generateId();
                    }
                    return ids;
                }));
            }

            Set<Long> seen = new HashSet<>();
            for (Future<long[]> future : futures) {
                for (long id : future.get()) {
                    assertTrue(seen.add(id), "duplicate id generated: " + id);
                }
            }
            assertEquals(threads * perThread, seen.size());
        } finally {
            pool.shutdown();
        }
    }

    @Test
    void rejectsOutOfRangeMachineId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(-1));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(1 << 10));
    }
}
