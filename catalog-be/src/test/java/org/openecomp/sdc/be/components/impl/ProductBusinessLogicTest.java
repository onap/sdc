package org.openecomp.sdc.be.components.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;


public class ProductBusinessLogicTest {

	private ProductBusinessLogic createTestSubject() {
		return new ProductBusinessLogic();
	}

	@Test
	public void testValidateProductNameExists() throws Exception {
		ProductBusinessLogic testSubject;
		String productName = "";
		String userId = "";
		Either<Map<String, Boolean>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	@Test
	public void testSetDeploymentArtifactsPlaceHolder() throws Exception {
		ProductBusinessLogic testSubject;
		Component component = null;
		User user = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDeploymentArtifactsPlaceHolder(component, user);
	}

	@Test
	public void testDeleteMarkedComponents() throws Exception {
		ProductBusinessLogic testSubject;
		Either<List<String>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	@Test
	public void testGetComponentInstanceBL() throws Exception {
		ProductBusinessLogic testSubject;
		ComponentInstanceBusinessLogic result;

		// default test
		testSubject = createTestSubject();
		
	}

	@Test
	public void testGetComponentInstancesFilteredByPropertiesAndInputs() throws Exception {
		ProductBusinessLogic testSubject;
		String componentId = "";
		ComponentTypeEnum componentTypeEnum = null;
		String userId = "";
		String searchText = "";
		Either<List<ComponentInstance>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	@Test
	public void testGetCacheManagerOperation() throws Exception {
		ProductBusinessLogic testSubject;
		ICacheMangerOperation result;

		// default test
		testSubject = createTestSubject();
		
	}

	@Test
	public void testSetCacheManagerOperation() throws Exception {
		ProductBusinessLogic testSubject;
		ICacheMangerOperation cacheManagerOperation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCacheManagerOperation(cacheManagerOperation);
	}

	@Test
	public void testGetUiComponentDataTransferByComponentId() throws Exception {
		ProductBusinessLogic testSubject;
		String componentId = "";
		List<String> dataParamsToReturn = null;
		Either<UiComponentDataTransfer, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testCreateProduct() throws Exception {
		ProductBusinessLogic testSubject;
		Product product = null;
		User user = null;
		Either<Product, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		product = null;
		
	}

	
	@Test
	public void testCheckUnupdatableProductFields() throws Exception {
		ProductBusinessLogic testSubject;
		Product product = null;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateProductBeforeCreate() throws Exception {
		ProductBusinessLogic testSubject;
		Product product = null;
		User user = null;
		AuditingActionEnum actionEnum = null;
		Either<Product, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateProductFieldsBeforeCreate() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product product = null;
		AuditingActionEnum actionEnum = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateProductContactsList() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product product = null;
		AuditingActionEnum actionEnum = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateGrouping() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product product = null;
		AuditingActionEnum actionEnum = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetProduct() throws Exception {
		ProductBusinessLogic testSubject;
		String productId = "";
		User user = null;
		Either<Product, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteProduct() throws Exception {
		ProductBusinessLogic testSubject;
		String productId = "";
		User user = null;
		Either<Product, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateProductFullNameAndCleanup() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product product = null;
		AuditingActionEnum actionEnum = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateProductNameAndCleanup() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product product = null;
		AuditingActionEnum actionEnum = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateTagsListAndRemoveDuplicates() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product product = null;
		String oldProductName = "";
		AuditingActionEnum actionEnum = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateProductMetadata() throws Exception {
		ProductBusinessLogic testSubject;
		String productId = "";
		Product updatedProduct = null;
		User user = null;
		Either<Product, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		updatedProduct = null;
	}

	
	@Test
	public void testValidateAndUpdateProductMetadata() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product currentProduct = null;
		Product updatedProduct = null;
		Either<Product, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateProductName() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product currentProduct = null;
		Product updatedProduct = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateFullName() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product currentProduct = null;
		Product updatedProduct = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateCategory() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product currentProduct = null;
		Product updatedProduct = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateContactList() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product currentProduct = null;
		Product updatedProduct = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateTags() throws Exception {
		ProductBusinessLogic testSubject;
		User user = null;
		Product currentProduct = null;
		Product updatedProduct = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateTagPattern() throws Exception {
		ProductBusinessLogic testSubject;
		String tag = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetProductByNameAndVersion() throws Exception {
		ProductBusinessLogic testSubject;
		String productName = "";
		String productVersion = "";
		String userId = "";
		Either<Product, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}
}