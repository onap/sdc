package org.openecomp.sdc.be.model;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition.PropertyNames;


public class PropertyDefinitionTest {

	private PropertyDefinition createTestSubject() {
		return new PropertyDefinition();
	}
	
	@Test
	public void testCtor() throws Exception {
		new PropertyDefinition(new PropertyDefinition());
		new PropertyDefinition(new PropertyDataDefinition());
	}
	
	@Test
	public void testPropertyNames() throws Exception {
		PropertyNames availabiltyZoneCount = PropertyDefinition.PropertyNames.AVAILABILTY_ZONE_COUNT;
	}
	
	@Test
	public void testPropertyNames_GetPropertyName() throws Exception {
		PropertyDefinition.PropertyNames.AVAILABILTY_ZONE_COUNT.getPropertyName();
	}
	
	@Test
	public void testPropertyNames_GetUpdateBehavior() throws Exception {
		PropertyDefinition.PropertyNames.AVAILABILTY_ZONE_COUNT.getUpdateBehavior();
	}
	
	@Test
	public void testPropertyNames_FindName() throws Exception {
		PropertyDefinition.PropertyNames.findName(null);
		PropertyDefinition.PropertyNames.findName(PropertyDefinition.PropertyNames.AVAILABILTY_ZONE_COUNT.getPropertyName());
	}
	
	@Test
	public void testGroupInstancePropertyValueUpdateBehavior_GetLevelName() throws Exception {
		PropertyDefinition.GroupInstancePropertyValueUpdateBehavior.NOT_RELEVANT.getLevelName();
	}
	
	@Test
	public void testGroupInstancePropertyValueUpdateBehavior_GetLevelNumber() throws Exception {
		PropertyDefinition.GroupInstancePropertyValueUpdateBehavior.NOT_RELEVANT.getLevelNumber();
	}
	
	@Test
	public void testGetConstraints() throws Exception {
		PropertyDefinition testSubject;
		List<PropertyConstraint> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConstraints();
	}

	@Test
	public void testSetConstraints() throws Exception {
		PropertyDefinition testSubject;
		List<PropertyConstraint> constraints = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setConstraints(constraints);
	}

	@Test
	public void testToString() throws Exception {
		PropertyDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testIsDefinition() throws Exception {
		PropertyDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDefinition();
	}

	@Test
	public void testSetDefinition() throws Exception {
		PropertyDefinition testSubject;
		boolean definition = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefinition(definition);
	}

	@Test
	public void testHashCode() throws Exception {
		PropertyDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		PropertyDefinition testSubject;
		Object obj = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
		
		result = testSubject.equals(new Object());
		result = testSubject.equals(testSubject);
		PropertyDefinition testSubject2 = createTestSubject();
		result = testSubject.equals(testSubject2);
		testSubject2.setConstraints(new LinkedList<>());
		result = testSubject.equals(testSubject2);
		testSubject.setConstraints(new LinkedList<>());
		result = testSubject.equals(testSubject2);
		testSubject2.setName("mock");
		result = testSubject.equals(testSubject2);
		testSubject.setName("mock");
		result = testSubject.equals(testSubject2);
	}
}