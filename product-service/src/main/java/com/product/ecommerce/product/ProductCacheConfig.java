package com.product.ecommerce.product;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.Assert;

@Configuration
@EnableCaching
public class ProductCacheConfig {
  @Bean
  CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
    JavaType productListType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, ProductResponse.class);
    RedisSerializer<Object> productSerializer = typedSerializer(objectMapper, objectMapper.constructType(ProductResponse.class));
    RedisSerializer<Object> productListSerializer = typedSerializer(objectMapper, productListType);

    RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(productSerializer))
        .disableCachingNullValues()
        .entryTtl(Duration.ofMinutes(5));

    return RedisCacheManager.builder(connectionFactory)
        .transactionAware()
        .cacheDefaults(defaults)
        .withCacheConfiguration("product-list", defaults
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(productListSerializer))
            .entryTtl(Duration.ofMinutes(1)))
        .withCacheConfiguration("product-by-id", defaults.entryTtl(Duration.ofMinutes(5)))
        .build();
  }

  private RedisSerializer<Object> typedSerializer(ObjectMapper objectMapper, JavaType valueType) {
    return new RedisSerializer<>() {
      @Override
      public byte[] serialize(Object value) throws SerializationException {
        if (value == null) {
          return null;
        }
        try {
          return objectMapper.writeValueAsBytes(value);
        } catch (IOException exception) {
          throw new SerializationException("Could not serialize product cache value", exception);
        }
      }

      @Override
      public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
          return null;
        }
        try {
          return objectMapper.readValue(bytes, valueType);
        } catch (IOException exception) {
          throw new SerializationException("Could not deserialize product cache value", exception);
        }
      }
    };
  }
}
