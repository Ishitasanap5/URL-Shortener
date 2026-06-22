package com.ishita.urlshortener.util;

import java.util.BitSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BloomFilter {

    private final BitSet bitSet;
    private final int size;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public BloomFilter(int size) {
        this.size = size;
        this.bitSet = new BitSet(size);
    }

    // -------------------------
    // ADD
    // -------------------------
    public void add(String value) {
        lock.writeLock().lock();
        try {
            int[] indices = getIndices(value);

            for (int index : indices) {
                bitSet.set(index);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    // -------------------------
    // CHECK
    // -------------------------
    public boolean mightContain(String value) {
        lock.readLock().lock();
        try {
            int[] indices = getIndices(value);

            for (int index : indices) {
                if (!bitSet.get(index)) {
                    return false;
                }
            }
            return true;

        } finally {
            lock.readLock().unlock();
        }
    }

    // -------------------------
    // HASHING (FIXED)
    // -------------------------
    private int[] getIndices(String value) {

        int hash1 = value.hashCode();
        int hash2 = murmurLike(hash1);
        int hash3 = hash2 ^ (hash2 >>> 16);

        return new int[] {
                normalize(hash1),
                normalize(hash2),
                normalize(hash3)
        };
    }

    private int murmurLike(int hash) {
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        return hash;
    }

    private int normalize(int hash) {
        return (hash & 0x7fffffff) % size;
    }
}