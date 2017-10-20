package org.openecomp.sdc.be.servlets;

import java.util.List;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;

public class ElementServletTest {

	private ElementServlet createTestSubject() {
		return new ElementServlet();
	}

	
	@Test
	public void testGetComponentCategories() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetAllCategories() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testCreateComponentCategory() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteComponentCategory() throws Exception {
		ElementServlet testSubject;
		String categoryUniqueId = "";
		String componentType = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testCreateComponentSubCategory() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		String categoryId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteComponentSubCategory() throws Exception {
		ElementServlet testSubject;
		String categoryUniqueId = "";
		String subCategoryUniqueId = "";
		String componentType = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testCreateComponentGrouping() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		String grandParentCategoryId = "";
		String parentSubCategoryId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteComponentGrouping() throws Exception {
		ElementServlet testSubject;
		String grandParentCategoryUniqueId = "";
		String parentSubCategoryUniqueId = "";
		String groupingUniqueId = "";
		String componentType = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetTags() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetPropertyScopes() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetArtifactTypes() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetConfiguration() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetFollowedResourcesServices() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetCatalogComponents() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		List<OriginTypeEnum> excludeTypes = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteMarkedResources() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetListOfCsars() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}
}