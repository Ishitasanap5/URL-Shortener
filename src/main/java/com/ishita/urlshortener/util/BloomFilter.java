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


    // ADD (WRITE LOCK)
    public void add(String value) {
        lock.writeLock().lock();
        try {
            int hash = value.hashCode();
            int index1 = Math.abs(hash % size);
            int index2 = Math.abs((hash * 31) % size);
            int index3 = Math.abs((hash * 17) % size);

            bitSet.set(index1);
            bitSet.set(index2);
            bitSet.set(index3);

        } finally {
            lock.writeLock().unlock();
        }
    }


    // CHECK (READ LOCK)
    public boolean mightContain(String value) {
        lock.readLock().lock();
        try {
            int hash = value.hashCode();
            int index1 = Math.abs(hash % size);
            int index2 = Math.abs((hash * 31) % size);
            int index3 = Math.abs((hash * 17) % size);

            return bitSet.get(index1)
                    && bitSet.get(index2)
                    && bitSet.get(index3);

        } finally {
            lock.readLock().unlock();
        }
    }
}