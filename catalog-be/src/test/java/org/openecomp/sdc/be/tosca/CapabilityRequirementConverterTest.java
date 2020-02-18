/*
 * Copyright © 2016-2019 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.tosca;


import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mockit.Deencapsulation;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;

public class CapabilityRequirementConverterTest {

    private final CapabilityRequirementConverter capabiltyRequirementConvertor = Mockito
        .spy(new CapabilityRequirementConverter());
    private final static ComponentInstance instanceProxy = Mockito.spy(new ComponentInstance());
    private final static ComponentInstance vfInstance = Mockito.spy(new ComponentInstance());
    private final static Component vfComponent = Mockito.spy(new Resource());
    private final static ComponentInstance vfcInstance = Mockito.spy(new ComponentInstance());
    private final static Component vfcComponent = Mockito.spy(new Resource());

    private final static String COMPONENT_ID = "componentId";
    private final static String TO_INSTANCE_ID = "toInstanceId";
    private final static String TO_INSTANCE_NAME = "toInstanceName";
    private final static String FROM_INSTANCE_ID = "fromInstanceId";
    private final static String RELATION_ID = "relationId";
    private final static String CAPABILITY_OWNER_ID = "capabilityOwnerId";
    private final static String CAPABILITY_UID = "capabilityUid";
    private final static String CAPABILITY_NAME = "capabilityName";
    private final static String REQUIREMENT_OWNER_ID = "requirementOwnerId";
    private final static String REQUIREMENT_UID = "requirementUid";
    private final static String REQUIREMENT_NAME = "requirementName";
    private final static String RELATIONSHIP_TYPE = "relationshipType";

    private Component resource;
    private ComponentInstance toInstance;
    private ComponentInstance fromInstance;
    private CapabilityDataDefinition capability;
    private RequirementDataDefinition requirement;
    private RequirementCapabilityRelDef relation;

    private final Map<String, Component> componentCash = Collections.unmodifiableMap(new HashMap<String, Component>() {
        {
            put("1", resource);
            put("2", resource);
            put("3", resource);
        }
    });

    @Mock
    private ComponentInstancePropInput componentInstancePropInput;

    @Before
    public void setUpMock() {
        MockitoAnnotations.initMocks(this);
        createComponents();
    }

    @Test
    public void testGetInstance() {
        assertNotNull(CapabilityRequirementConverter.getInstance());
    }

    @Test
    public void testGetReducedPathByOwner() throws Exception {
        List<String> pathList = new ArrayList<>();
        String uniqueId = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_2";

        String exerpt = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1";
        String duplicate = "a77df84e-83eb-4edc-9823-d1f9f6549693.c79e9a4a-b172-4323-a2e2-1c48d6603241.lb_swu_direct_4_rvmi";
        pathList.add(exerpt);
        pathList.add(duplicate);
        pathList.add(duplicate);
        pathList.add(uniqueId);

        pathList.add("5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0");
        pathList.add("86ae128e-3d0a-41f7-a957-db1df9fe598c.9cc8f8ac-6869-4dd6-a6e1-74ecb9570dc4.vepdgtp4837svc_proxy0");

        List<String> reducedMap = new CapabilityRequirementConverter().getReducedPathByOwner(pathList, uniqueId);

        assertThat(reducedMap).isNotNull().doesNotContain(exerpt).containsOnlyOnce(duplicate).hasSize(4);

        List<String> path = new ArrayList<String>();

        capabiltyRequirementConvertor.getReducedPathByOwner(path, uniqueId);

        path.add("");
        capabiltyRequirementConvertor.getReducedPathByOwner(path, uniqueId);
        capabiltyRequirementConvertor.getReducedPathByOwner(path, "");
    }

    // generate stub capability
    private Map<String, List<CapabilityDefinition>> newCapabilities(String capabilityName) {
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> list = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName(capabilityName);
        capabilityDefinition.setType("att.Node");
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty prop = new ComponentInstanceProperty();
        prop.setValue("value");
        properties.add(prop);
        capabilityDefinition.setProperties(properties);
        List<String> pathList = new ArrayList<>();

        capabilityDefinition.setOwnerId("41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693");
        pathList.add("41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1");
        pathList.add("5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0");
        pathList.add("86ae128e-3d0a-41f7-a957-db1df9fe598c.9cc8f8ac-6869-4dd6-a6e1-74ecb9570dc4.vepdgtp4837svc_proxy0");

        capabilityDefinition.setPath(pathList);
        list.add(capabilityDefinition);
        capabilities.put(capabilityDefinition.getType(), list);

        return capabilities;
    }

    @Test
    public void testBuildName() {
        doReturn("1").when(instanceProxy).getActualComponentUid();
        doReturn("2").when(vfInstance).getActualComponentUid();
        doReturn("3").when(vfcInstance).getActualComponentUid();
        // region proxy
        Component proxyOrigin = new Resource();

        proxyOrigin.setName("vepdgtp4837svc_proxy0");
        proxyOrigin.setComponentType(ComponentTypeEnum.RESOURCE);
        proxyOrigin.setComponentInstances(asList(vfInstance));

        // endregion
        // region vf+vfc
        vfInstance.setName("vepdgtp4837vf0");
        vfInstance.setNormalizedName("vepdgtp4837vf0");
        vfInstance.setUniqueId(
            "5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0");
        vfComponent.setName("vepdgtp4837vf0"); // origin
        vfComponent.setComponentInstances(Arrays.asList(vfcInstance));
        vfcInstance.setUniqueId("41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1");
        vfcInstance.setName("lb_1");
        vfcInstance.setNormalizedName("lb_1");
        vfcInstance.setName("41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1");
        vfcComponent.setName("lb_1");
        // endregion
        Map<String, List<CapabilityDefinition>> capabilities = newCapabilities("port");
        vfcComponent.setCapabilities(capabilities);
        Map<Component, ComponentInstance> map = Collections
            .unmodifiableMap(new HashMap<Component, ComponentInstance>() {
                {
                    put(proxyOrigin, null);
                    put(vfComponent, vfInstance);
                    put(vfcComponent, vfcInstance);
                }
            });
        Map<String, Component> cache = Collections.unmodifiableMap(new HashMap<String, Component>() {
            {
                put("1", proxyOrigin);
                put("2", vfComponent);
                put("3", vfcComponent);
            }
        });
        instanceProxy.setCapabilities(capabilities);
        proxyOrigin.setCapabilities(capabilities);
        List<CapabilityDefinition> flatList = capabilities.values().stream().flatMap(List::stream)
            .collect(Collectors.toList());
        flatList.stream().forEach((CapabilityDefinition capabilityDefinition) -> {
            String name = capabiltyRequirementConvertor.buildCapabilityNameForComponentInstance(cache, instanceProxy,
                capabilityDefinition);
            System.out.println("built name -> " + name);
            assertThat(name).isEqualTo("vepdgtp4837vf0.lb_1." + capabilityDefinition.getName());
        });
    }

    @Test
    public void getReducedPathByOwner() throws Exception {
        List<String> pathList = new ArrayList<>();
        String uniqueId = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_2";

        String exerpt = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1";
        String duplicate = "a77df84e-83eb-4edc-9823-d1f9f6549693.c79e9a4a-b172-4323-a2e2-1c48d6603241.lb_swu_direct_4_rvmi";
        pathList.add(exerpt);
        pathList.add(duplicate);
        pathList.add(duplicate);
        pathList.add(uniqueId);

        pathList.add("5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0");
        pathList.add("86ae128e-3d0a-41f7-a957-db1df9fe598c.9cc8f8ac-6869-4dd6-a6e1-74ecb9570dc4.vepdgtp4837svc_proxy0");

        List<String> reducedMap = new CapabilityRequirementConverter().getReducedPathByOwner(pathList, uniqueId);

        assertThat(reducedMap).isNotNull().doesNotContain(exerpt).containsOnlyOnce(duplicate).hasSize(4);
    }

    @Test
    public void testConvertSubstitutionMappingCapabilities() {
        resource.setGroups(getGroupDefinition(CAPABILITY_OWNER_ID));
        final Either<SubstitutionMapping, ToscaError> result = Deencapsulation.invoke(capabiltyRequirementConvertor,
            "convertSubstitutionMappingCapabilities", componentCash, resource);
        assertTrue(result.isLeft());
    }

    @Test
    public void testConvertSubstitutionMappingCapabilitiesFail() {
        resource.setGroups(new ArrayList<>());
        final Either<SubstitutionMapping, ToscaError> result = Deencapsulation.invoke(capabiltyRequirementConvertor,
            "convertSubstitutionMappingCapabilities", componentCash, resource);
        assertTrue(result.isRight());
    }

    @Test
    public void testConvertSubstitutionMappingRequirements() {
        resource.setGroups(getGroupDefinition(REQUIREMENT_OWNER_ID));
        final Either<SubstitutionMapping, ToscaError> result = Deencapsulation.invoke(capabiltyRequirementConvertor,
            "convertSubstitutionMappingRequirementsAsMap", componentCash, resource);
        assertTrue(result.isLeft());
    }

    @Test
    public void testConvertSubstitutionMappingRequirementsFail() {
        resource.setGroups(new ArrayList<>());
        final Either<SubstitutionMapping, ToscaError> result = Deencapsulation.invoke(capabiltyRequirementConvertor,
            "convertSubstitutionMappingRequirementsAsMap", componentCash, resource);
        assertTrue(result.isRight());
    }

    private List<GroupDefinition> getGroupDefinition(final String groupId) {
        final GroupDefinition group = new GroupDefinition();
        final List<GroupDefinition> groups = new ArrayList<>();
        group.setUniqueId(groupId);
        group.setType("org.openecomp.groups.VfModule");
        group.setArtifacts(Collections.singletonList("artifact"));
        groups.add(group);

        return groups;
    }

    private void createComponents() {
        createRelation();
        createInstances();
        createResource();
    }

    private void createRelation() {

        relation = new RequirementCapabilityRelDef();
        CapabilityRequirementRelationship relationship = new CapabilityRequirementRelationship();
        RelationshipInfo relationInfo = new RelationshipInfo();
        relationInfo.setId(RELATION_ID);
        relationship.setRelation(relationInfo);

        relation.setRelationships(Lists.newArrayList(relationship));
        relation.setToNode(TO_INSTANCE_ID);
        relation.setFromNode(REQUIREMENT_OWNER_ID);

        relationInfo.setCapabilityOwnerId(CAPABILITY_OWNER_ID);
        relationInfo.setCapabilityUid(CAPABILITY_UID);
        relationInfo.setCapability(CAPABILITY_NAME);
        relationInfo.setRequirementOwnerId(REQUIREMENT_OWNER_ID);
        relationInfo.setRequirementUid(REQUIREMENT_UID);
        relationInfo.setRequirement(REQUIREMENT_NAME);
        final RelationshipImpl relationshipImpl = new RelationshipImpl();
        relationshipImpl.setType(RELATIONSHIP_TYPE);
        relationInfo.setRelationships(relationshipImpl);
    }

    private void createInstances() {
        toInstance = new ComponentInstance();
        toInstance.setUniqueId(TO_INSTANCE_ID);
        toInstance.setName(TO_INSTANCE_NAME);

        fromInstance = new ComponentInstance();
        fromInstance.setUniqueId(FROM_INSTANCE_ID);

        capability = new CapabilityDataDefinition();
        capability.setOwnerId(CAPABILITY_OWNER_ID);
        capability.setUniqueId(CAPABILITY_UID);
        capability.setName(CAPABILITY_NAME);

        final Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        final CapabilityDefinition capabilityDefinition = new CapabilityDefinition(capability);
        final ArrayList<ComponentInstanceProperty> properties = new ArrayList<>();
        properties.add(componentInstancePropInput);
        capabilityDefinition.setProperties(properties);
        capabilityDefinition.setPath(Collections.singletonList(CAPABILITY_OWNER_ID));

        capabilities.put(capability.getName(), Lists.newArrayList(capabilityDefinition));

        requirement = new RequirementDataDefinition();
        requirement.setOwnerId(REQUIREMENT_OWNER_ID);
        requirement.setUniqueId(REQUIREMENT_UID);
        requirement.setName(REQUIREMENT_NAME);
        requirement.setRelationship(RELATIONSHIP_TYPE);
        requirement.setPath(Collections.singletonList(REQUIREMENT_OWNER_ID));

        final Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
        requirements.put(requirement.getCapability(), Lists.newArrayList(new RequirementDefinition(requirement)));

        toInstance.setCapabilities(capabilities);
        fromInstance.setRequirements(requirements);
    }

    private void createResource() {
        resource = new Resource();
        resource.setUniqueId(COMPONENT_ID);
        resource.setComponentInstancesRelations(Lists.newArrayList(relation));
        resource.setComponentInstances(Lists.newArrayList(toInstance, fromInstance));
        resource.setCapabilities(toInstance.getCapabilities());
        resource.setRequirements(fromInstance.getRequirements());
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        resource.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
    }

}
