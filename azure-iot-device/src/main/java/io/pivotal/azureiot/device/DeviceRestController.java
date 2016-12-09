package io.pivotal.azureiot.device;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeviceRestController {
	
	@Autowired
	private Device device;

	@RequestMapping("/start")
	public void start() {
		device.start();
	}

	@RequestMapping("/stop")
	public void stop() {
		device.stop();
	}

	@RequestMapping("/plus")
	public void plus() {
		device.plus();
	}
	
	@RequestMapping("/minus")
	public void minus() {
		device.minus();
	}

	@RequestMapping("/kill")
	public void kill() throws IOException {
		device.kill();
	}
	
	@RequestMapping("/status")
	public String status() 
	{
		return device.status();
	}
}
