package io.pivotal.azureiot.sink;

import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DataManagerTest 
{
	@MockBean
	private StringRedisTemplate template;
	
	@MockBean
	private BoundListOperations<String, String> boundListOperations;
	
	@MockBean
	private BoundHashOperations<String, Object, Object> boundHashOperations;
	
	private DataManager dataManager = null;
	
	@Before
	public void before()
	{
		dataManager = new DataManager(template, new Calculator());
	}
	
	@Test
	public void testSaveDataExpectSuccess() 
	{
		String data = "{\"deviceId\":\"DeviceId\", \"type\":\"update\", \"windSpeed\":10.0}";
		List<String> speeds = new ArrayList<>(Arrays.asList("10.0"));
		
		given(this.template.boundListOps("List:DeviceId")).willReturn(boundListOperations);
		given(this.template.boundHashOps("Device:DeviceId")).willReturn(boundHashOperations);
		given(boundListOperations.size()).willReturn(1l);
		given(boundListOperations.range(0, -1)).willReturn(speeds);
		DeviceCommand command = dataManager.saveData(data);
		Assert.assertNotNull(command);
	}
}
