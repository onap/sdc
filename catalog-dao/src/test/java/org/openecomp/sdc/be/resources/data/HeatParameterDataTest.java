package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;


public class HeatParameterDataTest {

	private HeatParameterData createTestSubject() {
		return new HeatParameterData();
	}

	

	
	@Test
	public void testGetHeatDataDefinition() throws Exception {
		HeatParameterData testSubject;
		HeatParameterDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHeatDataDefinition();
	}

	
	@Test
	public void testSetHeatDataDefinition() throws Exception {
		HeatParameterData testSubject;
		HeatParameterDataDefinition heatDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHeatDataDefinition(heatDataDefinition);
	}

	
	@Test
	public void testGetName() throws Exception {
		HeatParameterData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		HeatParameterData testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetType() throws Exception {
		HeatParameterData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		HeatParameterData testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		HeatParameterData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		HeatParameterData testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetCurrentValue() throws Exception {
		HeatParameterData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCurrentValue();
	}

	
	@Test
	public void testSetCurrentValue() throws Exception {
		HeatParameterData testSubject;
		String currentValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCurrentValue(currentValue);
	}

	
	@Test
	public void testGetDefaultValue() throws Exception {
		HeatParameterData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultValue();
	}

	
	@Test
	public void testSetDefaultValue() throws Exception {
		HeatParameterData testSubject;
		String defaultValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultValue(defaultValue);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		HeatParameterData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		HeatParameterData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}