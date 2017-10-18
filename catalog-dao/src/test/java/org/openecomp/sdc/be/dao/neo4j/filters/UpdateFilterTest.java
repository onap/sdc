package org.openecomp.sdc.be.dao.neo4j.filters;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class UpdateFilterTest {

	private UpdateFilter createTestSubject() {
		return new UpdateFilter(null);
	}

	
	@Test
	public void testGetToUpdate() throws Exception {
		UpdateFilter testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToUpdate();
	}

	
	@Test
	public void testSetToUpdate() throws Exception {
		UpdateFilter testSubject;
		Map<String, Object> toUpdate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setToUpdate(toUpdate);
	}

	

}