package io.pivotal.azureiot.source;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.ServiceBusException;

@EnableBinding(Source.class)
@EnableConfigurationProperties({ AzureIotHubSourceProperties.class })
public class AzureIotHubSource {

	private Queue<String> messageQueue = new ConcurrentLinkedQueue<String>();
	private List<EventHubClient> clientList = new ArrayList<EventHubClient>();

	@Autowired
	private AzureIotHubSourceProperties config;

	@PostConstruct
	public void startup() {
		System.out.println("startup");

		String connectString = "Endpoint=" + config.getHubendpoint()
				+ ";EntityPath=" + config.getHubname()
				+ ";SharedAccessKeyName=" + config.getHubkeyname()
				+ ";SharedAccessKey=" + config.getHubkey();

		System.out.println("connectString = " + connectString);

		for (int i=0; i<config.getPartitions(); i++)
		{
			final EventHubClient client = receiveMessages(Integer.toString(i), connectString);
			clientList.add(client);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("shutting down...");
				for (int i=0; i< clientList.size(); i++)
				{
					EventHubClient client = clientList.get(i);
					if (client != null) {
						try {
							client.closeSync();
						} catch (ServiceBusException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println("shutdown complete");
			}
		});
	}

	/**
	 * Default poller will be called once per second.
	 */
	@InboundChannelAdapter(Source.OUTPUT)
	public String sendData() {
		return messageQueue.poll();
	}

	private EventHubClient receiveMessages(final String partitionId,
			final String connStr) {
		EventHubClient client = null;
		try {
			client = EventHubClient.createFromConnectionStringSync(connStr);
		} catch (Exception e) {
			System.out.println("Failed to create client: " + e.getMessage());
			System.exit(1);
		}
		try {
			client.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
					partitionId, Instant.now()).thenAccept(
					new Consumer<PartitionReceiver>() {
						public void accept(PartitionReceiver receiver) {
							System.out
									.println("** Created receiver on partition "
											+ partitionId);
							try {
								while (true) {
									Iterable<EventData> receivedEvents = receiver
											.receive(100).get();
									int batchSize = 0;
									if (receivedEvents != null) {
										for (EventData receivedEvent : receivedEvents) {
											System.out.println(String
													.format("Offset: %s, SeqNo: %s, EnqueueTime: %s",
															receivedEvent
																	.getSystemProperties()
																	.getOffset(),
															receivedEvent
																	.getSystemProperties()
																	.getSequenceNumber(),
															receivedEvent
																	.getSystemProperties()
																	.getEnqueuedTime()));

											messageQueue.add(new String(
													receivedEvent.getBody(),
													Charset.defaultCharset()));

											System.out.println(String.format(
													"| Message Payload: %s",
													new String(receivedEvent
															.getBody(), Charset
															.defaultCharset())));
											batchSize++;
										}
									}
									System.out.println(String
											.format("Partition: %s, ReceivedBatch Size: %s",
													partitionId, batchSize));
								}
							} catch (Exception e) {
								System.out
										.println("Failed to receive messages: "
												+ e.getMessage());
							}
						}
					});
		} catch (Exception e) {
			System.out.println("Failed to create receiver: " + e.getMessage());
		}
		return client;
	}
}
