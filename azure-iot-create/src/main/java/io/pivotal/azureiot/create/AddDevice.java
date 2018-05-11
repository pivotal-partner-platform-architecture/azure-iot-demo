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
