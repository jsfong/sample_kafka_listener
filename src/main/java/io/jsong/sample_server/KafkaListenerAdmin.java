package io.jsong.sample_server;
/*
 * 
 */

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.network.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class KafkaListenerAdmin {

  private ConfigurableApplicationContext context;


  @Autowired
  KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  @PostConstruct
  public void setContext(){
    context = SampleServerApplication.getContext();
  }

  public boolean startListener(String listenerId) {
    MessageListenerContainer listenerContainer =
        kafkaListenerEndpointRegistry.getListenerContainer(listenerId);
    assert listenerContainer != null : false;
    listenerContainer.start();
    log.info("{} Kafka Listener Started", listenerId);
    return true;
  }

  public boolean stopListener(String listenerId) {
    MessageListenerContainer listenerContainer =
        kafkaListenerEndpointRegistry.getListenerContainer(listenerId);

    assert listenerContainer != null : false;
    listenerContainer.stop();
    log.info("{} Kafka Listener Stopped.", listenerId);
    return true;
  }

  public boolean stopAllListener() {
    var listenerContainers =
        kafkaListenerEndpointRegistry.getAllListenerContainers();

    listenerContainers.forEach(c -> c.stop());

    log.info("{} All Kafka Listener Stopped.");
    return true;
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  ModelConsumer modelConsumer(String id, String topic, String groupId) {
    return new ModelConsumer(id, topic, groupId);
  }

  public ModelConsumer createModelConsumer(String id, String topic, String groupId) {
    return context.getBean(ModelConsumer.class, id, topic, groupId);
  }


}
