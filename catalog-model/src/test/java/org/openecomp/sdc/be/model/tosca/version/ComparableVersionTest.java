package org.openecomp.sdc.be.model.tosca.version;

import javax.annotation.Generated;

import org.junit.Test;


public class ComparableVersionTest {

	private ComparableVersion createTestSubject() {
		return new ComparableVersion("");
	}

	
	@Test
	public void testParseVersion() throws Exception {
		ComparableVersion testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.parseVersion(version);
	}

	


	


	
	@Test
	public void testToString() throws Exception {
		ComparableVersion testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testEquals() throws Exception {
		ComparableVersion testSubject;
		Object o = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(o);
	}

	
	@Test
	public void testHashCode() throws Exception {
		ComparableVersion testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}
}