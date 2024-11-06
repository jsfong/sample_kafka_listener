package io.jsong.sample_server.config;
/*
 * 
 */

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaResultProducerConfig {

  @Value("${BOOT_STRAP_SERVERS:kafka.playground.svc.cluster.local:9092}")
  private String bootstrapAddress;

  @Value("${KAFKA_METRICS_TOPIC:keda-result}")
  private String kafkaResultTopic;

  @Value("${KAFKA_MODEL_PARTITION:1}")
  private Integer kafkaModelPartition;

  @Value("${KAFKA_MAX_MESSAGE_BYTES:52428800}")
  private Integer kafkaMaxMessageBytes;

  private String USERNAME ="user1";
  
  @Value(value = "${KAFKA_PASSWORD:DnPRAkDYVl}")
  private String PWD = "DnPRAkDYVl";

  @Value(value = "${USING_SASL:true}")
  public boolean USING_SASL;

  @Bean
  public ProducerFactory<String, String> resultProducerFactory() {
    Map<String, Object> configProps = new HashMap<>();

    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaMaxMessageBytes);
    //Auth
    if (USING_SASL) {
      configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
      configProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
      configProps.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
          "%s required username=\"%s\" " + "password=\"%s\";", PlainLoginModule.class.getName(),
          USERNAME, PWD
      ));
    } else {
      configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
    }
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean(name="kafkaResultTemplate")
  public KafkaTemplate<String, String> kafkaMetricTemplate() {
    return new KafkaTemplate<String, String>(resultProducerFactory());
  }


  @Bean
  public NewTopic createResultTopic() {
    return TopicBuilder.name(kafkaResultTopic)
        .partitions(kafkaModelPartition)
        .replicas(1)
        .config(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, kafkaMaxMessageBytes.toString())
        .build();
  }

}
