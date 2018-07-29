package org.openecomp.sdc.be.components.merge.property;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.LinkedList;
import java.util.List;

public class MergePropertyDataTest {

	private MergePropertyData createTestSubject() {
		return new MergePropertyData();
	}

	@Test
	public void testGetOldProp() throws Exception {
		MergePropertyData testSubject;
		PropertyDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOldProp();
	}

	@Test
	public void testSetOldProp() throws Exception {
		MergePropertyData testSubject;
		PropertyDataDefinition oldProp = null;
		MergePropertyData result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOldProp(oldProp);
	}

	@Test
	public void testSetNewProp() throws Exception {
		MergePropertyData testSubject;
		PropertyDataDefinition newProp = null;
		MergePropertyData result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setNewProp(newProp);
	}

	@Test
	public void testGetNewProp() throws Exception {
		MergePropertyData testSubject;
		PropertyDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNewProp();
	}

	@Test
	public void testAddAddGetInputNamesToMerge() throws Exception {
		MergePropertyData testSubject;
		List<String> getInputsNameToMerge = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
		testSubject.addAddGetInputNamesToMerge(getInputsNameToMerge);
	}

	@Test
	public void testGetGetInputNamesToMerge() throws Exception {
		MergePropertyData testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGetInputNamesToMerge();
	}

	@Test
	public void testIsGetInputProp() throws Exception {
		MergePropertyData testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		testSubject.setOldProp(new PropertyDataDefinition());
		result = testSubject.isGetInputProp();
	}
}