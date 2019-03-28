/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.be.model.operations.impl;

import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.HealingTitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceOperationTest {

	@InjectMocks
	private ComponentInstanceOperation componentInstanceOperation;

	@Mock
	protected HealingTitanGenericDao titanGenericDao;


	@Test
	public void testSetTitanGenericDao() {
		componentInstanceOperation.setTitanGenericDao(titanGenericDao);
	}

	@Test
	public void testUpdateInputValueInResourceInstance() {
		ComponentInstanceInput input = null;
		String resourceInstanceId = "";
		boolean b = false;
		Either<ComponentInstanceInput, StorageOperationStatus> result;

		result = componentInstanceOperation.updateInputValueInResourceInstance(input, resourceInstanceId, b);
		assertNull(result);
	}

	@Test
	public void testUpdateCustomizationUUID() {
		StorageOperationStatus result;
		String componentInstanceId = "instanceId";
		TitanVertex titanVertex = Mockito.mock(TitanVertex.class);
		when(titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(),componentInstanceId)).thenReturn(Either.left(titanVertex));
		result = componentInstanceOperation.updateCustomizationUUID(componentInstanceId);
		assertEquals(StorageOperationStatus.OK, result);
	}

	@Test
	public void testupdateComponentInstanceModificationTimeAndCustomizationUuidOnGraph_CatchException() throws Exception {
        ComponentInstance componentInstance = new ComponentInstance();
        GroupInstance groupInstance=new GroupInstance();
        groupInstance.setCreationTime(23234234234L);
        groupInstance.setCustomizationUUID("CUSTUUID0.1");
        groupInstance.setGroupUid("GRP0.1");
        groupInstance.setGroupUUID("GRPU0.1");
        groupInstance.setGroupName("GRP1");
        List gilist = new ArrayList<GroupInstance>();
        gilist.add(groupInstance);
        componentInstance.setUniqueId("INST0.1");
        componentInstance.setComponentUid("RES0.1");
        componentInstance.setGroupInstances(gilist);
        Either<ComponentInstanceData, StorageOperationStatus> result = componentInstanceOperation.updateComponentInstanceModificationTimeAndCustomizationUuidOnGraph(componentInstance, NodeTypeEnum.Component,234234545L,false);
        assertEquals(StorageOperationStatus.GENERAL_ERROR, result.right().value());
	}

    @Test
    public void testupdateComponentInstanceModificationTimeAndCustomizationUuidOnGraph_GENERAL_ERROR() throws Exception {
        ComponentInstance componentInstance = new ComponentInstance();
        GroupInstance groupInstance=new GroupInstance();
        groupInstance.setCreationTime(23234234234L);
        groupInstance.setCustomizationUUID("CUSTUUID0.1");
        groupInstance.setGroupUid("GRP0.1");
        groupInstance.setGroupUUID("GRPU0.1");
        groupInstance.setGroupName("GRP1");
        List gilist = new ArrayList<GroupInstance>();
        gilist.add(groupInstance);
        componentInstance.setUniqueId("INST0.1");
        componentInstance.setComponentUid("RES0.1");
        componentInstance.setGroupInstances(gilist);
        when(titanGenericDao.updateNode(anyObject(),eq(ComponentInstanceData.class))).thenReturn(Either.right(TitanOperationStatus.GENERAL_ERROR));
        Either<ComponentInstanceData, StorageOperationStatus> result = componentInstanceOperation.updateComponentInstanceModificationTimeAndCustomizationUuidOnGraph(componentInstance, NodeTypeEnum.Component,234234545L,false);
        assertEquals(StorageOperationStatus.GENERAL_ERROR, result.right().value());
    }

    @Test
    public void testupdateComponentInstanceModificationTimeAndCustomizationUuidOnGraph() throws Exception {
        ComponentInstance componentInstance = new ComponentInstance();
        GroupInstance groupInstance=new GroupInstance();
        groupInstance.setCreationTime(23234234234L);
        groupInstance.setCustomizationUUID("CUSTUUID0.1");
        groupInstance.setGroupUid("GRP0.1");
        groupInstance.setGroupUUID("GRPU0.1");
        groupInstance.setGroupName("GRP1");
        List gilist = new ArrayList<GroupInstance>();
        gilist.add(groupInstance);
        componentInstance.setUniqueId("INST0.1");
        componentInstance.setComponentUid("RES0.1");
        componentInstance.setGroupInstances(gilist);
        ComponentInstanceData componentInstanceData = new ComponentInstanceData();
        when(titanGenericDao.updateNode(anyObject(),eq(ComponentInstanceData.class))).thenReturn(Either.left(componentInstanceData));
        Either<ComponentInstanceData, StorageOperationStatus> result = componentInstanceOperation.updateComponentInstanceModificationTimeAndCustomizationUuidOnGraph(componentInstance, NodeTypeEnum.Component,234234545L,false);
        assertEquals(componentInstanceData, result.left().value());
    }
    
}
