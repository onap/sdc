package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class ComponentInstanceOperationTest {

	private ComponentInstanceOperation createTestSubject() {
		return new ComponentInstanceOperation();
	}

	
	@Test
	public void testSetTitanGenericDao() throws Exception {
		ComponentInstanceOperation testSubject;
		TitanGenericDao titanGenericDao = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTitanGenericDao(titanGenericDao);
	}

	@Test
	public void testUpdateInputValueInResourceInstance() throws Exception {
		ComponentInstanceOperation testSubject;
		ComponentInstanceInput input = null;
		String resourceInstanceId = "";
		boolean b = false;
		Either<ComponentInstanceInput, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.updateInputValueInResourceInstance(input, resourceInstanceId, b);
	}

	


	

}