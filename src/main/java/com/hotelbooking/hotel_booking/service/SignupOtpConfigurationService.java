package com.hotelbooking.hotel_booking.service;

import com.hotelbooking.hotel_booking.config.CachingConfiguration;
import com.hotelbooking.hotel_booking.entity.SignupOtpConfiguration;
import com.hotelbooking.hotel_booking.repository.SignupOtpConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;

@Service
public class SignupOtpConfigurationService {
    private static final Logger log = LoggerFactory.getLogger(SignupOtpConfigurationService.class);

    static final Duration DEFAULT_OTP_VALIDITY = Duration.ofSeconds(300);
    static final Duration MIN_OTP_VALIDITY = Duration.ofSeconds(30);
    static final Duration MAX_OTP_VALIDITY = Duration.ofSeconds(86_400);

    static final short CONFIGURATION_ID = 1;

    private final SignupOtpConfigurationRepository repository;
    private final TransactionTemplate readInOwnTransaction;

    public SignupOtpConfigurationService(SignupOtpConfigurationRepository repository,
                                         PlatformTransactionManager transactionManager) {
        this.repository = repository;
        // The read runs in its own transaction so that a failure here (e.g. the
        // table not yet migrated) cannot mark the caller's signup transaction
        // rollback-only or leave its PostgreSQL transaction aborted.
        this.readInOwnTransaction = new TransactionTemplate(transactionManager);
        this.readInOwnTransaction.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.readInOwnTransaction.setReadOnly(true);
    }

    /**
     * Signup OTP validity from "Production".signup_otp_configuration. A missing
     * row, unreadable table or out-of-range value never fails the request: it
     * logs a warning and returns {@link #DEFAULT_OTP_VALIDITY}.
     */
    @Cacheable(cacheNames = CachingConfiguration.APP_SETTINGS_CACHE, key = "'otp.validity.seconds'")
    public Duration getOtpValidity() {
        Integer minutes;
        try {
            minutes = readInOwnTransaction.execute(status -> repository.findById(CONFIGURATION_ID)
                    .map(SignupOtpConfiguration::getValidityMinutes)
                    .orElse(null));
        } catch (RuntimeException e) {
            log.warn("Could not read signup OTP configuration; using default {}s",
                    DEFAULT_OTP_VALIDITY.toSeconds(), e);
            return DEFAULT_OTP_VALIDITY;
        }

        if (minutes == null) {
            log.warn("Signup OTP configuration row id={} is missing or empty; using default {}s",
                    CONFIGURATION_ID, DEFAULT_OTP_VALIDITY.toSeconds());
            return DEFAULT_OTP_VALIDITY;
        }

        Duration validity = Duration.ofMinutes(minutes);
        if (validity.compareTo(MIN_OTP_VALIDITY) < 0 || validity.compareTo(MAX_OTP_VALIDITY) > 0) {
            log.warn("Signup OTP validity of {} minutes is outside {}s..{}s; using default {}s",
                    minutes, MIN_OTP_VALIDITY.toSeconds(), MAX_OTP_VALIDITY.toSeconds(),
                    DEFAULT_OTP_VALIDITY.toSeconds());
            return DEFAULT_OTP_VALIDITY;
        }
        return validity;
    }

    /**
     * Drops the cached validity so the next read hits the database. Intended
     * for a future admin endpoint; nothing calls it yet.
     * <p>
     * Cache annotations are applied by a Spring proxy, so this has no effect if
     * called from another method of this same bean (self-invocation). Call it
     * through the injected bean.
     */
    @CacheEvict(cacheNames = CachingConfiguration.APP_SETTINGS_CACHE, key = "'otp.validity.seconds'")
    public void evictOtpValidity() {
    }
}
