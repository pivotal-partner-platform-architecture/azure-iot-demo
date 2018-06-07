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

package io.pivotal.azureiot.sink;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import com.microsoft.azure.iot.service.sdk.DeliveryAcknowledgement;
import com.microsoft.azure.iot.service.sdk.FeedbackBatch;
import com.microsoft.azure.iot.service.sdk.FeedbackReceiver;
import com.microsoft.azure.iot.service.sdk.IotHubServiceClientProtocol;
import com.microsoft.azure.iot.service.sdk.Message;
import com.microsoft.azure.iot.service.sdk.ServiceClient;

@EnableBinding(Sink.class)
@EnableConfigurationProperties({ AzureIotOutputSourceProperties.class })
public class AzureIotOutputSource 
{

	private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;

	private ServiceClient client;
	
	@Autowired
	private AzureIotOutputSourceProperties config;
	
	@Autowired
	private DataManager dataManager;
	
	@PostConstruct
	public void startup() {
		System.out.println("startup");
		
		String connectString = "HostName=" + config.getHostname()
				+ ";SharedAccessKeyName=" + config.getHubkeyname()
				+ ";SharedAccessKey=" + config.getHubkey();

		System.out.println("connectString = " + connectString);

		client = createClient(connectString);
	}
	
	@StreamListener(Sink.INPUT)
	public void process(String data) {
		System.out.println("Received: " + data);
		DeviceCommand cmd = dataManager.saveData(data);
		if (cmd != null)
		{
			sendCommandToDevice(cmd);
		}
	}
	
	private ServiceClient createClient(final String connStr) {
		ServiceClient client = null;
		try {
			client = ServiceClient
					.createFromConnectionString(connStr, protocol);
		} catch (Exception e) {
			System.err.println("Failed to create client: " + e.getMessage());
			System.exit(1);
		}
		return client;
	}
	
	private void sendCommandToDevice(DeviceCommand command) {

		try {
			client.open();
			FeedbackReceiver feedbackReceiver = client
					.getFeedbackReceiver(command.getDeviceId());
			
			if (feedbackReceiver != null) {
				feedbackReceiver.open();
			}

			ObjectMapper mapper = new ObjectMapper();
			String jsonString = mapper.writeValueAsString(command);
			System.out.println("Sending command to device: " + jsonString);
			
			Message messageToSend = new Message(jsonString);
			messageToSend
					.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);

			client.send(command.getDeviceId(), messageToSend);
			System.out.println("Command sent to device");

			FeedbackBatch feedbackBatch = feedbackReceiver.receive(10000);
			if (feedbackBatch != null) {
				System.out.println("Command feedback received, feedback time: "
						+ feedbackBatch.getEnqueuedTimeUtc().toString());
			}
			else
			{
				System.out.println("feedbackBatch was null.");
			}

			if (feedbackReceiver != null)
			{
				feedbackReceiver.close();
			}
			client.close();
			
		} catch (IOException | InterruptedException e) {
			System.err.println("Error sending command to device");
			e.printStackTrace();
		}
	}
	
}
