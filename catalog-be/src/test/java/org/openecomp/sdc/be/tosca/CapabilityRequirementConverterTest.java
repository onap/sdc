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

import com.google.common.collect.ImmutableList;
import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateCapability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CapabilityRequirementConverterTest {

    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private PropertyConvertor propertyConvertor;

    @InjectMocks
    private CapabilityRequirementConverter capabilityRequirementConverter;

    @Spy
    private ComponentInstance instanceProxy = new ComponentInstance();
    @Spy
    private ComponentInstance vfInstance = new ComponentInstance();
    @Spy
    private Component vfComponent = new Resource();
    @Spy
    private ComponentInstance vfcInstance = new ComponentInstance();
    @Spy
    private Component vfcComponent = new Resource();

    private static final String VF_INSTANCE_NAME = "vepdgtp4837vf0";
    private static final String VF_INSTANCE_NORMALIZED_NAME = "vepdgtp4837vf0";
    private static final String VF_INSTANCE_UNIQUE_ID = "5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0";
    private static final String VF_COMPONENT_NAME = "vepdgtp4837vf0";

    private static final String VFC_INSTANCE_UNIQUE_ID = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1";
    private static final String VFC_INSTANCE_NAME = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1";
    private static final String VFC_INSTANCE_NORMALIZED_NAME = "lb_1";
    private static final String VFC_COMPONENT_NAME = "clb_1";

    private static final String ORIGIN_NAME = "vepdgtp4837svc_proxy0";

    private static final String OWNER_ID = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693";
    private static final String DATA_DEFINITION_NAME = "name";

    private static final String GROUP_NAME = "groupName";

    private static final String PROPERTY_VALUE = "propValue";
    private static final String PROPERTY_NAME = "propName";

    @Before
    public void clean() {
        instanceProxy = Mockito.spy(new ComponentInstance());
        vfInstance = Mockito.spy(new ComponentInstance());
        vfcInstance = Mockito.spy(new ComponentInstance());
        vfComponent = Mockito.spy(new Resource());
        vfcComponent = Mockito.spy(new Resource());
    }

    @Test
    public void testGetInstance() {
        assertNotNull(CapabilityRequirementConverter.getInstance());
    }

    @Test
    public void testGetReducedPathByOwner() {
        List<String> pathList = new ArrayList<>();
        String uniqueId = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_2";

        String duplicate = "a77df84e-83eb-4edc-9823-d1f9f6549693.c79e9a4a-b172-4323-a2e2-1c48d6603241.lb_swu_direct_4_rvmi";
        pathList.add(VFC_INSTANCE_UNIQUE_ID);
        pathList.add(duplicate);
        pathList.add(duplicate);
        pathList.add(uniqueId);

        pathList.add(VF_INSTANCE_UNIQUE_ID);
        pathList.add("86ae128e-3d0a-41f7-a957-db1df9fe598c.9cc8f8ac-6869-4dd6-a6e1-74ecb9570dc4.vepdgtp4837svc_proxy0");

        List<String> reducedMap = new CapabilityRequirementConverter().getReducedPathByOwner(pathList, uniqueId);

        assertThat(reducedMap).isNotNull().doesNotContain(VFC_INSTANCE_UNIQUE_ID)
                .containsOnlyOnce(duplicate).hasSize(4);

        List<String> path = new ArrayList<>();
        capabilityRequirementConverter.getReducedPathByOwner(path, uniqueId);

        path.add("");
        capabilityRequirementConverter.getReducedPathByOwner(path, uniqueId);
        capabilityRequirementConverter.getReducedPathByOwner(path, "");
    }

    @Test
    public void testBuildName() {
        // endregion
        Map<String, List<CapabilityDefinition>> capabilities = newCapabilities("port");
        Component origin = getOrigin(vfInstance);
        Map<String, Component> cache = populateInstanceAndGetCache(origin, capabilities);

        List<CapabilityDefinition> flatList = capabilities.values().stream().flatMap(List::stream)
                .collect(Collectors.toList());
        flatList.forEach((CapabilityDefinition capabilityDefinition) -> {
            String name = capabilityRequirementConverter.buildCapabilityNameForComponentInstance(cache, instanceProxy,
                    capabilityDefinition);
            assertThat(name).isEqualTo(VF_INSTANCE_NAME + "." +
                    VFC_INSTANCE_NORMALIZED_NAME + "." + capabilityDefinition.getName());
        });
    }

    @Test
    public void getReducedPathByOwner() {
        List<String> pathList = new ArrayList<>();
        String uniqueId = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_2";

        String duplicate = "a77df84e-83eb-4edc-9823-d1f9f6549693.c79e9a4a-b172-4323-a2e2-1c48d6603241.lb_swu_direct_4_rvmi";
        pathList.add(VFC_INSTANCE_UNIQUE_ID);
        pathList.add(duplicate);
        pathList.add(duplicate);
        pathList.add(uniqueId);

        pathList.add(VF_INSTANCE_UNIQUE_ID);
        pathList.add("86ae128e-3d0a-41f7-a957-db1df9fe598c.9cc8f8ac-6869-4dd6-a6e1-74ecb9570dc4.vepdgtp4837svc_proxy0");

        List<String> reducedMap = new CapabilityRequirementConverter().getReducedPathByOwner(pathList, uniqueId);

        assertThat(reducedMap).isNotNull().doesNotContain(VFC_INSTANCE_UNIQUE_ID)
                .containsOnlyOnce(duplicate).hasSize(4);
    }

    @Test
    public void testConvertSubstitutionMappingCapabilities() {
        String capabilityName = "port";
        // endregion
        Map<String, List<CapabilityDefinition>> capabilities = newCapabilities(capabilityName);
        Component origin = getOrigin(vfcInstance);
        Map<String, Component> cache = populateInstanceAndGetCache(origin, capabilities);
        Either<Map<String, String[]>, ToscaError> res = capabilityRequirementConverter
                .convertSubstitutionMappingCapabilities(cache, origin);

        assertFalse(res.isRight());
        assertTrue(res.isLeft());
        Map<String, String[]> map = res.left().value();
        String[] substitution = map.get(VFC_INSTANCE_NORMALIZED_NAME + "." + capabilityName);
        assertEquals(VFC_INSTANCE_UNIQUE_ID, substitution[0]);
        assertEquals(capabilityName, substitution[1]);
    }

    @Test
    public void buildRequirementNameForComponentInstanceTest() {
        String capabilityName = "port";
        // endregion
        Map<String, List<CapabilityDefinition>> capabilities = newCapabilities(capabilityName);
        Component origin = getOrigin(vfcInstance);
        Map<String, Component> cache = populateInstanceAndGetCache(origin, capabilities);

        RequirementDefinition def = getRequirementDefinition();

        String resp = capabilityRequirementConverter.buildRequirementNameForComponentInstance(cache, vfcInstance, def);
        assertEquals(DATA_DEFINITION_NAME, resp);
    }

    @Test
    public void convertComponentInstanceCapabilitiesTest() {
        vfcInstance.setUniqueId(VFC_INSTANCE_UNIQUE_ID);
        vfcInstance.setName(VFC_INSTANCE_NAME);
        vfcInstance.setNormalizedName(VFC_INSTANCE_NORMALIZED_NAME);

        Component origin = getOrigin(vfcInstance);
        GroupDefinition group = new GroupDefinition();
        group.setUniqueId("5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0");
        group.setName(GROUP_NAME);
        origin.setGroups(ImmutableList.of(group));

        when(toscaOperationFacade.getToscaElement(any(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(origin));

        when(propertyConvertor.convertToToscaObject(any(ComponentInstanceProperty.class),
                eq(PROPERTY_VALUE), any(), eq(false))).thenReturn(PROPERTY_VALUE);

        String capabilityName = "port";
        // endregion
        Map<String, List<CapabilityDefinition>> capabilities = newCapabilities(capabilityName);
        vfcInstance.setCapabilities(capabilities);

        ToscaNodeTemplate nodeTemplate = new ToscaNodeTemplate();

        Either<ToscaNodeTemplate, ToscaError> resp = capabilityRequirementConverter.convertComponentInstanceCapabilities(vfcInstance,
                new HashMap<>(), nodeTemplate);

        assertTrue(resp.isLeft());
        assertFalse(resp.isRight());

        Map<String, ToscaTemplateCapability> convertCapabilities = resp.left().value().getCapabilities();
        assertNotNull(convertCapabilities.get(GROUP_NAME + "." + capabilityName));

        ToscaTemplateCapability templateCapability = convertCapabilities.get("groupName.port");
        assertEquals(PROPERTY_VALUE, templateCapability.getProperties().get(PROPERTY_NAME));
    }

    private Component getOrigin(ComponentInstance instance) {
        // region proxy
        Component proxyOrigin = new Resource();

        proxyOrigin.setName(ORIGIN_NAME);
        proxyOrigin.setComponentType(ComponentTypeEnum.RESOURCE);
        proxyOrigin.setComponentInstances(Collections.singletonList(instance));
        return proxyOrigin;
    }

    private Map<String, Component> populateInstanceAndGetCache(Component proxyOrigin,
                                                              Map<String, List<CapabilityDefinition>> capabilities) {
        when(instanceProxy.getActualComponentUid()).thenReturn("1");
        when(vfInstance.getActualComponentUid()).thenReturn("2");
        when(vfcInstance.getActualComponentUid()).thenReturn("3");

        // endregion
        // region vf+vfc
        vfInstance.setName(VF_INSTANCE_NAME);
        vfInstance.setNormalizedName(VF_INSTANCE_NORMALIZED_NAME);
        vfInstance.setUniqueId(VF_INSTANCE_UNIQUE_ID);

        vfComponent.setName(VF_COMPONENT_NAME); // origin
        vfComponent.setComponentInstances(Collections.singletonList(vfcInstance));

        vfcInstance.setUniqueId(VFC_INSTANCE_UNIQUE_ID);
        vfcInstance.setName(VFC_INSTANCE_NAME);
        vfcInstance.setNormalizedName(VFC_INSTANCE_NORMALIZED_NAME);

        vfcComponent.setName(VFC_COMPONENT_NAME);

        vfcComponent.setCapabilities(capabilities);
        instanceProxy.setCapabilities(capabilities);
        proxyOrigin.setCapabilities(capabilities);

        return Collections.unmodifiableMap(new HashMap<String, Component>() {
            {
                put("1", proxyOrigin);
                put("2", vfComponent);
                put("3", vfcComponent);
            }
        });
    }

    private RequirementDefinition getRequirementDefinition() {
        RequirementDataDefinition dataDefinition = new RequirementDataDefinition();
        dataDefinition.setName(DATA_DEFINITION_NAME);
        dataDefinition.setPreviousName("previousName");
        dataDefinition.setOwnerId(OWNER_ID);
        dataDefinition.setPath(getPath());

        return new RequirementDefinition(dataDefinition);
    }

    // generate stub capability
    private Map<String, List<CapabilityDefinition>> newCapabilities(String capabilityName) {
        Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
        List<CapabilityDefinition> list = new ArrayList<>();
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName(capabilityName);
        capabilityDefinition.setType("att.Node");
        capabilityDefinition.setExternal(true);
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty prop = new ComponentInstanceProperty();
        prop.setName(PROPERTY_NAME);
        prop.setValue(PROPERTY_VALUE);
        properties.add(prop);
        capabilityDefinition.setProperties(properties);
        capabilityDefinition.setOwnerId(OWNER_ID);

        List<String> pathList = getPath();
        capabilityDefinition.setPath(pathList);
        list.add(capabilityDefinition);
        capabilities.put(capabilityDefinition.getType(), list);

        return capabilities;
    }

    private List<String> getPath() {
        List<String> path = new ArrayList<>();
        path.add(VFC_INSTANCE_UNIQUE_ID);
        // pathList.add("a77df84e-83eb-4edc-9823-d1f9f6549693.c79e9a4a-b172-4323-a2e2-1c48d6603241.lb_swu_direct_4_rvmi");
        path.add(VF_INSTANCE_UNIQUE_ID);
        path.add("86ae128e-3d0a-41f7-a957-db1df9fe598c.9cc8f8ac-6869-4dd6-a6e1-74ecb9570dc4.vepdgtp4837svc_proxy0");

        return path;
    }

}
