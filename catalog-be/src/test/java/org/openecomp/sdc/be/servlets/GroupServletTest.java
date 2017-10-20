package org.openecomp.sdc.be.servlets;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;

public class GroupServletTest {

	private GroupServlet createTestSubject() {
		return new GroupServlet();
	}

	
	@Test
	public void testGetGroupArtifactById() throws Exception {
		GroupServlet testSubject;
		String containerComponentType = "";
		String componentId = "";
		String groupId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
	}

	
	@Test
	public void testUpdateGroupMetadata() throws Exception {
		GroupServlet testSubject;
		String containerComponentType = "";
		String componentId = "";
		String groupUniqueId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}
}