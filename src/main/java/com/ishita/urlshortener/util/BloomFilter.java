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


    private int[] getIndices(String value) {
        int h1 = value.hashCode();
        int h2 = murmurLike(h1);
        // Double hashing: gi(x) = h1(x) + i * h2(x)
        // Gives k independent indices from just two base hashes
        return new int[]{
                normalize(h1),
                normalize(h1 + h2),
                normalize(h1 + 2 * h2)
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