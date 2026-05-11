package dev.synapse.core.infrastructure.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "synapse:token:blacklist:";

    private final SetOperations<String, String> setOperations;
    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.setOperations = redisTemplate.opsForSet();
    }

    /**
     * Revokes a token by adding its JTI to the blacklist.
     * The blacklist entry expires after the given TTL to match the token's expiry.
     *
     * @param jti       the JWT ID to revoke
     * @param ttlMs     time-to-live in milliseconds matching the token's remaining validity
     */
    public void revokeToken(String jti, long ttlMs) {
        String key = BLACKLIST_KEY_PREFIX + jti;
        setOperations.add(key, jti);
        redisTemplate.expire(key, ttlMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Checks whether a given JTI has been revoked.
     *
     * @param jti the JWT ID to check
     * @return true if the token is blacklisted
     */
    public boolean isTokenRevoked(String jti) {
        String key = BLACKLIST_KEY_PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
