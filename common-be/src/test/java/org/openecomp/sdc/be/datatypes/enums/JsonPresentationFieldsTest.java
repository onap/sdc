package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class JsonPresentationFieldsTest {

	private JsonPresentationFields createTestSubject() {
		return JsonPresentationFields.API_URL;
	}

	@Test
	public void testGetPresentation() throws Exception {
		JsonPresentationFields testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPresentation();
	}

	@Test
	public void testSetPresentation() throws Exception {
		JsonPresentationFields testSubject;
		String presentation = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPresentation(presentation);
	}

	@Test
	public void testGetStoredAs() throws Exception {
		JsonPresentationFields testSubject;
		GraphPropertyEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStoredAs();
	}

	@Test
	public void testSetStoredAs() throws Exception {
		JsonPresentationFields testSubject;
		GraphPropertyEnum storedAs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setStoredAs(storedAs);
	}

	@Test
	public void testGetPresentationByGraphProperty() throws Exception {
		GraphPropertyEnum property = null;
		String result;

		// default test
		result = JsonPresentationFields.getPresentationByGraphProperty(null);
		result = JsonPresentationFields.getPresentationByGraphProperty(GraphPropertyEnum.INVARIANT_UUID);
	}

	@Test
	public void testToString() throws Exception {
		JsonPresentationFields testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testGetByPresentation() throws Exception {
		String presentation = "";
		JsonPresentationFields result;

		// default test
		result = JsonPresentationFields.getByPresentation(presentation);
	}
}