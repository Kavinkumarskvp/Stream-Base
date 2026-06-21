package kavin.personal_project.streambase.sharding;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
public class SnowFlakeIdGenerator {

    private static final long EPOCH = 1704067200000L; // 2024-01-01 UTC
    private static final long MACHINE_ID_BITS = 10;
    private static final long SEQUENCE_BITS = 12;
    private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1; // 1023
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1; // 4095

    private final long machineId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowFlakeIdGenerator() {

        // For demo: random machine ID. Production would use HOSTNAME or k8s pod index.
        this.machineId = ThreadLocalRandom.current().nextLong(
                0,
                MAX_MACHINE_ID + 1
        );
    }

    @PostConstruct
    public void init() {
        log.info("SnowflakeIdGenerator initialized with machineId={}",
                machineId
        );
    }

    public synchronized long nextId() {

        long now = Instant.now().toEpochMilli();
        if (now < lastTimestamp) {
            // Clock went backwards (NTP sync, VM pause). Block until we catch up.
            now = lastTimestamp;
        }

        if (now == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // Same millisecond, sequence exhausted — wait for next ms
                while (now <= lastTimestamp) {
                    now = Instant.now().toEpochMilli();
                }
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = now;

        return ((now - EPOCH) << (MACHINE_ID_BITS + SEQUENCE_BITS))
               | (machineId << SEQUENCE_BITS)
               | sequence;
    }
}
