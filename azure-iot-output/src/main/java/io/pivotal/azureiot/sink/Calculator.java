package io.pivotal.azureiot.sink;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Calculator {

	public double average(List<String> speeds)
	{
		double sum = 0.0;
		double average = 0.0;
		
		for (String speed : speeds)
		{
			sum += Double.valueOf(speed);
		}
		if (speeds.size() > 0)
		{
			average = sum / speeds.size();
		}
		return average;
	}
}
