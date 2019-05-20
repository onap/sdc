package org.openecomp.sdc.be.components.impl.instance;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.group.GroupMembersUpdater;
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
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupMembersUpdateOperationTest {

    private static final String CONTAINER_ID = "containerId";
    private static final String INSTANCE_ID_PRE_CHANGE = "inst2";
    private static final String INSTANCE_ID_POST_CHANGE = "newInst2";
    private GroupMembersUpdateOperation testInstance;
    @Mock
    private GroupsOperation groupsOperation;
    @Captor
    private ArgumentCaptor<List<GroupDataDefinition>> updatedGroupsCaptor;
    private GroupDefinition group1, group2, group3;
    private Resource container;
    private ComponentInstance prevInst2Version, currInst2Version;
    private static final ComponentInstance DONT_CARE_WHICH_INST = new ComponentInstance();
    private static final String DONT_CARE_WHICH_ID = "someString";

    @Before
    public void setUp() throws Exception {
        testInstance = new GroupMembersUpdateOperation(groupsOperation, new ComponentsUtils(mock(AuditingManager.class)), new GroupMembersUpdater());
        group1 = createGroupWithMembers("group1", "inst1", INSTANCE_ID_PRE_CHANGE);
        group2 = createGroupWithMembers("group2", "inst1", INSTANCE_ID_PRE_CHANGE, "inst3");
        group3 = createGroupWithMembers("group3", "inst1", "inst3");
        container = new ResourceBuilder()
                .addGroup(group1)
                .addGroup(group2)
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
    public void onChangeVersion_whenNoGroupsOnContainer_returnOk() {
        ActionStatus actionStatus = testInstance.onChangeVersion(new Resource(), DONT_CARE_WHICH_INST, DONT_CARE_WHICH_INST);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void onChangeVersion_whenEmptyListOfGroups_returnOk() {
        Resource resource = new Resource();
        resource.setGroups(new ArrayList<>());
        ActionStatus actionStatus = testInstance.onChangeVersion(resource, DONT_CARE_WHICH_INST, DONT_CARE_WHICH_INST);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void onChangeVersion_whenGroupsHasNoMembers_returnOk() {
        GroupDefinition group1 = new GroupDefinition();
        group1.setMembers(emptyMap());
        GroupDefinition group2 = new GroupDefinition();
        group2.setMembers(emptyMap());
        Resource container = new ResourceBuilder()
                .addGroup(group1)
                .addGroup(group2)
                .build();
        ComponentInstance prevInstance = new ComponentInstanceBuilder().setId("inst1").build();
        ActionStatus actionStatus = testInstance.onChangeVersion(container, prevInstance, DONT_CARE_WHICH_INST);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void onChangeVersion_whenPrevInstanceIsNotAMemberOfAnyGroup_returnOk() {
        ComponentInstance prevInstance = new ComponentInstanceBuilder().setId("nonMemberInst").build();
        ActionStatus actionStatus = testInstance.onChangeVersion(container, prevInstance, DONT_CARE_WHICH_INST);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void onChangeVersion_whenGroupHasPrevInstanceAsMember_replaceWithNewInstanceId_updateReplacedGroups() {
        verifyAllGroupsHasPrevInstancesAsMembers();
        when(groupsOperation.updateGroups(eq(container), updatedGroupsCaptor.capture(), eq(false))).thenReturn(Either.left(null));
        ActionStatus actionStatus = testInstance.onChangeVersion(container, prevInst2Version, currInst2Version);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        assertUpdatedGroups(updatedGroupsCaptor.getValue(), group1, group2);
        verifyGroupWithPrevInstanceMemberWereReplaced();
    }

    @Test
    public void onChangeVersion_whenFailingToUpdateGroups_propagateError() {
        when(groupsOperation.updateGroups(eq(container), updatedGroupsCaptor.capture(), eq(false))).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        ActionStatus actionStatus = testInstance.onChangeVersion(container, prevInst2Version, currInst2Version);
        assertThat(actionStatus).isEqualTo(ActionStatus.RESOURCE_NOT_FOUND);
    }

    @Test
    public void onDeleteInstance_whenNoGroupsOnContainer_returnOk() {
        ActionStatus actionStatus = testInstance.onDelete(new Resource(), DONT_CARE_WHICH_ID);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void onDeleteInstance_whenEmptyListOfGroups_returnOk() {
        Resource resource = new Resource();
        resource.setGroups(new ArrayList<>());
        ActionStatus actionStatus = testInstance.onDelete(resource, DONT_CARE_WHICH_ID);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void onDeleteInstance_whenGroupsHasNoMembers_returnOk() {
        GroupDefinition group1 = new GroupDefinition();
        group1.setMembers(emptyMap());
        GroupDefinition group2 = new GroupDefinition();
        group2.setMembers(emptyMap());
        Resource container = new ResourceBuilder()
                .addGroup(group1)
                .addGroup(group2)
                .build();
        ActionStatus actionStatus = testInstance.onDelete(container, "inst1");
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void onDeleteInstance_whenDeletedInstanceIsNotAMemberOfAnyGroup_returnOk() {
        ActionStatus actionStatus = testInstance.onDelete(container, "nonMemberInst");
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void onDeleteInstance_removeInstanceIdFromGroupMember() {
        when(groupsOperation.updateGroups(eq(container), updatedGroupsCaptor.capture(), eq(false))).thenReturn(Either.left(null));
        ActionStatus actionStatus = testInstance.onDelete(container, INSTANCE_ID_PRE_CHANGE);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        assertUpdatedGroups(updatedGroupsCaptor.getValue(), group1, group2);
        assertGroupMembersIds(group1, "inst1");
        assertGroupMembersIds(group2, "inst1", "inst3");
    }

    @Test
    public void onDeleteInstance_whenGroupsUpdateFails_propagateTheFailure() {
        when(groupsOperation.updateGroups(eq(container), anyList(), eq(false))).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        ActionStatus actionStatus = testInstance.onDelete(container, INSTANCE_ID_PRE_CHANGE);
        assertThat(actionStatus).isEqualTo(ActionStatus.RESOURCE_NOT_FOUND);
    }

    private void assertUpdatedGroups(List<GroupDataDefinition> actual, GroupDataDefinition ... expected) {
        assertThat(actual)
                .containsExactlyInAnyOrder(expected);
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
        Stream.of(membersIds).forEach(groupDefinitionBuilder::addMember);
        groupDefinitionBuilder.setUniqueId(groupId);
        return groupDefinitionBuilder.build();
    }

}