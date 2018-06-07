package io.pivotal.azureiot.sink;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CalculatorTest {

	private Calculator calculator = new Calculator();
	
	@Test
	public void testEmpty() 
	{
		List<String> values = new ArrayList<String>();
		double average = calculator.average(values);
		Assert.assertEquals(0.0d, average, 0.01);
	}

	@Test
	public void testOne() 
	{
		List<String> values = new ArrayList<String>();
		values.add("1");
		double average = calculator.average(values);
		Assert.assertEquals(1d, average, 0.01);
	}

	@Test
	public void testTen() 
	{
		List<String> values = new ArrayList<String>();
		values.add("1");
		values.add("2.0");
		values.add("3");
		values.add("4");
		values.add("5");
		values.add("6");
		values.add("7.0");
		values.add("8");
		values.add("9");
		values.add("10");
		double average = calculator.average(values);
		Assert.assertEquals(5.5d, average, 0.01);
	}

}
