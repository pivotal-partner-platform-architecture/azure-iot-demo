package io.pivotal.azureiot.device;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("azureiot")
public class AzureIotHubDeviceProperties {

	@Value("${hostname}")
	private String hostname;

	@Value("${device.id}")
	private String deviceId;

	@Value("${shared.access.key}")
	private String sharedAccessKey;

	public String buildConnectionString()
	{
		String connectString = "HostName=" + getHostname() + ";DeviceId="
				+ getDeviceId() + ";SharedAccessKey=" + getSharedAccessKey();
		return connectString;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getSharedAccessKey() {
		return sharedAccessKey;
	}

	public void setSharedAccessKey(String sharedAccessKey) {
		this.sharedAccessKey = sharedAccessKey;
	}
}
