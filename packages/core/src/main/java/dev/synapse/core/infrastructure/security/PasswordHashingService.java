package dev.synapse.core.infrastructure.security;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordHashingService {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 3;
    private static final int MEMORY_KB = 65536; // 64 MB
    private static final int PARALLELISM = 1;

    private final SecureRandom secureRandom;

    public PasswordHashingService() {
        this.secureRandom = new SecureRandom();
    }

    public String hash(String plainPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withIterations(ITERATIONS)
            .withMemoryAsKB(MEMORY_KB)
            .withParallelism(PARALLELISM)
            .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(plainPassword.toCharArray(), hash);

        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        String encodedHash = Base64.getEncoder().encodeToString(hash);

        return String.format("$argon2id$v=19$m=%d,t=%d,p=%d$%s$%s",
            MEMORY_KB, ITERATIONS, PARALLELISM, encodedSalt, encodedHash);
    }

    public boolean verify(String plainPassword, String hashedPassword) {
        try {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length != 6 || !parts[1].equals("argon2id")) {
                return false;
            }

            String[] params = parts[3].split(",");
            int memory = Integer.parseInt(params[0].split("=")[1]);
            int iterations = Integer.parseInt(params[1].split("=")[1]);
            int parallelism = Integer.parseInt(params[2].split("=")[1]);

            byte[] salt = Base64.getDecoder().decode(parts[4]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[5]);

            Argon2Parameters argonParams = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withIterations(iterations)
                .withMemoryAsKB(memory)
                .withParallelism(parallelism)
                .build();

            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(argonParams);

            byte[] computedHash = new byte[expectedHash.length];
            generator.generateBytes(plainPassword.toCharArray(), computedHash);

            return constantTimeEquals(expectedHash, computedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
