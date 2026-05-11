package dev.synapse.core.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SecretValidator implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SecretValidator.class);

    private static final String DEFAULT_JWT_SECRET = "CHANGE_ME_IN_PRODUCTION_THIS_MUST_BE_AT_LEAST_256_BITS_LONG_FOR_HS256";
    private static final String DEFAULT_ENCRYPTION_KEY = "dev_key_32bytes_changeinprod__";
    private static final String DEFAULT_DB_PASSWORD = "synapse_dev_password";

    private final String jwtSecret;
    private final String encryptionKey;
    private final String dbPassword;
    private final Environment environment;

    public SecretValidator(
        @Value("${jwt.secret:CHANGE_ME_IN_PRODUCTION_THIS_MUST_BE_AT_LEAST_256_BITS_LONG_FOR_HS256}") String jwtSecret,
        @Value("${secrets.encryption-key:dev_key_32bytes_changeinprod__}") String encryptionKey,
        @Value("${spring.datasource.password:synapse_dev_password}") String dbPassword,
        Environment environment
    ) {
        this.jwtSecret = jwtSecret;
        this.encryptionKey = encryptionKey;
        this.dbPassword = dbPassword;
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("production");
        boolean hasDefault = false;

        if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            log.warn("SECURITY WARNING: jwt.secret is set to the default value. This is extremely insecure for production. Change it immediately!");
            hasDefault = true;
        }

        if (DEFAULT_ENCRYPTION_KEY.equals(encryptionKey)) {
            log.warn("SECURITY WARNING: secrets.encryption-key is set to the default value. This is extremely insecure for production. Change it immediately!");
            hasDefault = true;
        }

        if (DEFAULT_DB_PASSWORD.equals(dbPassword)) {
            log.warn("SECURITY WARNING: spring.datasource.password is set to the default value. This is extremely insecure for production. Change it immediately!");
            hasDefault = true;
        }

        if (isProduction && hasDefault) {
            throw new IllegalStateException(
                "Production profile detected with default secrets. " +
                "Override jwt.secret, secrets.encryption-key, and spring.datasource.password before running in production."
            );
        }
    }
}
