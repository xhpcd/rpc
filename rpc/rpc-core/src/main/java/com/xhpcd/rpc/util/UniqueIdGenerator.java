package com.xhpcd.rpc.util;

public class UniqueIdGenerator {

    private static final long EPOCH = 1420041600000L; // 2015-01-01 00:00:00 UTC
    private static final int WORKER_ID_BITS = 5;
    private static final int DATACENTER_ID_BITS = 5;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_WORKER_ID = (1 << WORKER_ID_BITS) - 1;
    private static final long MAX_DATACENTER_ID = (1 << DATACENTER_ID_BITS) - 1;
    private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;

    private static long lastTimestamp = -1L;
    private static long sequence = 0L;
    private static long workerId = 0L;
    private static long datacenterId = 0L;

    public static long generateUniqueId() {
        long timestamp = System.currentTimeMillis();
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << (WORKER_ID_BITS + DATACENTER_ID_BITS + SEQUENCE_BITS))
                | (workerId << (DATACENTER_ID_BITS + SEQUENCE_BITS))
                | (datacenterId << SEQUENCE_BITS)
                | sequence;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
