package com.ishita.urlshortener.util;

import java.util.BitSet;

public class BloomFilter {

    private final BitSet bitSet;
    private final int size;

    public BloomFilter(int size) {
        this.size = size;
        this.bitSet = new BitSet(size);
    }


    private int hash1(String value) {
        return mix(value.hashCode());
    }

    private int hash2(String value) {
        return mix(value.hashCode() ^ (value.hashCode() >>> 16));
    }

    private int mix(int hash) {
        return (hash & 0x7fffffff) % size;
    }

    public synchronized void add(String value) {

        int h1 = hash1(value);
        int h2 = hash2(value);
        int h3 = (h1 + h2) % size;

        bitSet.set(h1);
        bitSet.set(h2);
        bitSet.set(h3);
    }


    public boolean mightContain(String value) {

        int h1 = hash1(value);
        int h2 = hash2(value);
        int h3 = (h1 + h2) % size;

        return bitSet.get(h1)
                && bitSet.get(h2)
                && bitSet.get(h3);
    }
}