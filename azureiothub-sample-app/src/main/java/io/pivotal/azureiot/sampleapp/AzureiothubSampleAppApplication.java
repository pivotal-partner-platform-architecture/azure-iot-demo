package io.pivotal.azureiot.sampleapp;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.Message;

@SpringBootApplication
public class AzureiothubSampleAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AzureiothubSampleAppApplication.class, args);
	}
	
	@Autowired
	private DeviceClient client;

	@Autowired
	private IotHubEventCallback callback;

	@Service
	private class MessageSender implements CommandLineRunner {

		@Override
		public void run(String... arg0) throws Exception {
			System.out.println("run...");
			process("Hello World");
		}

		public void process(String string) throws URISyntaxException,
				IOException, InterruptedException {
			client.open();
			Object lockobj = new Object();
			Message msg = new Message(string);
			System.out.println("Sending message: " + string);
			client.sendEventAsync(msg, callback, lockobj);
			synchronized (lockobj) {
				lockobj.wait();
			}
			System.out.println("process complete.");
		}

	}
}
