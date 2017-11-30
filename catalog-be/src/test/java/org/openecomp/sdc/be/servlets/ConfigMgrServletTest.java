package org.openecomp.sdc.be.servlets;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.datastax.driver.core.Configuration;

public class ConfigMgrServletTest {

	private ConfigMgrServlet createTestSubject() {
		return new ConfigMgrServlet();
	}

	
	@Test
	public void testGetConfig() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		String type = "";
		String result;

		// test 1
		testSubject = createTestSubject();
		type = null;


		// test 2
		testSubject = createTestSubject();
		type = "";

		// test 3
		testSubject = createTestSubject();
		type = "configuration";
	}

	
	@Test
	public void testSetConfig1() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		Configuration configuration = null;
		String result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testSetConfig2() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		Configuration configuration = null;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testSetConfig3() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		Configuration configuration = null;
		String result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testSetConfig4() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		Configuration configuration = null;

		// default test
		testSubject = createTestSubject();
	}
}