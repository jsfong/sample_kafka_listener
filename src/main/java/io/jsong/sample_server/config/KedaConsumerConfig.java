package io.jsong.sample_server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RangeAssignor;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@EnableKafka
@Configuration
public class KedaConsumerConfig {


  @Autowired
  private KafkaCustomProperties kafkaCustomProperties;

  @Value(value = "${BOOT_STRAP_SERVERS:kafka.playground.svc.cluster.local:9092}")
  public String BOOT_STRAP_SERVERS;

  @Value(value = "${USING_SASL:true}")
  public boolean USING_SASL;

  @Value(value = "${CONCURRENCY:1}")
  public Integer CONCURRENCY;

  private String USERNAME ="user1";

  @Value(value = "${KAFKA_PASSWORD:DnPRAkDYVl}")
  private String PWD = "DnPRAkDYVl";

  //JSON consumer
  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> config = new HashMap<>();
    var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOT_STRAP_SERVERS);
    config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false"); // Disable auto jackson deserializer using header type value
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); //latest is use for auto setting new partition commit to 0

    //Auth
    if (USING_SASL) {
      config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
      config.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
      config.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
          "%s required username=\"%s\" " + "password=\"%s\";", PlainLoginModule.class.getName(),
          USERNAME, PWD
      ));
    } else {
      config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
    }

    config.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RangeAssignor.class.getName());


    config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
    config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 60000);
//    config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

    var factory = new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(),
        new StringDeserializer());


    return factory;
  }



  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setCommonErrorHandler(kafkaCustomProperties.commonErrorHandler());
    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

    factory.setConcurrency(CONCURRENCY); //Control how many consumer listening


    return factory;
  }
  //TODO it is possible to delay ack when the listener already done
}
