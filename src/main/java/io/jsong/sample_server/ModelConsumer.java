package io.jsong.sample_server;
/*
 * 
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@AllArgsConstructor
@Getter
public class ModelConsumer {

  private final String id;
  private final String topic;
  private final String groupid;

  @KafkaListener(id = "#{__listener.id}", topics = "#{__listener.topic}", groupId = "#{__listener.groupid}")
  public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
    log.info("[ModelConsumer] - listen msg {}", record.value());
    ack.acknowledge();
  }
}
