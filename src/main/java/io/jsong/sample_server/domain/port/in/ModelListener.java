package io.jsong.sample_server.domain.port.in;
/*
 * 
 */

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ModelListener <String>{
  void consume(ConsumerRecord<String, String> record);
}
