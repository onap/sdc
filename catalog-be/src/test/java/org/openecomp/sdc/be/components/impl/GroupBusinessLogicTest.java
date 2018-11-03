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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
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


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
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
}