/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.merge.group;


import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GroupPropertiesMergeCommandTest {

    private GroupPropertiesMergeCommand testInstance;
    @Mock
    private DataDefinitionsValuesMergingBusinessLogic mergeBusinessLogic;
    @Mock
    private GroupsOperation groupsOperation;
    @Captor
    private ArgumentCaptor<List<GroupDefinition>> updatedGroupsCaptor;

    private GroupDefinition newGroup1, newGroup2, newGroup3;
    private GroupDefinition prevGroup1, prevGroup2, prevGroup3;
    private Resource prevResource, newResource;


    @Before
    public void setUp() throws Exception {
        testInstance = new GroupPropertiesMergeCommand(groupsOperation, new ComponentsUtils(mock(AuditingManager.class)), mergeBusinessLogic);
        newGroup1 = createVspGroupWithProperties("group1Id", "group1New", "prop1", "prop2");
        newGroup2 = createUserGroupWithProperties("group2Id", "group2New", "prop3", "prop4");
        newGroup3 = createVspGroupWithProperties("group3Id", "group3New");
        prevGroup1 = createVspGroupWithProperties("group1Id", "group1Old", "prop1", "prop2");
        prevGroup2 = createUserGroupWithProperties("group2Id", "group2Old", "prop3", "prop4");
        prevGroup3 = createVspGroupWithProperties("group3Id", "group3Old");

        prevResource = new ResourceBuilder()
                .addGroup(prevGroup1)
                .addGroup(prevGroup2)
                .addGroup(prevGroup3)
                .addInput("input1")
                .addInput("input2")
                .build();

        newResource = new ResourceBuilder()
                .addGroup(newGroup1)
                .addGroup(newGroup2)
                .addGroup(newGroup3)
                .addInput("input1")
                .build();
    }

    @Test
    public void whenNewComponentHasNoGroups_returnOk() {
        ActionStatus mergeStatus = testInstance.mergeComponents(prevResource, new Resource());
        assertThat(mergeStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(mergeBusinessLogic, groupsOperation);
    }

    @Test
    public void whenOldComponentHasNoGroups_returnOk() {
        ActionStatus mergeStatus = testInstance.mergeComponents(new Resource(), newResource);
        assertThat(mergeStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(mergeBusinessLogic, groupsOperation);
    }

    @Test
    public void whenOldOrNewGroupHasNoProperties_noNeedToMergeItsProperties() {
        GroupDefinition oldGrpNoProps = createVspGroupWithProperties("grp1", "grp1");
        GroupDefinition newGrpWithProps = createVspGroupWithProperties("grp1", "grp1", "prop1", "prop2");

        GroupDefinition oldGrpWithProps = createVspGroupWithProperties("grp2", "grp2", "prop3");
        GroupDefinition newGrpNoProps = createVspGroupWithProperties("grp2", "grp2");

        Resource prevResource = createResourceWithGroups(oldGrpNoProps, oldGrpWithProps);
        Resource newResource = createResourceWithGroups(newGrpWithProps, newGrpNoProps);
        ActionStatus mergeStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(mergeStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(mergeBusinessLogic, groupsOperation);
    }

    @Test
    public void whenNewGroupIsUserDefined_itWasAlreadyMergedInEarlierCommand_noNeedToMerge() {
        GroupDefinition oldUserDefinedGrp = createUserGroupWithProperties("grp1", "grp1", "prop1");
        GroupDefinition newUserDefinedGrp = createVspGroupWithProperties("grp1", "grp1", "prop1");
        Resource prevResource = createResourceWithGroups(oldUserDefinedGrp);
        Resource newResource = createResourceWithGroups(newUserDefinedGrp);
        ActionStatus mergeStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(mergeStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(mergeBusinessLogic, groupsOperation);
    }

    @Test
    public void whenNewGroupWasntExistInPrevVersion_noMergeRequired_matchByInvariantName() {
        GroupDefinition oldGrp = createUserGroupWithProperties("grp1", "grp1", "prop1");
        GroupDefinition newGrp = createVspGroupWithProperties("newGrp1", "grp1", "prop1");
        Resource prevResource = createResourceWithGroups(oldGrp);
        Resource newResource = createResourceWithGroups(newGrp);
        ActionStatus mergeStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(mergeStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(mergeBusinessLogic, groupsOperation);
    }

    @Test
    public void mergeGroupProperties_updateGroupsAfterMerge_mergeOnlyGroups() {
        when(groupsOperation.updateGroups(eq(newResource), updatedGroupsCaptor.capture(), any(PromoteVersionEnum.class))).thenReturn(Either.left(null));
        ActionStatus mergeStatus = testInstance.mergeComponents(prevResource, newResource);
        assertThat(mergeStatus).isEqualTo(ActionStatus.OK);
        verify(mergeBusinessLogic).mergeInstanceDataDefinitions(prevGroup1.getProperties(), prevResource.getInputs(), newGroup1.getProperties(), newResource.getInputs());
        assertThat(updatedGroupsCaptor.getValue())
                .containsExactly(newGroup1);

    }

    private GroupDefinition createUserGroupWithProperties(String invariantName, String groupName, String ... propsNames) {
        return createGroupWithProperties(invariantName, groupName, CreatedFrom.UI, propsNames);
    }

    private GroupDefinition createVspGroupWithProperties(String invariantName, String groupName, String ... propsNames) {
        return createGroupWithProperties(invariantName, groupName, CreatedFrom.CSAR, propsNames);
    }

    private GroupDefinition createGroupWithProperties(String invariantName, String groupName, CreatedFrom createdFrom, String ... propsNames) {
        GroupDefinitionBuilder groupDefinitionBuilder = GroupDefinitionBuilder.create()
                .setUniqueId(invariantName)
                .setName(groupName)
                .setCreatedFrom(createdFrom)
                .setInvariantName(invariantName);
        Stream.of(propsNames).forEach(groupDefinitionBuilder::addProperty);
        return groupDefinitionBuilder.build();
    }

    private Resource createResourceWithGroups(GroupDefinition ... groups) {
        ResourceBuilder resourceBuilder = new ResourceBuilder();
        Stream.of(groups).forEach(resourceBuilder::addGroup);
        return resourceBuilder.build();
    }
}
