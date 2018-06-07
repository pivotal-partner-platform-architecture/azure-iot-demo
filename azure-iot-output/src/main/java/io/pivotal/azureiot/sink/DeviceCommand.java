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

public class DeviceCommand 
{
	private String deviceId;
	
	@SuppressWarnings("unused")
	private String status;
	
	@SuppressWarnings("unused")
	private double average;
	
	public DeviceCommand(String deviceId, String status, double average)
	{
		this.deviceId = deviceId;
		this.status = status;
		this.average = average;
	}

	public String getDeviceId() {
		return deviceId;
	}
	
}
