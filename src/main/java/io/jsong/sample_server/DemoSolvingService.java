package io.jsong.sample_server;
/*
 * 
 */

import io.jsong.sample_server.config.EngineConfig;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DemoSolvingService {

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;



  private Duration STIMULATE_SOLVING_TIME = Duration.ofSeconds(10);

  public void processAndDie(String key){
    //Stay alive , create new topic and, listen to new topic
    try {
      log.info("[KEDA - {}: in thread {}] Sleeping for {} s ...", EngineConfig.engineId,
          Thread.currentThread().getId(),
          STIMULATE_SOLVING_TIME.toSeconds());
      Thread.sleep(STIMULATE_SOLVING_TIME.toMillis());

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    log.info("[KEDA - {}: in thread {}] Wakeup and suicide after {} s ...", EngineConfig.engineId,
        Thread.currentThread().getId(),
        STIMULATE_SOLVING_TIME.toSeconds());

    redisTemplate.opsForValue().setIfAbsent(key, key);
    redisTemplate.expire(key, 5, TimeUnit.MINUTES);

    //Suicide after done
    SampleServerApplication.getContext().close();
  }


}
