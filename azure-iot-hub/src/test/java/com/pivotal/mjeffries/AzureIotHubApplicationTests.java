package com.pivotal.mjeffries;

import io.pivotal.azureiot.source.AzureIotHubSourceProperties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations="classpath:test.properties")
public class AzureIotHubApplicationTests {
	
	@Autowired
	private AzureIotHubSourceProperties source;

	@Test
	public void contextLoads() {
	}

}
