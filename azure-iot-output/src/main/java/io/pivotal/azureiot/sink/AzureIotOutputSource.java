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
import java.util.List;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.microsoft.azure.iot.service.sdk.DeliveryAcknowledgement;
import com.microsoft.azure.iot.service.sdk.FeedbackBatch;
import com.microsoft.azure.iot.service.sdk.FeedbackReceiver;
import com.microsoft.azure.iot.service.sdk.IotHubServiceClientProtocol;
import com.microsoft.azure.iot.service.sdk.Message;
import com.microsoft.azure.iot.service.sdk.ServiceClient;

@EnableBinding(Sink.class)
@EnableConfigurationProperties({ AzureIotOutputSourceProperties.class })
public class AzureIotOutputSource extends AbstractCloudConfig {

	private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;

	private ServiceClient client;

	@Autowired
	private AzureIotOutputSourceProperties config;
	
	@Autowired
	private StringRedisTemplate template;
	
	@PostConstruct
	public void startup() {
		System.out.println("startup");
		
		String connectString = "HostName=" + config.getHostname()
				+ ";SharedAccessKeyName=" + config.getHubkeyname()
				+ ";SharedAccessKey=" + config.getHubkey();

		System.out.println("connectString = " + connectString);

		client = createClient(connectString);
	}
	
	@Bean
	public RedisConnectionFactory redisFactory() {
	    return connectionFactory().redisConnectionFactory();
	}
	
	@StreamListener(Sink.INPUT)
	public void process(String data) {
		System.out.println("Received: " + data);
		DeviceCommand cmd = saveData(data);
		if (cmd != null)
		{
			sendCommandToDevice(cmd);
		}
	}

	/**
	 * Save the data in redis.
	 * @param data Data to save
	 * @return {@link DeviceCommand} if the status changed, null otherwise
	 */
	private DeviceCommand saveData(String data)
	{
		DeviceCommand result = null;
		
		JSONObject obj = new JSONObject(data);
		String deviceId = obj.getString("deviceId");
		double windSpeed = obj.getDouble("windSpeed");
		
		String listKey = "List:" + deviceId;
		String deviceKey = "Device:" + deviceId;
		
		BoundListOperations<String, String> listOps = template.boundListOps(listKey);
		long listSize = listOps.size();
		if (listSize >= 10)
		{
			System.out.println("Removing " + listOps.rightPop());
		}

		listOps.leftPush(Double.toString(windSpeed));
		
		List<String> speeds = listOps.range(0, -1);
		
		double sum = 0.0;
		for (String speed : speeds)
		{
			sum += Double.valueOf(speed);
		}
		double average = sum / speeds.size();
		System.out.println("average: " + average + " for " + speeds.size() + " values");
		
		BoundHashOperations<String, String, String> hashOps = template.boundHashOps(deviceKey);
		
		String oldStatus = hashOps.get("status");
		String newStatus = computeStatus(average);
		if (! newStatus.equals(oldStatus))
		{
			System.out.println("Status Changed, sending Command.  Old status = " + oldStatus + ", newStatus = " + newStatus);
			result = new DeviceCommand();
			result.setAverage(average);
			result.setStatus(newStatus);
			result.setDeviceId(deviceId);
		}
		
		hashOps.put("currentSpeed", Double.toString(windSpeed));
		hashOps.put("averageSpeed", Double.toString(average));
		hashOps.put("status", newStatus);
		
		return result;
	}
	
	private ServiceClient createClient(final String connStr) {
		ServiceClient client = null;
		try {
			client = ServiceClient
					.createFromConnectionString(connStr, protocol);
		} catch (Exception e) {
			System.out.println("Failed to create client: " + e.getMessage());
			System.exit(1);
		}
		return client;
	}
	
	private String computeStatus(double average)
	{
		String result = "green";
		if (average > 30.0)
		{
			result = "red";
		}
		else if (average < 10.0)
		{
			result = "yellow";
		}
		return result;
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

			FeedbackBatch feedbackBatch = feedbackReceiver.receive(10000);
			if (feedbackBatch != null) {
				System.out.println("Command feedback received, feedback time: "
						+ feedbackBatch.getEnqueuedTimeUtc().toString());
			}

			if (feedbackReceiver != null)
			{
				feedbackReceiver.close();
			}
			client.close();
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private class DeviceCommand
	{
		private String deviceId;
		private String status;
		private double average;
		
		public String getDeviceId() {
			return deviceId;
		}
		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}
		@SuppressWarnings("unused")
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		@SuppressWarnings("unused")
		public double getAverage() {
			return average;
		}
		public void setAverage(double average) {
			this.average = average;
		}
	}
}
