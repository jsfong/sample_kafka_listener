package io.jsong.sample_server.config;
/*
 * 
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

@Slf4j
public class KafkaErrorHandler implements CommonErrorHandler {

  @Override
  public void handleOtherException(Exception exception, Consumer<?, ?> consumer,
      MessageListenerContainer container, boolean batchListener) {

    if (exception instanceof RecordDeserializationException ex) {
      log.warn(
          "[KafkaErrorHandler] - Unable to deserialize record. Skipping record with offset {}",
          ex.offset());
      consumer.seek(ex.topicPartition(), ex.offset() + 1L);
      consumer.commitSync();

    } else {
      log.error("[KafkaErrorHandler] - Handle other exception. Exception not handled", exception);

    }
  }

  @Override
  public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record,
      Consumer<?, ?> consumer, MessageListenerContainer container) {
    log.error("[KafkaErrorHandler] - Handle one exception");
    return CommonErrorHandler.super.handleOne(thrownException, record, consumer, container);
  }
}
