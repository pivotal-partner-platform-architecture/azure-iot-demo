package io.pivotal.azureiot.create;

import com.microsoft.azure.iot.service.exceptions.IotHubException;
import com.microsoft.azure.iot.service.sdk.Device;
import com.microsoft.azure.iot.service.sdk.RegistryManager;

public class AddDevice {

	public static void main(String[] args) 
	{
		if (args.length < 2)
		{
			System.err.println("Usage: java -jar xxx.jar CONNECTION_STRING DEVICE_ID");
			System.exit(1);
		}
		
		AddDevice program = new AddDevice();
		program.addDevice(args[0], args[1]);
	}
	
	private void addDevice(String conn, String id) 
	{
		try {
			RegistryManager registryManager = RegistryManager.createFromConnectionString(conn);
			Device device = Device.createFromId(id, null, null);
			try {
				device = registryManager.addDevice(device);
				System.out.println("\nDevice added succesfully!");
			} catch (IotHubException iote) {
				try {
					device = registryManager.getDevice(id);
					System.out.println("\nDevice already exists!");
				} catch (IotHubException iotf) {
					iotf.printStackTrace();
				}
			}
			System.out.println("\nDevice id: " + device.getDeviceId());
			System.out.println("\nDevice key: " + device.getPrimaryKey());
		} catch (Exception e) {
			System.err.println("Error adding device. " + e.getMessage());
			e.printStackTrace();
		}
	}
}
