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
package org.openecomp.sdc.be.components.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.policy.PolicyTargetsUpdateHandler;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsontitan.operations.GroupsOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import fj.data.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GroupBusinessLogicTest {

    @InjectMocks
    private GroupBusinessLogic test;

    @Mock
    private ApplicationDataTypeCache dataTypeCache;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private IGroupTypeOperation groupTypeOperation;
    @Mock
    private GroupsOperation groupsOperation;
    @Mock
    private AccessValidations accessValidations;
    @Mock
    private TitanDao titanDao;
    @Mock
    PolicyTargetsUpdateHandler policyTargetsUpdateHandler;

    @Test
    public void testCreateGroups_NoDataType() {
        Either<List<GroupDefinition>, ResponseFormat> result;
        Component component = new Resource();
        List<GroupDefinition> groupDefinitions = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinitions.add(groupDefinition);
        when(dataTypeCache.getAll()).thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
        result = test.createGroups(component, groupDefinitions, true);
        assertThat(result.isRight());
    }

    @Test
    public void testCreateGroups() {
        Either<List<GroupDefinition>, ResponseFormat> result;
        Component component = new Resource();
        component.setUniqueId("id");
        List<GroupDefinition> groupDefinitions = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setName("name");
        groupDefinitions.add(groupDefinition);
        groupDefinition.setType(Constants.DEFAULT_GROUP_VF_MODULE);
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        Map<String, DataTypeDefinition> map = new HashMap<>();
        when(dataTypeCache.getAll()).thenReturn(Either.left(map));
        when(groupTypeOperation.getLatestGroupTypeByType(Constants.DEFAULT_GROUP_VF_MODULE, true)).thenReturn(Either.left(groupTypeDefinition));
        when(groupsOperation.createGroups(any(Component.class), anyMap())).thenReturn(Either.left(groupDefinitions));
        when(groupsOperation.addCalculatedCapabilitiesWithProperties(anyString(), anyMap(), anyMap())).thenReturn(StorageOperationStatus.OK);
        result = test.createGroups(component, groupDefinitions, true);
        assertThat(result.isLeft());
    }

    @Test
    public void testUpdateGroup() throws Exception {

        Component component= new Resource();
        GroupDefinition updatedGroup = new GroupDefinition();
        List<GroupDefinition> grpdefList = new ArrayList<>();
        updatedGroup.setName("GRP.01");
        grpdefList.add(updatedGroup);
        component.setUniqueId("GRP.01");
        component.setGroups(grpdefList);
        updatedGroup.setUniqueId("GRP.01");
        when(accessValidations.validateUserCanWorkOnComponent("compid", ComponentTypeEnum.SERVICE, "USR01", "UpdateGroup")).thenReturn(component);
        when(groupsOperation.updateGroup(component, updatedGroup)).thenReturn(Either.left(updatedGroup));
        GroupDefinition Gdefinition = test.updateGroup("compid", ComponentTypeEnum.SERVICE, "GRP.01",
                "USR01", updatedGroup);
        Assert.assertEquals(Gdefinition,updatedGroup);
    }


    @Test(expected = ComponentException.class)
    public void testUpdateGroup_Invalidname() throws Exception {

        Component component= new Resource();
        GroupDefinition updatedGroup = new GroupDefinition();
        List<GroupDefinition> grpdefList = new ArrayList<>();
        updatedGroup.setName("GRP~01");
        updatedGroup.setUniqueId("GRP.01");
        grpdefList.add(updatedGroup);
        component.setUniqueId("GRP.01");
        component.setGroups(grpdefList);
        when(accessValidations.validateUserCanWorkOnComponent("compid", ComponentTypeEnum.SERVICE, "USR01", "UpdateGroup")).thenReturn(component);
        GroupDefinition Gdefinition = test.updateGroup("compid", ComponentTypeEnum.SERVICE, "GRP.01",
                "USR01", updatedGroup);

    }

    @Test(expected = ComponentException.class)
    public void testDeleteGroup_exception() throws Exception {

        Component component= new Resource();
        GroupDefinition updatedGroup = new GroupDefinition();
        List<GroupDefinition> grpdefList = new ArrayList<>();
        updatedGroup.setName("GRP~01");
        updatedGroup.setUniqueId("GRP.01");
        grpdefList.add(updatedGroup);
        component.setUniqueId("GRP.01");
        component.setGroups(grpdefList);
        when(accessValidations.validateUserCanWorkOnComponent("compid", ComponentTypeEnum.SERVICE, "USR01", "DeleteGroup")).thenReturn(component);
        when(groupsOperation.deleteGroups(anyObject(),anyList())).thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));

        when(titanDao.rollback()).thenReturn(TitanOperationStatus.OK);
        GroupDefinition Gdefinition = test.deleteGroup("compid", ComponentTypeEnum.SERVICE, "GRP.01",
                "USR01");
    }

    @Test
    public void testDeleteGroup() throws Exception {

        Component component= new Resource();
        GroupDefinition updatedGroup = new GroupDefinition();
        List<GroupDefinition> grpdefList = new ArrayList<>();
        updatedGroup.setName("GRP~01");
        updatedGroup.setUniqueId("GRP.01");
        grpdefList.add(updatedGroup);
        component.setUniqueId("GRP.01");
        component.setGroups(grpdefList);
        when(accessValidations.validateUserCanWorkOnComponent("compid", ComponentTypeEnum.SERVICE, "USR01", "DeleteGroup")).thenReturn(component);
        when(groupsOperation.deleteGroups(anyObject(),anyList())).thenReturn(Either.left(grpdefList));
        when(groupsOperation.deleteCalculatedCapabilitiesWithProperties(anyString(), anyObject())).thenReturn(StorageOperationStatus.OK);
        when(policyTargetsUpdateHandler.removePoliciesTargets(anyObject(),anyString(),anyObject())).thenReturn(ActionStatus.OK);

        GroupDefinition Gdefinition = test.deleteGroup("compid", ComponentTypeEnum.SERVICE, "GRP.01",
                "USR01");
        Assert.assertEquals(Gdefinition,updatedGroup);
    }


}