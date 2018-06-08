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

import java.text.DecimalFormat;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataManager 
{
	@Autowired
	private StringRedisTemplate template;
	
	@Autowired
	private Calculator calculator;

	private DecimalFormat formatter = new DecimalFormat("#0.00");

	public DataManager(StringRedisTemplate template, Calculator calculator)
	{
		this.template = template;
		this.calculator = calculator;
	}
	
	/**
	 * Save the data in redis.
	 * @param data Data to save
	 * @return {@link DeviceCommand} if the status changed, null otherwise
	 */
	public DeviceCommand saveData(String data)
	{
		DeviceCommand result = null;
		
		JSONObject obj = new JSONObject(data);
		String deviceId = obj.getString("deviceId");
		double windSpeed = obj.getDouble("windSpeed");
		String messageType = obj.getString("type");
		
		String listKey = "List:" + deviceId;
		String deviceKey = "Device:" + deviceId;
		
		BoundListOperations<String, String> listOps = template.boundListOps(listKey);
		
		if ("clear".equals(messageType))
		{
			System.out.println("Clearing list of values");
			listOps.trim(0, 0);
			listOps.rightPop();
		}
		
		long listSize = listOps.size();
		if (listSize >= 10)
		{
			System.out.println("Removing " + listOps.rightPop());
		}

		listOps.leftPush(Double.toString(windSpeed));
		
		List<String> speeds = listOps.range(0, -1);
		
		double average = calculator.average(speeds);
		System.out.println("average: " + formatter.format(average) + " for " + speeds.size() + " values");
		
		BoundHashOperations<String, String, String> hashOps = template.boundHashOps(deviceKey);
		String oldStatus = hashOps.get("status");
		
		if ("clear".equals(messageType))
		{
			System.out.println("Clearing status");
			oldStatus = null;
		}

		String newStatus = calculator.computeStatus(average);
		if (! newStatus.equals(oldStatus))
		{
			System.out.println("Status Changed, sending Command.  Old status = " + oldStatus + ", newStatus = " + newStatus);
			result = new DeviceCommand(deviceId, newStatus, average);
		}
		
		hashOps.put("currentSpeed", Double.toString(windSpeed));
		hashOps.put("averageSpeed", Double.toString(average));
		hashOps.put("status", newStatus);
		
		return result;
	}

}
