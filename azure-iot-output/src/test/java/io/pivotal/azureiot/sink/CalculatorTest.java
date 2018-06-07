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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CalculatorTest {

	private Calculator calculator = new Calculator();
	
	@Test
	public void testAverageEmpty() 
	{
		List<String> values = new ArrayList<String>();
		double average = calculator.average(values);
		Assert.assertEquals(0.0d, average, 0.01);
	}

	@Test
	public void testAverageOne() 
	{
		List<String> values = new ArrayList<String>();
		values.add("1");
		double average = calculator.average(values);
		Assert.assertEquals(1d, average, 0.01);
	}

	@Test
	public void testAverageTen() 
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

	@Test
	public void testComputeStatusExpectGreen()
	{
		String result = calculator.computeStatus(10.0d);
		Assert.assertEquals("green", result);
		
		result = calculator.computeStatus(30.0d);
		Assert.assertEquals("green", result);
	}
	
	@Test
	public void testComputeStatusExpectYellow()
	{
		String result = calculator.computeStatus(0.0d);
		Assert.assertEquals("yellow", result);
		
		result = calculator.computeStatus(9.9d);
		Assert.assertEquals("yellow", result);
	}
	
	@Test
	public void testComputeStatusExpectRed()
	{
		String result = calculator.computeStatus(30.1d);
		Assert.assertEquals("red", result);
		
		result = calculator.computeStatus(100d);
		Assert.assertEquals("red", result);
	}
	
}
