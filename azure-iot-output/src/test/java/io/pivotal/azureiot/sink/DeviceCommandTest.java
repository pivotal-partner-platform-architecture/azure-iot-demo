package io.pivotal.azureiot.sink;

import org.junit.Assert;
import org.junit.Test;

public class DeviceCommandTest {

	@Test
	public void testCreate() 
	{
		DeviceCommand command = new DeviceCommand(null, null, 0.0d);
		Assert.assertNotNull(command);
		Assert.assertNull(command.getDeviceId());
		
		command = new DeviceCommand("deviceId", "status", 0.0d);
		Assert.assertNotNull(command);
		Assert.assertNotNull(command.getDeviceId());
		Assert.assertEquals("deviceId", command.getDeviceId());
	}

}
