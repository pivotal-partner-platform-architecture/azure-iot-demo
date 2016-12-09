package io.pivotal.azureiot.device;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AzureIotDeviceApplication {

	public static void main(String[] args) throws IOException,
			URISyntaxException {
		SpringApplication.run(AzureIotDeviceApplication.class, args);
	}

}
