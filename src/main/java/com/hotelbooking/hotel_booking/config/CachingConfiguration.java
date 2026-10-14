package com.hotelbooking.hotel_booking.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

/**
 * Cache layer. Uses Upstash Redis when {@code spring.data.redis.url} is set,
 * otherwise an in-memory cache so the project runs without a Redis account.
 * Either way, cache failures are logged and swallowed: a dead cache must cost
 * performance, never availability.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CachingConfiguration implements CachingConfigurer {
    public static final String APP_SETTINGS_CACHE = "appSettings";

    // Duration.ZERO = never expires. Cached settings stay until evicted by the
    // admin API (PUT /api/admin/settings/signup-otp calls evictOtpValidity()).
    // Consequences: an edit made directly in the database is NOT picked up
    // until someone runs DEL on the Redis key (or calls the admin API), and if
    // an eviction fails because Redis is briefly unreachable, the old value
    // stays until the next successful eviction.
    static final Duration APP_SETTINGS_TTL = Duration.ZERO;

    // Namespaces every key so the Upstash database can be shared later.
    static final String KEY_PREFIX = "hotelbooking:";

    static final Duration REDIS_CONNECT_TIMEOUT = Duration.ofSeconds(2);
    static final Duration REDIS_COMMAND_TIMEOUT = Duration.ofSeconds(2);

    private static final String REDIS_URL_PROPERTY = "spring.data.redis.url";

    @Override
    public CacheErrorHandler errorHandler() {
        return new WarningCacheErrorHandler();
    }

    static RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .prefixCacheNameWith(KEY_PREFIX)
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withCacheConfiguration(APP_SETTINGS_CACHE, defaults
                        .entryTtl(APP_SETTINGS_TTL)
                        .serializeValuesWith(SerializationPair.fromSerializer(appSettingsSerializer())))
                .build();
    }

    // The generic serializer writes JDK scalars (Duration, Long, ...) WITHOUT
    // type information: a Duration comes back as the String "PT5M" and a Long
    // as an Integer, which would surface as a ClassCastException in the caller
    // on the first cache hit. appSettings therefore uses a serializer bound to
    // Duration, its only value type. A setting of another type needs its own cache.
    static RedisSerializer<Duration> appSettingsSerializer() {
        return new JacksonJsonRedisSerializer<>(Duration.class);
    }

    // Default for other caches. Spring Boot 4 uses Jackson 3 (tools.jackson),
    // so this is the Jackson 3 serializer, not the legacy
    // GenericJackson2JsonRedisSerializer. Type information is embedded for
    // application objects; the validator limits polymorphic types to JDK and
    // this application's classes instead of unrestricted default typing.
    static RedisSerializer<Object> valueSerializer() {
        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("java.lang.")
                        .allowIfSubType("java.time.")
                        .allowIfSubType("java.util.")
                        .allowIfSubType("com.hotelbooking.hotel_booking.")
                        .build())
                .build();
    }

    // Spring Boot's Redis auto-configuration is excluded in application.yml:
    // it rejects a blank spring.data.redis.url at startup, and a blank URL is
    // the supported "no Redis" mode. The connection factory is built here,
    // only when a URL is configured.
    @Configuration(proxyBeanMethods = false)
    @Conditional(RedisUrlConfigured.class)
    static class RedisCacheBackend {
        @Bean
        LettuceConnectionFactory redisConnectionFactory(
                @Value("${" + REDIS_URL_PROPERTY + "}") String url) {
            return lettuceConnectionFactory(parseRedisUrl(url));
        }

        @Bean
        CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
            return redisCacheManager(connectionFactory);
        }
    }

    static LettuceConnectionFactory lettuceConnectionFactory(RedisURI uri) {
        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                // TLS (rediss://), credentials and client name come from the URL.
                .apply(uri)
                // Upstash is a remote network hop, not localhost: bound every wait.
                .commandTimeout(REDIS_COMMAND_TIMEOUT)
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .connectTimeout(REDIS_CONNECT_TIMEOUT)
                                .build())
                        // While the connection is down, fail commands at once
                        // instead of queueing them until the command timeout;
                        // the error handler then falls through to the database.
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .build())
                .build();
        return new LettuceConnectionFactory(
                LettuceConnectionFactory.createRedisConfiguration(uri), clientConfiguration);
    }

    /**
     * Parses a redis:// or rediss:// URL, or returns null when it is blank or
     * malformed. Redis is optional, so a bad URL must never stop startup.
     */
    static RedisURI parseRedisUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String trimmed = url.trim();
        String lower = trimmed.toLowerCase(java.util.Locale.ROOT);
        if (!lower.startsWith("redis://") && !lower.startsWith("rediss://")) {
            return null;
        }
        try {
            RedisURI uri = RedisURI.create(trimmed);
            return StringUtils.hasText(uri.getHost()) ? uri : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Conditional(RedisUrlMissing.class)
    static class InMemoryCacheBackend {
        private static final Logger log = LoggerFactory.getLogger(InMemoryCacheBackend.class);

        @Bean
        CacheManager cacheManager(@Value("${" + REDIS_URL_PROPERTY + ":}") String url) {
            if (StringUtils.hasText(url)) {
                // Never log the URL or the parse error: both contain the password.
                log.error("REDIS_URL is set but is not a valid redis:// or rediss:// URL "
                        + "(expected rediss://default:<password>@<host>:6379); "
                        + "using the in-memory cache instead");
            }
            ConcurrentMapCacheManager cacheManager =
                    new ConcurrentMapCacheManager(APP_SETTINGS_CACHE);
            cacheManager.setAllowNullValues(false);
            return cacheManager;
        }
    }

    static class RedisUrlConfigured implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return parseRedisUrl(context.getEnvironment().getProperty(REDIS_URL_PROPERTY)) != null;
        }
    }

    static class RedisUrlMissing implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return parseRedisUrl(context.getEnvironment().getProperty(REDIS_URL_PROPERTY)) == null;
        }
    }

    static class WarningCacheErrorHandler implements CacheErrorHandler {
        private static final Logger log = LoggerFactory.getLogger(WarningCacheErrorHandler.class);

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache get failed for {}::{}; reading from the database instead",
                    cache.getName(), key, exception);
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache,
                                        Object key, Object value) {
            log.warn("Cache put failed for {}::{}; value was not cached",
                    cache.getName(), key, exception);
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache evict failed for {}::{}; the stale entry expires with its TTL",
                    cache.getName(), key, exception);
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("Cache clear failed for {}; entries expire with their TTL",
                    cache.getName(), exception);
        }
    }
}
