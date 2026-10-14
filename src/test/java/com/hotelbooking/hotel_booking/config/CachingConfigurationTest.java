package com.hotelbooking.hotel_booking.config;

import com.hotelbooking.hotel_booking.entity.SignupOtpConfiguration;
import com.hotelbooking.hotel_booking.repository.SignupOtpConfigurationRepository;
import com.hotelbooking.hotel_booking.service.SignupOtpConfigurationService;
import com.hotelbooking.hotel_booking.service.SignupOtpSettingsAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachingConfigurationTest {
    private static final short CONFIGURATION_ID = 1;

    private final SignupOtpConfigurationRepository repository =
            mock(SignupOtpConfigurationRepository.class);

    private ApplicationContextRunner runner() {
        SignupOtpConfiguration row = mock(SignupOtpConfiguration.class);
        when(row.getValidityMinutes()).thenReturn(7);
        when(repository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(row));
        return new ApplicationContextRunner()
                .withUserConfiguration(CachingConfiguration.class)
                .withBean(SignupOtpConfigurationService.class, () -> new SignupOtpConfigurationService(
                        repository, mock(PlatformTransactionManager.class)));
    }

    @Test
    void blankRedisUrlUsesInMemoryCache() {
        runner().withPropertyValues("spring.data.redis.url=").run(context -> {
            assertThat(context.getBean(CacheManager.class))
                    .isInstanceOf(ConcurrentMapCacheManager.class);

            SignupOtpConfigurationService service = context.getBean(SignupOtpConfigurationService.class);
            assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7));
            assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(7));
            verify(repository, times(1)).findById(CONFIGURATION_ID);

            service.evictOtpValidity();
            service.getOtpValidity();
            verify(repository, times(2)).findById(CONFIGURATION_ID);
        });
    }

    @Test
    void appSettingsEntriesNeverExpire() {
        RedisCacheManager cacheManager = CachingConfiguration.redisCacheManager(
                mock(org.springframework.data.redis.connection.RedisConnectionFactory.class));
        cacheManager.initializeCaches();

        assertThat(cacheManager.getCacheConfigurations()
                .get(CachingConfiguration.APP_SETTINGS_CACHE)
                .getTtlFunction()
                .getTimeToLive("otp.validity.seconds", Duration.ofMinutes(5)))
                .isEqualTo(Duration.ZERO);
    }

    @Test
    void adminUpdateEvictsCacheSoNextReadSeesNewValue() {
        SignupOtpConfiguration row = new SignupOtpConfiguration(CONFIGURATION_ID);
        row.updateValidityMinutes(5, java.time.LocalDateTime.now());
        runner().withPropertyValues("spring.data.redis.url=")
                .withBean(SignupOtpConfigurationRepository.class, () -> repository)
                .withBean(SignupOtpSettingsAdminService.class)
                .run(context -> {
                    when(repository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(row));
                    when(repository.save(org.mockito.ArgumentMatchers.any()))
                            .thenAnswer(invocation -> invocation.getArgument(0));
                    SignupOtpConfigurationService service =
                            context.getBean(SignupOtpConfigurationService.class);

                    assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(5));
                    assertThat(service.getOtpValidity()).isEqualTo(Duration.ofMinutes(5)); // cached

                    context.getBean(SignupOtpSettingsAdminService.class).updateValidityMinutes(10);

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
                    service.evictOtpValidity();

                    // Every get failed, so every call went to the database.
                    verify(repository, times(2)).findById(CONFIGURATION_ID);
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
                    service.evictOtpValidity();

                    verify(repository, times(2)).findById(CONFIGURATION_ID);
                });
    }

    @Test
    void appSettingsSerializerRoundTripsDuration() {
        RedisSerializer<Duration> serializer = CachingConfiguration.appSettingsSerializer();

        Object restored = serializer.deserialize(serializer.serialize(Duration.ofMinutes(5)));

        assertThat(restored).isInstanceOf(Duration.class).isEqualTo(Duration.ofMinutes(5));
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
