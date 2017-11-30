package org.openecomp.sdc.be.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;

public class ProductServletTest {

	private ProductServlet createTestSubject() {
		return new ProductServlet();
	}

	
	@Test
	public void testCreateProduct() throws Exception {
		ProductServlet testSubject;
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetProductById() throws Exception {
		ProductServlet testSubject;
		String productId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetServiceByNameAndVersion() throws Exception {
		ProductServlet testSubject;
		String productName = "";
		String productVersion = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteProduct() throws Exception {
		ProductServlet testSubject;
		String productId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateProductMetadata() throws Exception {
		ProductServlet testSubject;
		String productId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateServiceName() throws Exception {
		ProductServlet testSubject;
		String productName = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}
}