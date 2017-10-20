package org.openecomp.sdc.be.servlets;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.exception.ResponseFormat;

import com.google.common.base.Equivalence.Wrapper;

public class AttributeServletTest {

	private AttributeServlet createTestSubject() {
		return new AttributeServlet();
	}

	
	@Test
	public void testCreateAttribute() throws Exception {
		AttributeServlet testSubject;
		String resourceId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateAttribute() throws Exception {
		AttributeServlet testSubject;
		String resourceId = "";
		String attributeId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteAttribute() throws Exception {
		AttributeServlet testSubject;
		String resourceId = "";
		String attributeId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testBuildAttributeFromString() throws Exception {
	AttributeServlet testSubject;String data = "";
	Wrapper<PropertyDefinition> attributesWrapper = null;
	Wrapper<ResponseFormat> errorWrapper = null;
	
	
	// default test
	}
}