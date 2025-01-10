package com.akgarg.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Profile({"prod", "PROD"})
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(final Environment environment) {
        final var configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(environment.getProperty("spring.data.redis.host", "localhost"));
        configuration.setPort(Integer.parseInt(environment.getProperty("spring.data.redis.port", "6379")));
        configuration.setDatabase(Integer.parseInt(environment.getProperty("spring.data.redis.database", "6")));
        configuration.setPassword(environment.getProperty("spring.data.redis.password", ""));
        return new JedisConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(final RedisConnectionFactory redisConnectionFactory) {
        final var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

}
