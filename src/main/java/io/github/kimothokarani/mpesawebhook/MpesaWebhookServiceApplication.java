package io.github.kimothokarani.mpesawebhook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MpesaWebhookServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MpesaWebhookServiceApplication.class, args);
	}

}
