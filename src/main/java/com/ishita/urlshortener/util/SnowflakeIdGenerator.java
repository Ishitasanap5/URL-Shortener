package com.ishita.urlshortener.util;

public class SnowflakeIdGenerator {

    // Epoch (custom start time)
    private static final long EPOCH = 1704067200000L;


    private static final long MACHINE_BITS = 10;
    private static final long SEQUENCE_BITS = 12;

    // Limits
    private static final long MAX_MACHINE_ID = (1L << MACHINE_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    // Fields
    private final long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long machineId) {

        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException(
                    "Machine ID must be between 0 and " + MAX_MACHINE_ID
            );
        }

        this.machineId = machineId;
    }

    // MAIN METHOD
    public synchronized long generateId() {

        long currentTimestamp = System.currentTimeMillis();

        //Clock moved backwards → unsafe to continue
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException(
                    "Clock moved backwards. Refusing to generate ID."
            );
        }

        // ⏱ Same millisecond → increment sequence
        if (currentTimestamp == lastTimestamp) {

            sequence = (sequence + 1) & MAX_SEQUENCE;

            //Overflow → wait for next millisecond
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }

        } else {
            // New millisecond → reset sequence
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        //Compose ID
        return ((currentTimestamp - EPOCH) << (MACHINE_BITS + SEQUENCE_BITS))
                | (machineId << SEQUENCE_BITS)
                | sequence;
    }

    // HELPER
    private long waitNextMillis(long lastTimestamp) {

        long timestamp = System.currentTimeMillis();

        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }

        return timestamp;
    }
}