package com.ishita.urlshortener.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BloomFilterTest {

    @Test
    void neverFalseNegativeForAddedValues() {
        BloomFilter filter = new BloomFilter(10_000, 0.01);

        Set<String> added = new HashSet<>();
        for (int i = 0; i < 5_000; i++) {
            String value = "code-" + i;
            filter.add(value);
            added.add(value);
        }

        for (String value : added) {
            assertTrue(filter.mightContain(value),
                    "a Bloom filter must never false-negative on an added value: " + value);
        }
    }

    @Test
    void falsePositiveRateIsInTheRightBallpark() {
        long expectedElements = 10_000;
        double targetFalsePositiveRate = 0.01;
        BloomFilter filter = new BloomFilter(expectedElements, targetFalsePositiveRate);

        for (int i = 0; i < expectedElements; i++) {
            filter.add("real-" + i);
        }

        int falsePositives = 0;
        int trials = 20_000;
        for (int i = 0; i < trials; i++) {
            if (filter.mightContain("absent-" + UUID.randomUUID())) {
                falsePositives++;
            }
        }

        double observedRate = falsePositives / (double) trials;
        // Generous bound (5x target) since this is a probabilistic structure,
        // not an exact one — the point is to catch gross mis-sizing (e.g. the
        // previous hardcoded 1-bit-per-element / k=3 setup, which measured
        // well above this bound), not to assert an exact statistic.
        assertTrue(observedRate < targetFalsePositiveRate * 5,
                "observed false-positive rate " + observedRate +
                        " is far above the " + targetFalsePositiveRate + " target");
    }

    @Test
    void sizingHelpersProduceSensibleValues() {
        int bits = BloomFilter.optimalBitCount(1_000_000, 0.01);
        int hashes = BloomFilter.optimalHashCount(bits, 1_000_000);

        // ~9.6 bits/element and ~7 hash functions is the textbook optimum
        // for a 1% target false-positive rate.
        assertTrue(bits > 9_000_000 && bits < 10_500_000, "bits=" + bits);
        assertTrue(hashes >= 6 && hashes <= 8, "hashes=" + hashes);
    }
}
