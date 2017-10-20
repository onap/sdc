package org.openecomp.sdc.be.model.tosca.version;

import java.util.StringTokenizer;

import javax.annotation.Generated;

import org.junit.Test;

public class VersionTest {

	private Version createTestSubject() {
		return new Version("");
	}

	
	@Test
	public void testHashCode() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		Version testSubject;
		Object other = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(other);
	}

	
	@Test
	public void testCompareTo() throws Exception {
		Version testSubject;
		Version otherVersion = null;
		int result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetMajorVersion() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMajorVersion();
	}

	
	@Test
	public void testGetMinorVersion() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMinorVersion();
	}

	
	@Test
	public void testGetIncrementalVersion() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIncrementalVersion();
	}

	
	@Test
	public void testGetBuildNumber() throws Exception {
		Version testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBuildNumber();
	}

	
	@Test
	public void testGetQualifier() throws Exception {
		Version testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getQualifier();
	}

	
	@Test
	public void testParseVersion() throws Exception {
		Version testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.parseVersion(version);
	}

	
	@Test
	public void testGetNextIntegerToken() throws Exception {
		Integer result;

		// default test
	}

	
	@Test
	public void testToString() throws Exception {
		Version testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}