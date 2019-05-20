package org.openecomp.sdc.be.components.merge.group;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComponentGroupMergeCommandTest {

    private static final Resource DONT_CARE = new Resource();
    private ComponentGroupMergeCommand testInstance;
    @Mock
    private GroupsOperation groupsOperation;

    @Captor
    private ArgumentCaptor<List<GroupDefinition>> groupsToAddCaptor;

    @Before
    public void setUp() throws Exception {
        testInstance = new ComponentGroupMergeCommand(groupsOperation, new ComponentsUtils(Mockito.mock(AuditingManager.class)));
    }

    @Test
    public void whenPrevComponentHasNoGroups_returnOk() {
        ActionStatus actionStatus = testInstance.mergeComponents(new Resource(), DONT_CARE);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);

        Resource prevResource = new Resource();
        prevResource.setGroups(emptyList());

        actionStatus = testInstance.mergeComponents(prevResource, DONT_CARE);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(groupsOperation);
    }

    @Test
    public void whenAllPrevGroupsCreatedFromCsar_returnOk() {
        GroupDefinition group1 = createCsarGroup("group1");
        GroupDefinition group2 = createCsarGroup("group2");
        Resource prevResource = createResourceWithGroups(group1, group2);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, DONT_CARE);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(groupsOperation);
    }

    @Test
    public void whenAllPrevGroupsAlreadyExistInNewComponent_returnOk() {
        GroupDefinition group1 = createCsarGroup("group1");
        GroupDefinition group2 = createUserDefinedGroup("group2");
        GroupDefinition group3 = createUserDefinedGroup("group3");
        Resource prevResource = createResourceWithGroups(group1, group2, group3);
        Resource currResource = createResourceWithGroups(group1, group2, group3);
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        verifyZeroInteractions(groupsOperation);
    }

    @Test
    public void addAllPrevUserDefinedGroupsToComponent() {
        GroupDefinition group1 = createCsarGroup("group1");
        GroupDefinition group2 = createUserDefinedGroup("group2");
        GroupDefinition group3 = createUserDefinedGroup("group3");
        Resource prevResource = createResourceWithGroups(group1, group2, group3);
        Resource currResource = createResourceWithGroups(group1, group2);
        when(groupsOperation.addGroups(eq(currResource), groupsToAddCaptor.capture())).thenReturn(Either.left(null));
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        assertThat(groupsToAddCaptor.getValue())
                .containsExactlyInAnyOrder(group3);
        assertThat(currResource.getGroups())
                .containsExactlyInAnyOrder(group1, group2, group3);
    }

    @Test
    public void whenPrevUserDefinedGroupHasNoMembers_itShouldHaveNoMembersInNewComponent() {
        GroupDefinition group1 = createUserDefinedGroup("group1");
        Resource prevResource = createResourceWithGroups(group1);
        Resource currResource = new Resource();
        when(groupsOperation.addGroups(eq(currResource), groupsToAddCaptor.capture())).thenReturn(Either.left(null));
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        List<GroupDefinition> groupsAdded = groupsToAddCaptor.getValue();
        assertThat(groupsAdded)
                .extracting("members")
                .containsNull();
    }

    @Test
    public void whenPrevUserDefinedGroupHasInstanceMembersThatNotExistInNewComponent_removeMembersFromGroup() {
        GroupDefinition group1 = createUserDefinedGroup("group1", "inst1Id", "inst2Id", "inst3Id");
        GroupDefinition group2 = createUserDefinedGroup("group2", "inst1Id");

        ComponentInstance inst1 = createInstance("inst1", "inst1Id");
        ComponentInstance inst2 = createInstance("inst2", "inst2Id");
        ComponentInstance inst3 = createInstance("inst3", "inst3Id");

        Resource prevResource = createResourceWithGroupsAndInstances(asList(group1, group2), asList(inst1, inst2, inst3));
        Resource currResource = createResourceWithInstances(inst3);

        when(groupsOperation.addGroups(eq(currResource), groupsToAddCaptor.capture())).thenReturn(Either.left(null));
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);

        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        List<GroupDefinition> groupsAdded = groupsToAddCaptor.getValue();
        Map<String, String> group1ExpectedMembers = cmptInstancesToGroups(inst3);
        Map<String, String> group2ExpectedMembers = emptyMap();
        assertThat(groupsAdded)
                .extracting("members")
                .containsExactlyInAnyOrder(group1ExpectedMembers, group2ExpectedMembers);
    }

    @Test
    public void groupsMembersShouldBeUpdatedAccordingToNewInstancesIds() {
        String prevInstance1Id = "inst1Id";
        String prevInstance2Id = "inst2Id";

        String currInstance1Id = "newInst1Id";
        String currInstance2Id = "newInst2Id";

        GroupDefinition group1 = createUserDefinedGroup("group1", prevInstance1Id, prevInstance2Id);
        ComponentInstance prevInst1 = createInstance("inst1", prevInstance1Id);
        ComponentInstance prevInst2 = createInstance("inst2", prevInstance2Id);
        Resource prevResource = createResourceWithGroupsAndInstances(singletonList(group1), asList(prevInst1, prevInst2));

        ComponentInstance currInst1 = createInstance("inst1", currInstance1Id);
        ComponentInstance currInst2 = createInstance("inst2", currInstance2Id);
        Resource currResource = createResourceWithInstances(currInst1, currInst2);

        when(groupsOperation.addGroups(eq(currResource), groupsToAddCaptor.capture())).thenReturn(Either.left(null));
        ActionStatus actionStatus = testInstance.mergeComponents(prevResource, currResource);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
        List<GroupDefinition> groupsAdded = groupsToAddCaptor.getValue();
        assertThat(groupsAdded)
                .extracting("members")
                .containsExactly(cmptInstancesToGroups(currInst1, currInst2));
    }

    private ComponentInstance createInstance(String name, String instId) {
        return new ComponentInstanceBuilder()
                    .setName(name)
                    .setUniqueId(instId)
                    .build();
    }

    private GroupDefinition createCsarGroup(String id) {
        return createGroup(id, CreatedFrom.CSAR);
    }

    private GroupDefinition createUserDefinedGroup(String id, String ... members) {
        return createGroup(id, CreatedFrom.UI, members);
    }

    private GroupDefinition createGroup(String id, CreatedFrom createdFrom, String ... members) {
        GroupDefinitionBuilder groupDefinitionBuilder = GroupDefinitionBuilder.create()
                .setUniqueId(id)
                .setCreatedFrom(createdFrom)
                .setName("name" + id)
                .setInvariantName("invName" + id);
        Stream.of(members).forEach(groupDefinitionBuilder::addMember);
        return groupDefinitionBuilder.build();
    }

    private Resource createResourceWithGroups(GroupDefinition ... groups) {
        return createResourceWithGroupsAndInstances(asList(groups), null);
    }

    private Resource createResourceWithInstances(ComponentInstance ... instances) {
        return createResourceWithGroupsAndInstances(null, asList(instances));
    }

    private Resource createResourceWithGroupsAndInstances(List<GroupDefinition> groups, List<ComponentInstance> instances) {
        ResourceBuilder resourceBuilder = new ResourceBuilder();
        if (groups != null) {
            groups.forEach(resourceBuilder::addGroup);
        }
        if (instances != null) {
            instances.forEach(resourceBuilder::addComponentInstance);
        }
        return resourceBuilder.build();
    }

    private Map<String, String> cmptInstancesToGroups(ComponentInstance ... instances) {
        return Stream.of(instances).collect(toMap(ComponentInstance::getName, ComponentInstance::getUniqueId));
    }

}