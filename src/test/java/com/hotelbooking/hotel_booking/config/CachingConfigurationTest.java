package com.hotelbooking.hotel_booking.config;

import com.hotelbooking.hotel_booking.entity.ConfigurationEntry;
import com.hotelbooking.hotel_booking.repository.ConfigurationEntryRepository;
import com.hotelbooking.hotel_booking.service.ConfigurationAdminService;
import com.hotelbooking.hotel_booking.service.ConfigurationService;
import com.hotelbooking.hotel_booking.service.SignupOtpConfigurationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachingConfigurationTest {
    private static final String OTP_KEY = "signup.otp.validity.minutes";

    private final ConfigurationEntryRepository repository = mock(ConfigurationEntryRepository.class);
    private final ConfigurationEntry otpEntry = entry("7");

    private ApplicationContextRunner runner() {
        when(repository.findById(OTP_KEY)).thenReturn(Optional.of(otpEntry));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return new ApplicationContextRunner()
                .withUserConfiguration(CachingConfiguration.class)
                .withBean(ConfigurationEntryRepository.class, () -> repository)
                .withBean(ConfigurationService.class)
                .withBean(SignupOtpConfigurationService.class)
                .withBean(ConfigurationAdminService.class);
    }

    @Test
    void blankRedisUrlUsesInMemoryCacheKeyedByConfigKey() {
        runner().withPropertyValues("spring.data.redis.url=").run(context -> {
            CacheManager cacheManager = context.getBean(CacheManager.class);
            assertThat(cacheManager).isInstanceOf(ConcurrentMapCacheManager.class);

            SignupOtpConfigurationService service = context.getBean(SignupOtpConfigurationService.class);
            assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7));
            assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7));
            verify(repository, times(1)).findById(OTP_KEY);

            // The cache entry is stored under the configuration key itself.
            assertThat(cacheManager.getCache(CachingConfiguration.APP_SETTINGS_CACHE)
                    .get(OTP_KEY).get()).isEqualTo("7");

            context.getBean(ConfigurationService.class).evict(OTP_KEY);
            service.getOtpValidity();
            verify(repository, times(2)).findById(OTP_KEY);
        });
    }

    @Test
    void missingValueIsNotCachedSoALaterReadRetries() {
        runner().withPropertyValues("spring.data.redis.url=").run(context -> {
            when(repository.findById(OTP_KEY)).thenReturn(Optional.empty());
            ConfigurationService configuration = context.getBean(ConfigurationService.class);

            assertThat(configuration.getValue(OTP_KEY)).isNull();
            assertThat(configuration.getValue(OTP_KEY)).isNull();

            verify(repository, times(2)).findById(OTP_KEY);
        });
    }

    @Test
    void appSettingsEntriesNeverExpire() {
        RedisCacheManager cacheManager = CachingConfiguration.redisCacheManager(
                mock(RedisConnectionFactory.class));
        cacheManager.initializeCaches();

        assertThat(cacheManager.getCacheConfigurations()
                .get(CachingConfiguration.APP_SETTINGS_CACHE)
                .getTtlFunction()
                .getTimeToLive(OTP_KEY, "5"))
                .isEqualTo(Duration.ZERO);
    }

    @Test
    void adminUpdateEvictsCacheSoNextReadSeesNewValue() {
        runner().withPropertyValues("spring.data.redis.url=").run(context -> {
            SignupOtpConfigurationService service = context.getBean(SignupOtpConfigurationService.class);

            assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7));
            assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7)); // cached

            context.getBean(ConfigurationAdminService.class).update(OTP_KEY, "10");

            // Eviction went through the real cache proxy, so the new value is read at once.
            assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(10));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "rediss:/default:fake-token@example.upstash.io:6379",
            "\"rediss://default:fake-token@example.upstash.io:6379\"",
            "redis-cli --tls -u rediss://default:fake-token@example.upstash.io:6379",
            "https://example.upstash.io",
            "not a url"})
    void malformedRedisUrlFallsBackToInMemoryCacheInsteadOfFailingStartup(String url) {
        runner().withPropertyValues("spring.data.redis.url=" + url).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(CacheManager.class))
                    .isInstanceOf(ConcurrentMapCacheManager.class);
            assertThat(context.getBean(SignupOtpConfigurationService.class).getOtpValidity())
                    .isEqualTo(Duration.ofMinutes(7));
        });
    }

    @Test
    void wellFormedUpstashUrlSelectsRedis() {
        assertThat(CachingConfiguration.parseRedisUrl(
                "rediss://default:fake-token@example.upstash.io:6379"))
                .isNotNull()
                .satisfies(uri -> {
                    assertThat(uri.getHost()).isEqualTo("example.upstash.io");
                    assertThat(uri.isSsl()).isTrue();
                });
    }

    @Test
    void cacheFailuresDoNotPropagateAndFallBackToDatabase() {
        runner().withPropertyValues("spring.data.redis.url=")
                .withBean("cacheManager", CacheManager.class, CachingConfigurationTest::brokenCacheManager)
                .withAllowBeanDefinitionOverriding(true)
                .run(context -> {
                    SignupOtpConfigurationService service =
                            context.getBean(SignupOtpConfigurationService.class);

                    assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7));
                    assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7));
                    context.getBean(ConfigurationService.class).evict(OTP_KEY);

                    // Every get failed, so every call went to the database.
                    verify(repository, times(2)).findById(OTP_KEY);
                });
    }

    @Test
    void unreachableRedisDoesNotPropagateAndFallsBackToDatabase() {
        // Nothing listens on port 1: the real Redis backend against a dead endpoint.
        runner().withPropertyValues("spring.data.redis.url=redis://127.0.0.1:1")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(CacheManager.class))
                            .isInstanceOf(RedisCacheManager.class);

                    SignupOtpConfigurationService service =
                            context.getBean(SignupOtpConfigurationService.class);
                    assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7));
                    assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7));
                    context.getBean(ConfigurationService.class).evict(OTP_KEY);

                    verify(repository, times(2)).findById(OTP_KEY);
                });
    }

    @Test
    void appSettingsSerializerStoresPlainText() {
        RedisSerializer<String> serializer = CachingConfiguration.appSettingsSerializer();

        byte[] stored = serializer.serialize("5");

        assertThat(new String(stored, java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("5");
        assertThat(serializer.deserialize(stored)).isEqualTo("5");
    }

    private static ConfigurationEntry entry(String value) {
        ConfigurationEntry entry = new ConfigurationEntry(OTP_KEY, "test");
        entry.updateValue(value, LocalDateTime.now());
        return entry;
    }

    private static CacheManager brokenCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(new BrokenCache(CachingConfiguration.APP_SETTINGS_CACHE)));
        cacheManager.initializeCaches();
        return cacheManager;
    }

    private static final class BrokenCache extends AbstractValueAdaptingCache {
        private final String name;

        BrokenCache(String name) {
            super(false);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return this;
        }

        @Override
        protected Object lookup(Object key) {
            throw new IllegalStateException("cache is down");
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            throw new IllegalStateException("cache is down");
        }

        @Override
        public void put(Object key, Object value) {
            throw new IllegalStateException("cache is down");
        }

        @Override
        public void evict(Object key) {
            throw new IllegalStateException("cache is down");
        }

        @Override
        public void clear() {
            throw new IllegalStateException("cache is down");
        }
    }
}
