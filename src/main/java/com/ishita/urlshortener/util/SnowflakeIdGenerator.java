package com.ishita.urlshortener.util;

public class SnowflakeIdGenerator {

    private static final long EPOCH = 1704067200000L;

    private static final long MACHINE_BITS = 10;

    private static final long SEQUENCE_BITS = 12;

    private static final long MAX_MACHINE_ID =
            (1L << MACHINE_BITS) - 1;

    private static final long MAX_SEQUENCE =
            (1L << SEQUENCE_BITS) - 1;

    private final long machineId;

    private long sequence = 0;

    private long lastTimestamp = -1;

    // Constructor
    public SnowflakeIdGenerator(long machineId) {

        if (machineId > MAX_MACHINE_ID) {

            throw new IllegalArgumentException(
                    "Invalid machine id"
            );
        }

        this.machineId = machineId;
    }


    public synchronized long generateId() {

        long currentTimestamp =
                System.currentTimeMillis();

        if (currentTimestamp == lastTimestamp) {

            sequence =
                    (sequence + 1) & MAX_SEQUENCE;

        } else {

            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        return

                ((currentTimestamp - EPOCH)
                        << (MACHINE_BITS + SEQUENCE_BITS))

                        |

                        (machineId << SEQUENCE_BITS)

                        |

                        sequence;
    }

}