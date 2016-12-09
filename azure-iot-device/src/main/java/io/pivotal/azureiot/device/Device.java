package io.pivotal.azureiot.device;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubMessageResult;
import com.microsoft.azure.iothub.IotHubStatusCode;
import com.microsoft.azure.iothub.Message;
import com.microsoft.azure.iothub.MessageCallback;

@Component
public class Device {
	@Value("${hostname}")
	private String hostname;

	@Value("${device.id}")
	private String deviceId;

	@Value("${shared.access.key}")
	private String sharedAccessKey;

	private static final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
	private DeviceClient client;
	private boolean paused = true;
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	private MessageSender sender = new MessageSender();

	private DeviceStatus status = new DeviceStatus();

	@PostConstruct
	public void startup() throws IOException, URISyntaxException {
		String connString = "HostName=" + hostname + ";DeviceId=" + deviceId
				+ ";SharedAccessKey=" + sharedAccessKey;
		client = new DeviceClient(connString, protocol);
		client.open();
		executor.execute(sender);

		MessageCallbackImpl receiver = new MessageCallbackImpl();

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

	private class TelemetryDataPoint {
		@SuppressWarnings("unused")
		public String deviceId;
		@SuppressWarnings("unused")
		public double windSpeed;

		public String serialize() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}

	private class EventCallback implements IotHubEventCallback {
		public void execute(IotHubStatusCode status, Object context) {
			if (! IotHubStatusCode.OK_EMPTY.equals(status))
			{
				System.out.println("IoT Hub responded to message with status: "	+ status.name());
			}

			if (context != null) {
				synchronized (context) {
					context.notify();
				}
			}
		}
	}

	private class MessageSender implements Runnable {
		public volatile boolean stopThread = false;

		public void run() {
			try {
				status.setCurrent(20.0);
				Random rand = new Random();

				while (!stopThread) {
					if (!paused) {
						status.setCurrent(calculateWindSpeed(rand, status.getCurrent()));
						TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
						telemetryDataPoint.deviceId = deviceId;
						telemetryDataPoint.windSpeed = status.getCurrent();

						String msgStr = telemetryDataPoint.serialize();
						Message msg = new Message(msgStr);
						System.out.println("Sending: " + msgStr);

						Object lockobj = new Object();
						EventCallback callback = new EventCallback();
						client.sendEventAsync(msg, callback, lockobj);

						synchronized (lockobj) {
							lockobj.wait();
						}

					}
					Thread.sleep(5000);
				}
			} catch (InterruptedException e) {
				System.out.println("Finished.");
			}
		}

		private double calculateWindSpeed(Random rand, double averageSpeed) {
			double currentWindSpeed = averageSpeed + rand.nextDouble() * 6 - 3;
			currentWindSpeed = Double.min(currentWindSpeed, 49.0);
			currentWindSpeed = Double.max(currentWindSpeed, 1.0);
			return currentWindSpeed;
		}
	}

	private class MessageCallbackImpl implements MessageCallback {

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
