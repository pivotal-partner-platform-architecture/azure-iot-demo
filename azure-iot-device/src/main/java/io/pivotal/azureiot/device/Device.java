/*
  * *****************************************************************************
  *     Copyright (c) 2017-Present Pivotal Software, Inc. All rights reserved. 
  *	Licensed under the Apache License, Version 2.0 (the "License");
  * 	you may not use this file except in compliance with the License.
  *	You may obtain a copy of the License at 
  *   
  *	http://www.apache.org/licenses/LICENSE-2.0
  *
  *	Unless required by applicable law or agreed to in writing, software 
  *	distributed under the License is distributed on an "AS IS" BASIS, 
  *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *	See the License for the specific language governing permissions and limitations under the License.
  * *****************************************************************************
*/

package io.pivotal.azureiot.device;

import io.pivotal.azureiot.autoconfigure.AzureIotAutoConfiguration.DeviceClientFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubMessageResult;
import com.microsoft.azure.iothub.Message;
import com.microsoft.azure.iothub.MessageCallback;

@Component
@EnableConfigurationProperties({ AzureIotHubDeviceProperties.class })
public class Device {
	
	@Autowired
	private DeviceClientFactory clientFactory;

	@Autowired
	private IotHubEventCallback messageSenderCallback;

	@Autowired
	private AzureIotHubDeviceProperties properties;

	private boolean paused = true;
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	private MessageSender sender = new MessageSender();

	private 	DeviceClient client;

	private DeviceStatus status = new DeviceStatus();
	
	private String dataPointType = "update";

	@PostConstruct
	public void startup() throws IOException, URISyntaxException {
		String connString = properties.buildConnectionString();
		client = clientFactory.createDeviceClient(connString);
		client.open();
		executor.execute(sender);

		CommandCallback receiver = new CommandCallback();

		Object context = null;
		client.setMessageCallback(receiver, context);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("shutting down...");
				executor.shutdown();
				if (client != null) {
					try {
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("shutdown complete");
			}
		});
	}

	public void start() {
		System.out.println("starting output...");
		paused = false;
	}

	public void stop() {
		System.out.println("stopping output...");
		paused = true;
	}

	public void kill() throws IOException {
		System.out.println("killing...");
		System.exit(1);
	}

	public void plus() {
		status.setCurrent(status.getCurrent() + 1.0);
	}

	public void minus() {
		status.setCurrent(status.getCurrent() - 1.0);
	}

	public String status() {
		String result = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.writeValueAsString(status);
			System.out.println("status: " + result);
		} catch (JsonProcessingException e) {
			System.err.println("Error writing json status");
			e.printStackTrace();
		}
		return result;
	}

	public void reset() {
		System.out.println("reset called");
		dataPointType = "clear";
	}

	private class TelemetryDataPoint {
		@SuppressWarnings("unused")
		public String deviceId;
		
		@SuppressWarnings("unused")
		public double windSpeed;

		@SuppressWarnings("unused")
		public String type;

		public String serialize() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}

	private class MessageSender implements Runnable {
		public volatile boolean stopThread = false;

		public void run() {
			try {
				System.out.println("starting MessageSender.........");
				status.setCurrent(20.0);
				Random rand = new Random();
				
				System.out.println("stopThread = " + stopThread);
				System.out.println("paused = " + paused);

				while (!stopThread) {
					if (!paused) {
						System.out.println("calculating......");
						status.setCurrent(calculateWindSpeed(rand, status.getCurrent()));
						TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
						telemetryDataPoint.deviceId = properties.getDeviceId();
						telemetryDataPoint.windSpeed = status.getCurrent();
						telemetryDataPoint.type = dataPointType;

						String msgStr = telemetryDataPoint.serialize();
						Message msg = new Message(msgStr);
						System.out.println("Sending: " + msgStr);

						Object lockobj = new Object();
						client.sendEventAsync(msg, messageSenderCallback, lockobj);

						synchronized (lockobj) {
							lockobj.wait();
						}
						dataPointType = "update";
					}
					Thread.sleep(5000);
					System.out.println("after sleep");
				}
			} catch (InterruptedException e) {
				System.out.println("Finished." + e.getMessage());
				e.printStackTrace();
			}
			System.out.println("Finished.");
		}

		private double calculateWindSpeed(Random rand, double averageSpeed) {
			double currentWindSpeed = averageSpeed + rand.nextDouble() * 6 - 3;
			currentWindSpeed = Double.min(currentWindSpeed, 49.0);
			currentWindSpeed = Double.max(currentWindSpeed, 1.0);
			return currentWindSpeed;
		}
	}

	private class CommandCallback implements MessageCallback {

		public IotHubMessageResult execute(Message msg, Object context) {
			String deviceCommand = new String(msg.getBytes());
			System.out.println("Device received command: " + deviceCommand);

			try {
				JSONObject obj = new JSONObject(deviceCommand);
				double averageSpeed = obj.getDouble("average");
				String statusColor = obj.getString("status");

				status.setAverage(averageSpeed);
				status.setStatus(statusColor);
			} catch (JSONException e) {
				System.err.println("Error reading json string " + deviceCommand);
				e.printStackTrace();
			}

			return IotHubMessageResult.COMPLETE;
		}
	}

	private class DeviceStatus {
		private String status;
		private double average;
		private double current;

		public DeviceStatus() {
			status = "unknown";
			average = 0.0;
			current = 0.0;
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

		public double getCurrent() {
			return current;
		}

		public void setCurrent(double current) {
			this.current = current;
		}
	}

}
