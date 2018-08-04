package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;

import org.junit.Test;


public class CompositionDataDefinitionTest {

	private CompositionDataDefinition createTestSubject() {
		return new CompositionDataDefinition();
	}

	
	@Test
	public void testGetComponentInstances() throws Exception {
		CompositionDataDefinition testSubject;
		Map<String, ComponentInstanceDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstances();
	}

	
	@Test
	public void testSetComponentInstances() throws Exception {
		CompositionDataDefinition testSubject;
		Map<String, ComponentInstanceDataDefinition> componentInstances = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstances(componentInstances);
	}

	
	@Test
	public void testGetRelations() throws Exception {
		CompositionDataDefinition testSubject;
		Map<String, RelationshipInstDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelations();
	}

	
	@Test
	public void testSetRelations() throws Exception {
		CompositionDataDefinition testSubject;
		Map<String, RelationshipInstDataDefinition> relations = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelations(relations);
	}

	
	@Test
	public void testAddInstance() throws Exception {
		CompositionDataDefinition testSubject;
		String key = "";
		ComponentInstanceDataDefinition instance = null;

		// default test
		testSubject = createTestSubject();
		testSubject.addInstance(key, instance);
	}

	
	@Test
	public void testAddRelation() throws Exception {
		CompositionDataDefinition testSubject;
		String key = "";
		RelationshipInstDataDefinition relation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.addRelation(key, relation);
	}
}