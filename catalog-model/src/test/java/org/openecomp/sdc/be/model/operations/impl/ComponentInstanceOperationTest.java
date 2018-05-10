package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.AttributeData;
import org.openecomp.sdc.be.resources.data.AttributeValueData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;

import fj.data.Either;

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





	
//	@Test
//	public void testValidateElementExistInGraph() throws Exception {
//		ComponentInstanceOperation testSubject;
//		String elementUniqueId = "";
//		NodeTypeEnum elementNodeType = null;
//		Supplier<Class<ElementData>> elementClassGen = null;
//		Wrapper<ElementData> elementDataWrapper = null;
//		Wrapper<TitanOperationStatus> errorWrapper = null;
//
//		// default test
//		testSubject = createTestSubject();
//		testSubject.validateElementExistInGraph(elementUniqueId, elementNodeType, elementClassGen, elementDataWrapper,
//				errorWrapper);
//	}

	
	

	




	


	

	
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