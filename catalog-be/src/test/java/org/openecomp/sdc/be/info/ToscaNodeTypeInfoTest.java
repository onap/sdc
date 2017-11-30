package org.openecomp.sdc.be.info;

import java.util.List;

import org.junit.Test;


public class ToscaNodeTypeInfoTest {

	private ToscaNodeTypeInfo createTestSubject() {
		return new ToscaNodeTypeInfo();
	}

	
	@Test
	public void testGetNodeName() throws Exception {
		ToscaNodeTypeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNodeName();
	}

	
	@Test
	public void testSetNodeName() throws Exception {
		ToscaNodeTypeInfo testSubject;
		String nodeName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNodeName(nodeName);
	}

	
	@Test
	public void testGetTemplateVersion() throws Exception {
		ToscaNodeTypeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTemplateVersion();
	}

	
	@Test
	public void testSetTemplateVersion() throws Exception {
		ToscaNodeTypeInfo testSubject;
		String templateVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTemplateVersion(templateVersion);
	}

	
	@Test
	public void testGetInterfaces() throws Exception {
		ToscaNodeTypeInfo testSubject;
		List<ToscaNodeTypeInterface> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInterfaces();
	}

	
	@Test
	public void testSetInterfaces() throws Exception {
		ToscaNodeTypeInfo testSubject;
		List<ToscaNodeTypeInterface> interfaces = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInterfaces(interfaces);
	}

	
	@Test
	public void testGetIconPath() throws Exception {
		ToscaNodeTypeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIconPath();
	}

	
	@Test
	public void testSetIconPath() throws Exception {
		ToscaNodeTypeInfo testSubject;
		String iconPath = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setIconPath(iconPath);
	}

	
	@Test
	public void testToString() throws Exception {
		ToscaNodeTypeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}