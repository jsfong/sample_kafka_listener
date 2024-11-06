package io.jsong.sample_server;

import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SampleServerApplication {

	@Getter
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(SampleServerApplication.class, args);
	}

}
