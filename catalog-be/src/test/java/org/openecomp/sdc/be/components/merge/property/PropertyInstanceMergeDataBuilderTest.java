package org.openecomp.sdc.be.components.merge.property;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;

import mockit.Deencapsulation;

public class PropertyInstanceMergeDataBuilderTest {

	private PropertyInstanceMergeDataBuilder createTestSubject() {
		return PropertyInstanceMergeDataBuilder.getInstance();
	}

	@Test
	public void testGetInstance() throws Exception {
		PropertyInstanceMergeDataBuilder result;

		// default test
		result = PropertyInstanceMergeDataBuilder.getInstance();
	}

	@Test
	public void testBuildDataForMerging() throws Exception {
		PropertyInstanceMergeDataBuilder testSubject;
		List oldProps = null;
		List<InputDefinition> oldInputs = null;
		List newProps = null;
		List<InputDefinition> newInputs = null;
		List<MergePropertyData> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.buildDataForMerging(oldProps, oldInputs, newProps, newInputs);
	}

	@Test
	public void testBuildMergeData() throws Exception {
		PropertyInstanceMergeDataBuilder testSubject;
		Map<String, T> oldPropsByName = null;
		Map<String, InputDefinition> oldInputsByName = null;
		Map<String, T> newPropsByName = null;
		Map<String, InputDefinition> newInputsByName = null;
		List<MergePropertyData> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "buildMergeData", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
	}

	@Test
	public void testBuildMergePropertyData() throws Exception {
		PropertyInstanceMergeDataBuilder testSubject;
		PropertyDataDefinition oldProp = null;
		Map<String, InputDefinition> oldInputsByName = null;
		PropertyDataDefinition newProp = null;
		Map<String, InputDefinition> newInputsByName = null;
		MergePropertyData result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "buildMergePropertyData", new PropertyDataDefinition(), new HashMap<>(),
				new PropertyDataDefinition(), new HashMap<>());
	}

	@Test
	public void testGetOldGetInputNamesWhichExistInNewVersion() throws Exception {
		PropertyInstanceMergeDataBuilder testSubject;
		List<GetInputValueDataDefinition> getInputValues = null;
		Map<String, InputDefinition> newInputsByName = null;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getOldGetInputNamesWhichExistInNewVersion", new LinkedList<>(), new HashMap<>());
	}

	@Test
	public void testGetOldDeclaredInputsByUser() throws Exception {
		PropertyInstanceMergeDataBuilder testSubject;
		List<GetInputValueDataDefinition> getInputValues = null;
		Map<String, InputDefinition> oldInputsByName = null;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getOldDeclaredInputsByUser", new LinkedList<>(), new HashMap<>());
	}
}