package io.pivotal.azureiot.sink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AzureIotOutputApplication {

	public static void main(String[] args) {
		SpringApplication.run(AzureIotOutputApplication.class, args);
	}
}
