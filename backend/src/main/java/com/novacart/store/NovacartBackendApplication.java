package com.novacart.store;

import com.novacart.store.config.MediaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MediaProperties.class)
public class NovacartBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(NovacartBackendApplication.class, args);
	}

}
