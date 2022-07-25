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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.policy.PolicyTargetsUpdateHandler;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

@ExtendWith(MockitoExtension.class)
class GroupBusinessLogicTest {

    static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
        "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
    @InjectMocks
    private GroupBusinessLogic test;
    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private IGroupTypeOperation groupTypeOperation;
    @Mock
    private GroupsOperation groupsOperation;
    @Mock
    private AccessValidations accessValidations;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private PropertyOperation propertyOperation;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private PolicyTargetsUpdateHandler policyTargetsUpdateHandler;

    @BeforeEach
    public void setUp() throws Exception {
        test.setApplicationDataTypeCache(applicationDataTypeCache);
        test.setToscaOperationFacade(toscaOperationFacade);
        test.setPropertyOperation(propertyOperation);
        test.setComponentsUtils(componentsUtils);
        test.setJanusGraphDao(janusGraphDao);
    }

    @Test
    void testCreateGroups_NoDataType() {
        Either<List<GroupDefinition>, ResponseFormat> result;
        Component component = new Resource();
        List<GroupDefinition> groupDefinitions = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinitions.add(groupDefinition);
        result = test.createGroups(component, groupDefinitions, true);
        assertThat(result.isRight()).isTrue();
    }

    @Test
    void testCreateGroups() {
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
        when(groupTypeOperation.getLatestGroupTypeByType(Constants.DEFAULT_GROUP_VF_MODULE, component.getModel(), true))
            .thenReturn(Either.left(groupTypeDefinition));
        when(groupsOperation.createGroups(any(Component.class), anyMap())).thenReturn(Either.left(groupDefinitions));
        when(groupsOperation.addCalculatedCapabilitiesWithProperties(anyString(), anyMap(), anyMap())).thenReturn(StorageOperationStatus.OK);
        result = test.createGroups(component, groupDefinitions, true);
        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void testValidUpdateVfGrpNameOnGraph() {
        Either<List<GroupDefinition>, ResponseFormat> result;
        Component component = new Resource();
        component.setSystemName("name");

        List<GroupDefinition> groupDefinitions = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setName("grp_one-1. ::module-1");
        groupDefinition.setType(Constants.DEFAULT_GROUP_VF_MODULE);
        groupDefinition.setDescription("desc");
        groupDefinitions.add(groupDefinition);

        result = test.validateUpdateVfGroupNamesOnGraph(groupDefinitions, component);
        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void testValidAndUpdateGrpInstancePropValues_fail() {
        String componentId = "id";
        String instanceId = "id";
        GroupInstance oldGroupInstance = new GroupInstance();
        List<GroupInstanceProperty> newProperties = new ArrayList<>();
        List<PropertyDataDefinition> properties = new LinkedList<>();
        properties.add(new PropertyDataDefinition());
        oldGroupInstance.setProperties(properties);
        when(toscaOperationFacade.getToscaElement(componentId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(new Resource()));
        final ComponentException actualException = assertThrows(ComponentException.class,
            () -> test.validateAndUpdateGroupInstancePropertyValues(componentId, instanceId, oldGroupInstance, newProperties));
        assertEquals(ActionStatus.GENERAL_ERROR, actualException.getActionStatus());
    }

    @Test
    void testCreateGroup() {
        GroupDefinition result;
        String componentId = "id";
        String grpType = "grpType";
        String userId = "userId";
        ComponentTypeEnum compTypeEnum = ComponentTypeEnum.RESOURCE;
        Component component = new Resource();
        component.setName("name");
        component.setUniqueId(componentId);
        component.setToscaType(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue());
        List<GroupDefinition> groupDefList = new ArrayList<>();
        Map<String, Set<String>> excludedGroupTypesMap = new HashMap<>();
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition();
        Map<String, DataTypeDefinition> map = new HashMap<>();
        when(accessValidations.validateUserCanWorkOnComponent(componentId, compTypeEnum, userId, "CreateGroup")).thenReturn(component);

        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        configurationManager.setConfiguration(new Configuration());
        configurationManager.getConfiguration().setExcludedGroupTypesMapping(excludedGroupTypesMap);

        List<PropertyDefinition> properties = asList(
            buildProperty("network_collection_type", "l3-network", "network collection type, defined with default value"));
        groupTypeDefinition.setProperties(properties);
        when(groupTypeOperation.getLatestGroupTypeByType(grpType, component.getModel(), false)).thenReturn(Either.left(groupTypeDefinition));
        when(toscaOperationFacade.canAddGroups(componentId)).thenReturn(true);
        when(groupTypeOperation.getLatestGroupTypeByType(grpType, component.getModel(), true)).thenReturn(Either.left(groupTypeDefinition));
        when(propertyOperation.checkInnerType(any(PropertyDefinition.class))).thenReturn(Either.left("ok"));
        when(propertyOperation.validateAndUpdatePropertyValue("string", null, "ok", map)).thenReturn(Either.left(component));
        when(groupsOperation.addGroups(any(Resource.class), any())).thenReturn(Either.left(groupDefList));
        when(groupsOperation.addCalculatedCapabilitiesWithProperties(anyString(), anyMap(), anyMap())).thenReturn(StorageOperationStatus.OK);
        result = test.createGroup(componentId, compTypeEnum, grpType, userId);
        assertThat(result.getClass()).isAssignableFrom(GroupDefinition.class);
    }

    private PropertyDefinition buildProperty(String name, String defaultValue, String description) {
        PropertyDefinition property = new PropertyDefinition();
        property.setName(name);
        property.setDefaultValue(defaultValue);
        property.setRequired(true);
        property.setDescription(description);
        property.setType(ToscaType.STRING.name().toLowerCase());
        return property;
    }

    @Test
    void testUpdateGroup() throws Exception {

        Component component = new Resource();
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
        Assertions.assertEquals(Gdefinition, updatedGroup);
    }

    @Test
    void testUpdateGroup_Invalidname() throws Exception {

        Component component = new Resource();
        GroupDefinition updatedGroup = new GroupDefinition();
        List<GroupDefinition> grpdefList = new ArrayList<>();
        updatedGroup.setName("GRP~01");
        updatedGroup.setUniqueId("GRP.01");
        grpdefList.add(updatedGroup);
        component.setUniqueId("GRP.01");
        component.setGroups(grpdefList);
        when(accessValidations.validateUserCanWorkOnComponent("compid", ComponentTypeEnum.SERVICE, "USR01", "UpdateGroup")).thenReturn(component);
        assertThrows(ComponentException.class, () -> {
            GroupDefinition Gdefinition = test.updateGroup("compid", ComponentTypeEnum.SERVICE, "GRP.01",
                "USR01", updatedGroup);
        });
    }

    @Test
    void testDeleteGroup_exception() throws Exception {

        Component component = new Resource();
        GroupDefinition updatedGroup = new GroupDefinition();
        List<GroupDefinition> grpdefList = new ArrayList<>();
        updatedGroup.setName("GRP~01");
        updatedGroup.setUniqueId("GRP.01");
        grpdefList.add(updatedGroup);
        component.setUniqueId("GRP.01");
        component.setGroups(grpdefList);
        when(accessValidations.validateUserCanWorkOnComponent("compid", ComponentTypeEnum.SERVICE, "USR01", "DeleteGroup")).thenReturn(component);
        when(groupsOperation.deleteGroups(anyObject(), anyList())).thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));

        when(janusGraphDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        assertThrows(ComponentException.class, () -> {
            GroupDefinition Gdefinition = test.deleteGroup("compid", ComponentTypeEnum.SERVICE, "GRP.01",
                "USR01");
        });
    }

    @Test
    void testDeleteGroup() {

        Component component = new Resource();
        List<GroupDefinition> groupDefList = new ArrayList<>();
        GroupDefinition updatedGroup = new GroupDefinition();
        updatedGroup.setName("GRP~01");
        updatedGroup.setUniqueId("GRP.01");
        groupDefList.add(updatedGroup);
        component.setUniqueId("GRP.01");
        component.setGroups(groupDefList);
        List<GroupDefinition> groupDefListCopy = new ArrayList<>();
        groupDefListCopy.add(updatedGroup);
        when(accessValidations.validateUserCanWorkOnComponent("compid", ComponentTypeEnum.SERVICE, "USR01", "DeleteGroup")).thenReturn(component);
        when(groupsOperation.deleteGroups(anyObject(), anyList())).thenReturn(Either.left(groupDefListCopy));
        when(groupsOperation.deleteCalculatedCapabilitiesWithProperties(anyString(), anyObject())).thenReturn(StorageOperationStatus.OK);
        when(policyTargetsUpdateHandler.removePoliciesTargets(anyObject(), anyString(), anyObject())).thenReturn(ActionStatus.OK);

        GroupDefinition Gdefinition = test.deleteGroup("compid", ComponentTypeEnum.SERVICE, "GRP.01",
            "USR01");
        Assertions.assertEquals(Gdefinition, updatedGroup);
    }

    @Test
    void testValidateGenerateVfModuleGroupNames_pass() {

        final List<ArtifactTemplateInfo> allGroups = new ArrayList<>();
        final ArtifactTemplateInfo artifactTemplateInfo1 = new ArtifactTemplateInfo();
        final ArtifactTemplateInfo artifactTemplateInfo2 = new ArtifactTemplateInfo();
        artifactTemplateInfo1.setGroupName("ArtTmpInfoName1");
        artifactTemplateInfo1.setDescription("ArtTmpInfoDesc1");
        artifactTemplateInfo2.setGroupName("ArtTmpInfoName2");
        artifactTemplateInfo2.setDescription("ArtTmpInfoDesc2");
        allGroups.add(artifactTemplateInfo1);
        allGroups.add(artifactTemplateInfo2);

        final Either<Boolean, ResponseFormat> result = test.validateGenerateVfModuleGroupNames(allGroups, "resourceSystemName", 0);

        Assertions.assertEquals(2, allGroups.size());
        Assertions.assertEquals("resourceSystemName..ArtTmpInfoDesc1..module-0", allGroups.get(0).getGroupName());
        Assertions.assertEquals("resourceSystemName..ArtTmpInfoDesc2..module-1", allGroups.get(1).getGroupName());
        Assertions.assertTrue(result.left().value());
    }

    @Test
    void testValidateGenerateVfModuleGroupNames_emptyArtifactDescriptionFail() {

        final List<ArtifactTemplateInfo> allGroups = new ArrayList<>();
        allGroups.add(new ArtifactTemplateInfo());
        final ResponseFormat expectedResponse = new ResponseFormat(400);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_VF_MODULE_NAME)).thenReturn(new ResponseFormat(400));

        final Either<Boolean, ResponseFormat> result = test.validateGenerateVfModuleGroupNames(allGroups, "resourceSystemName", 0);

        Assertions.assertEquals(1, allGroups.size());
        Assertions.assertNull(allGroups.get(0).getGroupName());
        Assertions.assertEquals(expectedResponse.getStatus(), result.right().value().getStatus());
     }

    @Test
    void testValidateGenerateVfModuleGroupNames_invalidArtifactDescriptionFail() {

        final List<ArtifactTemplateInfo> allGroups = new ArrayList<>();
        final ArtifactTemplateInfo artifactTemplateInfo1 = new ArtifactTemplateInfo();
        artifactTemplateInfo1.setGroupName("ArtTmpInfoName1");
        artifactTemplateInfo1.setDescription("!ArtTmpInfoDesc1!");
        allGroups.add(artifactTemplateInfo1);
        final ResponseFormat expectedResponse = new ResponseFormat(400);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_VF_MODULE_NAME)).thenReturn(new ResponseFormat(400));

        final Either<Boolean, ResponseFormat> result = test.validateGenerateVfModuleGroupNames(allGroups, "resourceSystemName", 0);

        Assertions.assertEquals(1, allGroups.size());
        Assertions.assertEquals("ArtTmpInfoName1", allGroups.get(0).getGroupName());
        Assertions.assertEquals(expectedResponse.getStatus(), result.right().value().getStatus());
    }

    @Test
    void testGetNextVfModuleNameCounter_groupNull() {

        final int resultCollection = test.getNextVfModuleNameCounter((Collection <GroupDefinition>) null);
        final int resultMap = test.getNextVfModuleNameCounter((Map<String, GroupDefinition>) null);

        Assertions.assertEquals(0, resultCollection);
        Assertions.assertEquals(0, resultMap);
    }

    @Test
    void testGetNextVfModuleNameCounter_groupEmpty() {

        final int resultCollection = test.getNextVfModuleNameCounter(new ArrayList<>());
        final int resultMap = test.getNextVfModuleNameCounter(new HashMap<>());

        Assertions.assertEquals(0, resultCollection);
        Assertions.assertEquals(0, resultMap);
    }

    @Test
    void testGetNextVfModuleNameCounter_takesCollectionPass() {

        final Collection<GroupDefinition> group = new ArrayList<>();
        final GroupDefinition groupDefinition1 = new GroupDefinition();
        final GroupDefinition groupDefinition2 = new GroupDefinition();
        final GroupDefinition groupDefinition3 = new GroupDefinition();
        groupDefinition1.setName("resourceSystemName..ArtTmpInfoDesc1..module-0");
        groupDefinition2.setName("resourceSystemName..ArtTmpInfoDesc1..module-9");
        groupDefinition3.setName("resourceSystemName..ArtTmpInfoDesc2..module-1");
        group.add(groupDefinition1);
        group.add(groupDefinition2);
        group.add(groupDefinition3);

        final int result = test.getNextVfModuleNameCounter(group);

        Assertions.assertEquals(10, result);
    }

    @Test
    void testGetNextVfModuleNameCounter_takesMapPass() {

        final Map<String, GroupDefinition> group = new HashMap<>();
        final GroupDefinition groupDefinition1 = new GroupDefinition();
        final GroupDefinition groupDefinition2 = new GroupDefinition();
        final GroupDefinition groupDefinition3 = new GroupDefinition();
        groupDefinition1.setName("resourceSystemName..ArtTmpInfoDesc1..module-0");
        groupDefinition2.setName("resourceSystemName..ArtTmpInfoDesc1..module-9");
        groupDefinition3.setName("resourceSystemName..ArtTmpInfoDesc2..module-1");

        group.put("first", groupDefinition1);
        group.put("second", groupDefinition2);
        group.put("third", groupDefinition3);

        final int result = test.getNextVfModuleNameCounter(group);

        Assertions.assertEquals(10, result);
    }
}