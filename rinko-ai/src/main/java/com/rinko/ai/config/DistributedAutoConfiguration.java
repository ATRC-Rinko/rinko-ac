package com.rinko.ai.config;

import io.agentscope.harness.agent.DistributedStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * 生产环境分布式配置。
 *
 * <p>仅在引入 agentscope-extensions-redis，且 rinko.ai.distributed.store-type=redis 时激活。
 * 使用反射避免硬编码 Redis 依赖，确保不引入该扩展时模块仍可正常编译。</p>
 *
 * <p>实际项目中，直接替换为显式 import 和类型安全的调用即可：</p>
 * <pre>{@code
 * import io.agentscope.extensions.redis.RedisDistributedStore;
 * import redis.clients.jedis.JedisPooled;
 *
 * DistributedStore store = RedisDistributedStore.fromJedis(
 *     new JedisPooled("redis://prod-redis:6379"));
 * }</pre>
 */
@Configuration
@AutoConfigureAfter(AgentScopeAutoConfiguration.class)
@ConditionalOnClass(name = "io.agentscope.extensions.redis.RedisDistributedStore")
@ConditionalOnProperty(prefix = "rinko.ai.distributed", name = "store-type", havingValue = "redis")
public class DistributedAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DistributedAutoConfiguration.class);

    private final AiProperties properties;

    public DistributedAutoConfiguration(AiProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建 Redis DistributedStore。
     *
     * <p>需要同时引入以下依赖：</p>
     * <pre>{@code
     * <dependency>
     *     <groupId>io.agentscope</groupId>
     *     <artifactId>agentscope-extensions-redis</artifactId>
     * </dependency>
     * <dependency>
     *     <groupId>redis.clients</groupId>
     *     <artifactId>jedis</artifactId>
     * </dependency>
     * }</pre>
     */
    @Bean
    @ConditionalOnClass(name = "io.agentscope.extensions.redis.RedisDistributedStore")
    public DistributedStore distributedStore() {
        AiProperties.Distributed.Redis redis = properties.getDistributed().getRedis();
        log.info("Creating Redis DistributedStore: uri={}", redis.getUri());

        try {
            // RedisDistributedStore.fromJedis(new JedisPooled(uri))
            Class<?> jedisPooledClass = Class.forName("redis.clients.jedis.JedisPooled");
            Object jedis = jedisPooledClass.getConstructor(String.class).newInstance(redis.getUri());

            Class<?> storeClass = Class.forName("io.agentscope.extensions.redis.RedisDistributedStore");
            Method fromJedis = storeClass.getMethod("fromJedis", jedis.getClass());
            DistributedStore store = (DistributedStore) fromJedis.invoke(null, jedis);
            log.info("Redis DistributedStore created successfully");
            return store;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to create Redis DistributedStore. "
                            + "Ensure agentscope-extensions-redis and jedis are on the classpath.", e);
        }
    }
}
