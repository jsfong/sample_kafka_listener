package io.jsong.sample_server.api;
/*
 * 
 */

import io.jsong.sample_server.config.EngineConfig;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping({"/model"})
public class ModelApi {


  @GetMapping(value = "/{modelId}")
  public String getModel(@PathVariable String modelId) {

    var msg = "Running in model " + modelId + " in engine id: " + EngineConfig.engineId;
    log.info(msg);
    return msg;
  }


}
