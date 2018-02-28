package org.openecomp.sdc.be.components.impl.version;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.GroupsOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupMembersUpdateOperationTest {

    private static final String CONTAINER_ID = "containerId";
    private static final String INSTANCE_ID_PRE_CHANGE = "inst2";
    private static final String INSTANCE_ID_POST_CHANGE = "newInst2";
    @InjectMocks
    private GroupMembersUpdateOperation testInstance;
    @Mock
    private GroupsOperation groupsOperation;
    @Mock
    private ComponentsUtils componentsUtils;

    @Captor
    private ArgumentCaptor<List<GroupDataDefinition>> updatedGroupsCaptor;
    private GroupDefinition group1, group2, group3;
    private Resource container;
    private ComponentInstance prevInst2Version, currInst2Version;
    private static final ComponentInstance DONT_CARE = new ComponentInstance();

    @Before
    public void setUp() throws Exception {
        group1 = createGroupWithMembers("group1", "inst1", INSTANCE_ID_PRE_CHANGE);
        group2 = createGroupWithMembers("group2", "inst1", "inst2", "inst3");
        group3 = createGroupWithMembers("group3", "inst1", "inst3");
        container = new ResourceBuilder()
                .addGroups(group1)
                .addGroups(group2)
                .setUniqueId(CONTAINER_ID)
                .setComponentType(ComponentTypeEnum.RESOURCE)
                .build();
        prevInst2Version = new ComponentInstanceBuilder()
                .setId(INSTANCE_ID_PRE_CHANGE)
                .build();
        currInst2Version = new ComponentInstanceBuilder()
                .setId(INSTANCE_ID_POST_CHANGE)
                .build();
    }

    @Test
    public void whenNoGroupsOnContainer_returnOk() {
        ActionStatus actionStatus = testInstance.onChangeVersion(new Resource(), DONT_CARE, DONT_CARE);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void whenEmptyListOfGroups_returnOk() {
        Resource resource = new Resource();
        resource.setGroups(new ArrayList<>());
        ActionStatus actionStatus = testInstance.onChangeVersion(resource, DONT_CARE, DONT_CARE);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void whenGroupsHasNoMembers_returnOk() {
        GroupDefinition group1 = new GroupDefinition();
        group1.setMembers(emptyMap());
        GroupDefinition group2 = new GroupDefinition();
        group2.setMembers(emptyMap());
        Resource container = new ResourceBuilder()
                .addGroups(group1)
                .addGroups(group2)
                .build();
        ComponentInstance prevInstance = new ComponentInstanceBuilder().setId("inst1").build();
        ActionStatus actionStatus = testInstance.onChangeVersion(container, prevInstance, DONT_CARE);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void whenPrevInstanceIsNotAMemberOfAnyGroup_returnOk() {
        ComponentInstance prevInstance = new ComponentInstanceBuilder().setId("nonMemberInst").build();
        ActionStatus actionStatus = testInstance.onChangeVersion(container, prevInstance, DONT_CARE);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void whenGroupHasPrevInstanceAsMember_replaceWithNewInstanceId_updateReplacedGroups() {
        verifyAllGroupsHasPrevInstancesAsMembers();
        when(groupsOperation.updateGroups(eq(container), updatedGroupsCaptor.capture())).thenReturn(Either.left(null));
        ActionStatus actionStatus = testInstance.onChangeVersion(container, prevInst2Version, currInst2Version);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyGroupWithPrevInstanceMemberWereReplaced();
        assertThat(updatedGroupsCaptor.getValue())
                .containsExactlyInAnyOrder(group1, group2);
    }

    @Test
    public void whenFailingToUpdateGroups_propagateError() {
        when(groupsOperation.updateGroups(eq(container), updatedGroupsCaptor.capture())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, ComponentTypeEnum.RESOURCE)).thenCallRealMethod();
        ActionStatus actionStatus = testInstance.onChangeVersion(container, prevInst2Version, currInst2Version);
        assertThat(actionStatus).isEqualTo(ActionStatus.RESOURCE_NOT_FOUND);
    }

    private void verifyGroupWithPrevInstanceMemberWereReplaced() {
        assertGroupMembersIds(group1, "inst1", INSTANCE_ID_POST_CHANGE);
        assertGroupMembersIds(group2, "inst1", INSTANCE_ID_POST_CHANGE, "inst3");
        assertGroupMembersIds(group3, "inst1", "inst3");
    }

    private void verifyAllGroupsHasPrevInstancesAsMembers() {
        assertGroupMembersIds(group1, "inst1", INSTANCE_ID_PRE_CHANGE);
        assertGroupMembersIds(group2, "inst1", INSTANCE_ID_PRE_CHANGE, "inst3");
        assertGroupMembersIds(group3, "inst1", "inst3");
    }

    private void assertGroupMembersIds(GroupDefinition group, String ... expectedMembersIds) {
        assertThat(group.getMembers())
                .containsValues(expectedMembersIds);
    }

    private GroupDefinition createGroupWithMembers(String groupId, String ... membersIds) {
        GroupDefinitionBuilder groupDefinitionBuilder = GroupDefinitionBuilder.create();
        Stream.of(membersIds).forEach(memberId -> groupDefinitionBuilder.addMember(memberId + "name", memberId));
        groupDefinitionBuilder.setUniqueId(groupId);
        return groupDefinitionBuilder.build();
    }

}