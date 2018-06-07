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
	
	@RequestMapping("/reset")
	public void reset() 
	{
		device.reset();
	}
}
