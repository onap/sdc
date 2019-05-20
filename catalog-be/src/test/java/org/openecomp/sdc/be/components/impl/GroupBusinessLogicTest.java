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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.policy.PolicyTargetsUpdateHandler;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
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
import fj.data.Either;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyObject;
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
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private PropertyOperation propertyOperation;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    PolicyTargetsUpdateHandler policyTargetsUpdateHandler;

    private final static ServletContext servletContext = Mockito.mock(ServletContext.class);
    private final static ConfigurationManager configurationManager = Mockito.mock(ConfigurationManager.class);
    private final static Configuration configuration = Mockito.mock(Configuration.class);
    static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");

    @Test
    public void testCreateGroups_NoDataType() {
        Either<List<GroupDefinition>, ResponseFormat> result;
        Component component = new Resource();
        List<GroupDefinition> groupDefinitions = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinitions.add(groupDefinition);
        when(dataTypeCache.getAll()).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        result = test.createGroups(component, groupDefinitions, true);
        Assert.assertTrue(result.isRight());
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
        Assert.assertTrue(result.isLeft());
    }

    @Test
    public void testValidUpdateVfGrpNameOnGraph() {
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
        Assert.assertTrue(result.isLeft());
    }

    @Test
    public void testValidAndUpdateGrpInstancePropValues_fail() {
        Either<GroupInstance, ResponseFormat> result;
        String componentId = "id";
        String instanceId = "id";
        GroupInstance oldGroupInstance = new GroupInstance();
        List<GroupInstanceProperty> newProperties = new ArrayList<>();
        List<PropertyDataDefinition> properties = new LinkedList<>();
        properties.add(new PropertyDataDefinition());
        oldGroupInstance.setProperties(properties);
        result = test.validateAndUpdateGroupInstancePropertyValues(componentId, instanceId, oldGroupInstance, newProperties);
        Assert.assertTrue(result.isRight());
    }

    @Test
    public void testCreateGroup() {
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
        when(dataTypeCache.getAll()).thenReturn(Either.left(map));
        when(accessValidations.validateUserCanWorkOnComponent(componentId, compTypeEnum, userId, "CreateGroup")).thenReturn(component);

        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        configurationManager.setConfiguration(new Configuration());
        configurationManager.getConfiguration().setExcludedGroupTypesMapping(excludedGroupTypesMap);

        List<PropertyDefinition> properties = asList(
                buildProperty("network_collection_type", "l3-network", "network collection type, defined with default value"));
        groupTypeDefinition.setProperties(properties);
        when(groupTypeOperation.getLatestGroupTypeByType(grpType, false)).thenReturn(Either.left(groupTypeDefinition));
        when(toscaOperationFacade.canAddGroups(componentId)).thenReturn(true);
        when(groupTypeOperation.getLatestGroupTypeByType(grpType, true)).thenReturn(Either.left(groupTypeDefinition));
        when(propertyOperation.checkInnerType(any(PropertyDefinition.class))).thenReturn(Either.left("ok"));
        when(propertyOperation.validateAndUpdatePropertyValue("string", null, "ok", map)).thenReturn(Either.left(component));
        when(groupsOperation.addGroups(any(Resource.class), any())).thenReturn(Either.left(groupDefList));
        when(groupsOperation.addCalculatedCapabilitiesWithProperties(anyString(), anyMap(), anyMap())).thenReturn(StorageOperationStatus.OK);
        result = test.createGroup(componentId, compTypeEnum, grpType, userId);
        assertThat(result.getClass().isInstance(GroupDefinition.class));
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

        when(janusGraphDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
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