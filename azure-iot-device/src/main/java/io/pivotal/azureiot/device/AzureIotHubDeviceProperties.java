/*
  * *****************************************************************************
  *     Copyright (c) 2017-Present Pivotal Software, Inc. All rights reserved. 
  *	Licensed under the Apache License, Version 2.0 (the "License");
  * 	you may not use this file except in compliance with the License.
  *	You may obtain a copy of the License at 
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  *	Unless required by applicable law or agreed to in writing, software 
  *	distributed under the License is distributed on an "AS IS" BASIS, 
  *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *	See the License for the specific language governing permissions and limitations under the License.
  * *****************************************************************************
*/

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
