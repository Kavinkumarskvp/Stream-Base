package kavin.personal_project.streambase.sharding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ConsistentHashRing {

    private final int virtualNodesPerShard;
    // ring: hashedPosition -> shardName
    private final SortedMap<Long, String> ring = new TreeMap<>();
    private final List<String> shards = new ArrayList<>();

    public ConsistentHashRing(int virtualNodesPerShard) {
        this.virtualNodesPerShard = virtualNodesPerShard;
    }

    public synchronized void addShard(String shardName) {

        if (shards.contains(shardName)) return;
        shards.add(shardName);
        for (int i = 0; i < virtualNodesPerShard; i++) {
            long position = hash(shardName + "#vn" + i);
            ring.put(position, shardName);
        }
    }

    public synchronized void removeShard(String shardName) {

        if (!shards.remove(shardName)) return;
        for (int i = 0; i < virtualNodesPerShard; i++) {
            long position = hash(shardName + "#vn" + i);
            ring.remove(position, shardName);
        }
    }

    /**
     * Returns which shard owns this key.
     */
    public synchronized String getShard(String key) {

        if (ring.isEmpty()) {
            throw new IllegalStateException("Ring is empty — no shards added");
        }

        long h = hash(key);
        // Walk clockwise: first shard at position >= h, else wrap to the smallest
        SortedMap<Long, String> tail = ring.tailMap(h);
        return tail.isEmpty()
                ? ring.get(ring.firstKey())
                : tail.get(tail.firstKey());
    }

    public List<String> getShards() {
        return Collections.unmodifiableList(shards);
    }

    /**
     * MD5-based hash — gives a uniform 64-bit value.
     */
    private long hash(String key) {
        try {

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(key.getBytes(StandardCharsets.UTF_8));

            // Take first 8 bytes as a long
            long h = 0;
            for (int i = 0; i < 8; i++) {
                h = (h << 8) | (digest[i] & 0xFF);
            }
            return h;

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
