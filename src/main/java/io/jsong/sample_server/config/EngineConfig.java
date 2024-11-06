package io.jsong.sample_server.config;
/*
 * 
 */

import java.util.UUID;

public class EngineConfig {
  public static String engineId;

  static {
    engineId = UUID.randomUUID().toString();
  }
}
