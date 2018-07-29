package org.openecomp.sdc.be.components.merge.property;

import mockit.Deencapsulation;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PropertyInstanceMergeDataBuilderTest {



	@Test
	public void testBuildDataForMerging() throws Exception {
		PropertyInstanceMergeDataBuilder testSubject;
		List oldProps = null;
		List<InputDefinition> oldInputs = null;
		List newProps = null;
		List<InputDefinition> newInputs = null;
		List<MergePropertyData> result;

		// default test

		result = PropertyInstanceMergeDataBuilder.buildDataForMerging(oldProps, oldInputs, newProps, newInputs);
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

		result = Deencapsulation.invoke(PropertyInstanceMergeDataBuilder.class, "buildMergeData", new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
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
		result = Deencapsulation.invoke(PropertyInstanceMergeDataBuilder.class, "buildMergePropertyData", new PropertyDataDefinition(), new HashMap<>(),
				new PropertyDataDefinition(), new HashMap<>());
	}

	@Test
	public void testGetOldGetInputNamesWhichExistInNewVersion() throws Exception {
		PropertyInstanceMergeDataBuilder testSubject;
		List<GetInputValueDataDefinition> getInputValues = null;
		Map<String, InputDefinition> newInputsByName = null;
		List<String> result;

		// default test
		result = Deencapsulation.invoke(PropertyInstanceMergeDataBuilder.class, "getOldGetInputNamesWhichExistInNewVersion", new LinkedList<>(), new HashMap<>());
	}

	@Test
	public void testGetOldDeclaredInputsByUser() throws Exception {
		PropertyInstanceMergeDataBuilder testSubject;
		List<GetInputValueDataDefinition> getInputValues = null;
		Map<String, InputDefinition> oldInputsByName = null;
		List<String> result;

		// default test
		result = Deencapsulation.invoke(PropertyInstanceMergeDataBuilder.class, "getOldDeclaredInputsByUser", new LinkedList<>(), new HashMap<>());
	}
}