package io.pivotal.azureiot.autoconfigure;

import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubStatusCode;

@Configuration
@EnableConfigurationProperties(AzureIotProperties.class)
public class AzureIotAutoConfiguration {

	@Autowired
	private AzureIotProperties properties;
	
	private static final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
	
	@Bean
	@ConditionalOnMissingBean(DeviceClient.class)
	public DeviceClient deviceClientFactory()
	{
		String connString = "HostName=" + properties.getHostname() + ";DeviceId="
				+ properties.getDeviceId() + ";SharedAccessKey=" + properties.getSharedAccessKey();
		System.out.println("connString = " + connString);
		try {
			DeviceClient client = new DeviceClient(connString, protocol);
			return client;
		} catch (URISyntaxException e) {
			System.err.println("Error creating DeviceClient" + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	@Bean
	@ConditionalOnMissingBean(IotHubEventCallback.class)
	public IotHubEventCallback eventCallbackFactory()
	{
		return new EventCallback();
	}
	
	private class EventCallback implements IotHubEventCallback {
		public void execute(IotHubStatusCode status, Object context) {
			System.out.println("IoT Hub responded to message with status: "
					+ status.name());
			if (context != null) {
				synchronized (context) {
					context.notify();
				}
			}
		}
	}
	
}
