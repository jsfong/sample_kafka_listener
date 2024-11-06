package io.jsong.sample_server.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@ConfigurationProperties(prefix = "kafka")
@Getter
@Setter
public class KafkaCustomProperties {

  @Value(value = "${BOOT_STRAP_SERVERS:kafka.playground.svc.cluster.local:9092}")
  private String BOOT_STRAP_SERVERS;

  @Value(value = "${USING_SASL:true}")
  public boolean USING_SASL;


  private String USERNAME = "user1";
  
  @Value(value = "${KAFKA_PASSWORD:DnPRAkDYVl}")
  private String PWD = "DnPRAkDYVl";

  private KafkaProperties.Security security = new KafkaProperties.Security();

  public Map<String, Object> buildCommonProperties() {
    Map<String, Object> properties = new HashMap<>();
    if (this.BOOT_STRAP_SERVERS != null) {
      properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, this.BOOT_STRAP_SERVERS);
    }
    properties.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    properties.put(JsonDeserializer.USE_TYPE_INFO_HEADERS,
        "false"); // Disable auto jackson deserializer using header type value
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    //Auth
    if (USING_SASL) {
      properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
      properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
      properties.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
          "%s required username=\"%s\" " + "password=\"%s\";", PlainLoginModule.class.getName(),
          USERNAME, PWD
      ));
    } else {
      properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
    }

    properties.putAll(this.security.buildProperties());

    return properties;
  }

  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOT_STRAP_SERVERS);

    if (USING_SASL) {
      configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
      configs.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
      configs.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
          "%s required username=\"%s\" " + "password=\"%s\";", PlainLoginModule.class.getName(),
          USERNAME, PWD
      ));

    } else {
      configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
    }

    return new KafkaAdmin(configs);
  }

  @Bean
  CommonErrorHandler commonErrorHandler() {
    return new KafkaErrorHandler();
  }
}
