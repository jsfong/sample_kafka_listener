package io.jsong.sample_server.config;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
//Use redis template instead of redis spring data repo which is slower
//@EnableRedisRepositories(
//    keyspaceConfiguration = RedisConfig.RedisKeyspaceConfig.class,     // Enable setting of TTL per keyspace
//    enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP,            // Enable removal of index after TTL
//    keyspaceNotificationsConfigParameter = "")                         // Enable removal of index after TTL
public class RedisConfig {

  @Value("${REDIS_HOSTNAME:localhost}")
  private String REDIS_HOSTNAME;

  @Value("${REDIS_PORT:6379}")
  private int REDIS_PORT;

//  @Value("${MODEL_METRIC_KEYSPACE_TTL_SECOND:86400}")
//  public Long MODEL_METRIC_KEYSPACE_TTL_SECOND;

  @Bean
  @Primary
  LettuceConnectionFactory lettuceConnectionFactory() {
    var config = new RedisStandaloneConfiguration(REDIS_HOSTNAME, REDIS_PORT);
    var lettuceClientConfiguration = LettucePoolingClientConfiguration.builder()
        .poolConfig(genericObjectPoolConfig()).build();
    return new LettuceConnectionFactory(config, lettuceClientConfiguration);
  }

  @Bean
  public GenericObjectPoolConfig genericObjectPoolConfig() {
    var genericObjectPoolConfig = new GenericObjectPoolConfig();
//    //Setting redis pool
//    genericObjectPoolConfig.setMaxTotal(100);
    return genericObjectPoolConfig;
  }


  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    RedisSerializer<String> stringSerializer = new StringRedisSerializer();

    template.setConnectionFactory(lettuceConnectionFactory());

    template.setKeySerializer(stringSerializer);
    template.setHashKeySerializer(stringSerializer);

    template.setValueSerializer(stringSerializer);
    template.setHashValueSerializer(stringSerializer);


    template.setEnableTransactionSupport(true);
    template.afterPropertiesSet();
    return template;
  }

//  @Bean
//  public RedisMappingContext keyValueMappingContext() {
//    return new RedisMappingContext(new MappingConfiguration(new IndexConfiguration(), new RedisKeyspaceConfig()));
//  }
//
//  public class RedisKeyspaceConfig extends KeyspaceConfiguration {
//
//    @Override
//    protected Iterable<KeyspaceSettings> initialConfiguration() {
//      KeyspaceSettings keyspaceSettings = new KeyspaceSettings(ModelMetrics.class, "ModelMetrics");
//      keyspaceSettings.setTimeToLive(MODEL_METRIC_KEYSPACE_TTL_SECOND);
//      return Collections.singleton(keyspaceSettings);
//    }
//  }

}
