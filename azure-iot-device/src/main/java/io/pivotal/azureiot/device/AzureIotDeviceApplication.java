package io.pivotal.azureiot.device;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class AzureIotDeviceApplication {

	public static void main(String[] args) throws IOException,
			URISyntaxException {
		SpringApplication.run(AzureIotDeviceApplication.class, args);
	}

}
