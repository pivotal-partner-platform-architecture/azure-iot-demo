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

package io.pivotal.azureiot.autoconfigure;

import java.net.URISyntaxException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubStatusCode;

@Configuration
public class AzureIotAutoConfiguration
{
	private static final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

	@Bean
	@ConditionalOnMissingBean(DeviceClientFactory.class)
	public DeviceClientFactory deviceClientFactory()
	{
		return new DeviceClientFactory();
	}

	public class DeviceClientFactory
	{
		public DeviceClient createDeviceClient(String connectionString)
		{
			System.out.println("connString = " + connectionString);
			try
			{
				DeviceClient client = new DeviceClient(connectionString, protocol);
				return client;
			} catch (URISyntaxException e)
			{
				System.err.println("Error creating DeviceClient" + e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
	}
	
	@Bean
	@ConditionalOnMissingBean(IotHubEventCallback.class)
	public IotHubEventCallback eventCallbackFactory()
	{
		return new EventCallback();
	}

	private class EventCallback implements IotHubEventCallback
	{
		public void execute(IotHubStatusCode status, Object context)
		{
			System.out.println("IoT Hub responded to message with status: " + status.name());
			if (context != null)
			{
				synchronized (context)
				{
					context.notify();
				}
			}
		}
	}
}
