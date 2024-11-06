package io.jsong.sample_server.repositories;
/*
 *
 */

import io.jsong.sample_server.DemoSolvingService;
import io.jsong.sample_server.KafkaListenerAdmin;
import io.jsong.sample_server.SampleServerApplication;
import io.jsong.sample_server.config.EngineConfig;
import io.jsong.sample_server.domain.port.in.ModelListener;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class KafkaModelKedaConsumer {

  @Autowired
  private KafkaListenerAdmin kafkaListenerAdmin;

  @Autowired
  private KafkaAdmin kafkaAdmin;

  @Autowired
  private DemoSolvingService demoSolvingService;

  private Duration STIMULATE_SOLVING_TIME = Duration.ofSeconds(20);

  private List<ModelListener> listeners = new ArrayList<>();

  public String CONSUMER_GROUP_ID = "keda-consumer-" + EngineConfig.engineId;

  ThreadLocal<List<String>> threadLocalValue = ThreadLocal.withInitial(ArrayList::new);

  private static ConcurrentHashMap<String, String> stateMap = new ConcurrentHashMap<>();

  @Autowired
  @Qualifier("kafkaResultTemplate")
  private KafkaTemplate<String, String> kafkaResultTemplate;


  @KafkaListener(id = "keda-modelid-consumer-1", topics = "${KAFKA_MODEL_METRIC_TOPIC:keda-modelid}", groupId = "${CONSUMER_GROUP_ID:keda-modelid-consumer}")
  public void consumeModelIdKedaRecord(ConsumerRecord<String, String> record, Acknowledgment ack)
      throws InterruptedException {

    long threadId = Thread.currentThread().getId();
    var key = record.key();
    var partition = record.partition();
    var offset = record.offset();
    var createdTime = record.timestamp();

    log.info("Create msg {}", convertTime(createdTime));
    log.info("Received at {}", Instant.now().toString());


    //Processing
    log.info(
        "[KEDA {} : Partition {} Offset {} : in thread {}] DONE  Consumer receive model id: {} in {} s",
        EngineConfig.engineId, partition, offset, threadId, record.key(),
        Duration.between(Instant.ofEpochMilli(createdTime), Instant.now()).toSeconds());
    ack.acknowledge();

    //Stop listening
    kafkaListenerAdmin.setContext();
    kafkaListenerAdmin.stopListener("keda-modelid-consumer-1");
//    kafkaListenerAdmin.stopAllListener();

    long timestamp = record.timestamp();
    var instant = Instant.ofEpochMilli(timestamp);
    sendResult((String) record.value(),
        " model id solved in " + EngineConfig.engineId
            + " : " + Thread.currentThread().getId()
            + " Time: " + Duration.between(instant,
            Instant.now()).toSeconds() + "S"
    );

    //Create new topic
    var modelIdTopic = "keda-modelId-" + key;
    createModelIdTopic(modelIdTopic);

    //Stimualte send msg
    sendModelMsg(key, "Testing");

    //Create a new consumer and listen to it
    kafkaListenerAdmin.createModelConsumer("keda-model-consumer-" + key, modelIdTopic,
        "keda-model-consumer-" + key);

    //Stimulate processing and completion
    Thread newThread = new Thread(() -> demoSolvingService.processAndDie(key));
    newThread.start();

    log.info("XXXXXXXXXXXXXXXX");
  }


  public void subscribe(ModelListener listener) {
    this.listeners.add(listener);
  }


  private void processing(ConsumerRecord record, RecordProcessingSignal signal,
      boolean collisionHappen) {
    //Processing

    sendResult((String) record.key(),
        "Start solving in " + EngineConfig.engineId
            + " : " + Thread.currentThread().getId()
            + " Time: " + Instant.now().toString()
            + " collision: " + collisionHappen
    );

    long timestamp = record.timestamp();
    var instant = Instant.ofEpochMilli(timestamp);
    notifyListener(record);

    if (signal != null) {
      signal.signalDone();
    }
    log.info("[KEDA - {}: in thread {}] DONE Key: {} acknowledged", EngineConfig.engineId,
        Thread.currentThread().getId(), record.key());

    sendResult((String) record.key(),
        "Solved in " + EngineConfig.engineId
            + " : " + Thread.currentThread().getId()
            + " Time: " + Duration.between(instant,
            Instant.now()).toSeconds() + "S"
            + " collision: " + collisionHappen
    );
  }

  private void notifyListener(ConsumerRecord<String, String> record) {
    //Stimulate processing
    try {
      log.info("[KEDA - {}: in thread {}] Sleeping for {} s ...", EngineConfig.engineId,
          Thread.currentThread().getId(),
          STIMULATE_SOLVING_TIME.toSeconds());
      Thread.sleep(STIMULATE_SOLVING_TIME.toMillis());


    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void sendResult(String modelId, String result) {

    try {
      kafkaResultTemplate.send("keda-result", modelId, result);
    } catch (Exception e) {
      log.error("Error in Sending Kafka Message for for Topic: {}, ModelId : {}, ex: {}",
          "keda-result", modelId, e.getMessage());
    }
  }

  public void sendModelMsg(String modelId, String result) {

    try {
      kafkaResultTemplate.send("keda-modelId-" + modelId, modelId, result);
    } catch (Exception e) {
      log.error("Error in Sending Kafka Message for for Topic: {}, ModelId : {}, ex: {}",
          "keda-result", modelId, e.getMessage());
    }
  }


  public void createModelIdTopic(String topic) {

    var client = AdminClient.create(kafkaAdmin.getConfigurationProperties());
    try {
      var modelTopic = new NewTopic(topic, 1, (short) 1);
      client.createTopics(Collections.singleton(modelTopic)).all().get();

    } catch (Exception e) {
      log.error("Error in Sending Kafka Message for for Topic: {}, ModelId : {}, ex: {}",
          "keda-result", topic, e.getMessage());

    } finally {
      client.close();
    }
  }


  public void closeModelIdTopic(String modelId) {

    var client = AdminClient.create(kafkaAdmin.getConfigurationProperties());
    try {
      client.deleteTopics(Collections.singleton("keda-modelId-" + modelId)).all().get();
    } catch (Exception e) {
      log.error("Error in Sending Kafka Message for for Topic: {}, ModelId : {}, ex: {}",
          "keda-result", modelId, e.getMessage());

    } finally {
      client.close();
    }
  }

  private String convertTime(long time){
    Date date = new Date(time);
    Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
    return format.format(date);
  }

}
