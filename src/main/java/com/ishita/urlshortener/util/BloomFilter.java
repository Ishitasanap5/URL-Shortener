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
        return Math.abs(value.hashCode()) % size;
    }

    private int hash2(String value) {
        return Math.abs((value.hashCode() * 31)) % size;
    }

    private int hash3(String value) {
        return Math.abs((value.hashCode() * 17)) % size;
    }

    public void add(String value) {
        bitSet.set(hash1(value));
        bitSet.set(hash2(value));
        bitSet.set(hash3(value));
    }

    public boolean mightContain(String value) {
        return bitSet.get(hash1(value))
                && bitSet.get(hash2(value))
                && bitSet.get(hash3(value));
    }
}