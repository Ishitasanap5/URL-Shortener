package com.ishita.urlshortener.util;

import java.util.BitSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BloomFilter {

    private final BitSet bitSet;
    private final int size;
    private final int hashCount;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public BloomFilter(long expectedElements, double falsePositiveRate) {
        this(optimalBitCount(expectedElements, falsePositiveRate),
                optimalHashCount(optimalBitCount(expectedElements, falsePositiveRate), expectedElements));
    }

    public BloomFilter(int size) {
        this(size, 3);
    }

    private BloomFilter(int size, int hashCount) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        this.size = size;
        this.hashCount = hashCount;
        this.bitSet = new BitSet(size);
    }

    public static int optimalBitCount(long expectedElements, double falsePositiveRate) {
        if (expectedElements <= 0) {
            throw new IllegalArgumentException("expectedElements must be positive");
        }
        if (falsePositiveRate <= 0 || falsePositiveRate >= 1) {
            throw new IllegalArgumentException("falsePositiveRate must be between 0 and 1");
        }
        double bits = -expectedElements * Math.log(falsePositiveRate) / (Math.log(2) * Math.log(2));
        return (int) Math.min(Integer.MAX_VALUE - 64, Math.ceil(bits));
    }

    public static int optimalHashCount(int bitCount, long expectedElements) {
        return Math.max(1, (int) Math.round((double) bitCount / expectedElements * Math.log(2)));
    }

    public void add(String value) {
        lock.writeLock().lock();
        try {
            for (int index : getIndices(value)) {
                bitSet.set(index);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean mightContain(String value) {
        lock.readLock().lock();
        try {
            for (int index : getIndices(value)) {
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
        if (normalize(h2) == 0) {
            h2 = 1;
        }
        int[] indices = new int[hashCount];
        for (int i = 0; i < hashCount; i++) {
            indices[i] = normalize(h1 + i * h2);
        }
        return indices;
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