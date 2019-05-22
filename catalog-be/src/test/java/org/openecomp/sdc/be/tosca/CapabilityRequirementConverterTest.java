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

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;


import fj.data.Either;
import mockit.Deencapsulation;
import org.openecomp.sdc.be.tosca.model.ToscaRequirement;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public class CapabilityRequirementConverterTest {

	@InjectMocks
	CapabilityRequirementConverter testSubject;

	@Mock
	ToscaOperationFacade toscaOperationFacade;

	CapabilityRequirementConverter capabiltyRequirementConvertor = Mockito.spy(new CapabilityRequirementConverter());
	ComponentInstance instanceProxy = Mockito.spy(new ComponentInstance());
	ComponentInstance vfInstance = Mockito.spy(new ComponentInstance());
	Component vfComponent = Mockito.spy(new Resource());
	ComponentInstance vfcInstance = Mockito.spy(new ComponentInstance());
	Component vfcComponent = Mockito.spy(new Resource());

	@Before
	public void setUpMock() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetInstance() {
		CapabilityRequirementConverter.getInstance();
	}

	@Test
	public void testConvertComponentInstanceCapabilties() {
		Map<String, List<CapabilityDefinition>> capabilities = newCapabilities("port");
		vfInstance.setCapabilities(capabilities);
		ToscaNodeTemplate nodeTemplate = new ToscaNodeTemplate();
		Map<String, DataTypeDefinition> testDataTypes = new HashMap<String, DataTypeDefinition>();

		capabilities.get("att.Node").clear();
		testSubject.convertComponentInstanceCapabilities(vfInstance, testDataTypes, nodeTemplate);

		capabilities = newCapabilities("port");
		vfInstance.setCapabilities(capabilities);
		vfInstance.setComponentUid("uid");

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		testSubject.convertComponentInstanceCapabilities(vfInstance, testDataTypes, nodeTemplate);

	}

	@Test
	public void testConvertComponentInstanceCapabilties_1() {
		Map<String, List<CapabilityDefinition>> capabilities = newCapabilities("port");
		ToscaNodeTemplate nodeTemplate = new ToscaNodeTemplate();
		Map<String, DataTypeDefinition> testDataTypes = new HashMap<String, DataTypeDefinition>();

		vfInstance.setComponentUid("uid");

		vfInstance.setCapabilities(capabilities);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		testSubject.convertComponentInstanceCapabilities(vfInstance, testDataTypes, nodeTemplate);

	}



	@Test
	public void testConvertSubstitutionMappingRequirements() {
		Map<String, Component> componentsCache = new HashMap<>();
		SubstitutionMapping substitution = new SubstitutionMapping();
		Map<String, List<RequirementDefinition>> requirementsMap = new HashMap<String, List<RequirementDefinition>>();
		List<RequirementDefinition> requirementsArray = new ArrayList<RequirementDefinition>();
		RequirementDefinition definition = new RequirementDefinition();
		definition.setOwnerId("id");
		definition.setName("name");
		definition.setParentName("parentName");
		List<String> path = new ArrayList<>();
		path.add("path1");
		path.add("path2");
		definition.setPath(path);
		requirementsArray.add(definition);
		requirementsMap.put("key", requirementsArray);
		List<ComponentInstance> instances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("uid");
		instances.add(instance);
		vfComponent.setRequirements(requirementsMap);
		vfComponent.setComponentInstances(instances);

		testSubject.convertSubstitutionMappingRequirements(componentsCache, vfComponent, substitution);
	}

	@Test
	public void testConvertSubstitutionMappingRequirements_1() {
		Map<String, Component> componentsCache = new HashMap<>();
		SubstitutionMapping substitution = new SubstitutionMapping();
		Map<String, List<RequirementDefinition>> requirementsMap = new HashMap<String, List<RequirementDefinition>>();
		List<RequirementDefinition> requirementsArray = new ArrayList<RequirementDefinition>();
		RequirementDefinition definition = new RequirementDefinition();
		definition.setName("name");
		definition.setParentName("parentName");
		List<String> path = new ArrayList<>();
		path.add("path1.");
		path.add("id");
		definition.setPath(path);
		requirementsArray.add(definition);
		requirementsMap.put("key", requirementsArray);
		List<ComponentInstance> instances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		instance.setComponentUid("id");
		instances.add(instance);
		vfComponent.setRequirements(requirementsMap);
		vfComponent.setComponentInstances(instances);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(vfcComponent));

		testSubject.convertSubstitutionMappingRequirements(componentsCache, vfComponent, substitution);
	}

	@Test
	public void testConvertSubstitutionMappingRequirementsAsMap() {
		Map<String, Component> componentsCache = new HashMap<>();
		vfComponent.setRequirements(null);

		Deencapsulation.invoke(testSubject, "convertSubstitutionMappingRequirementsAsMap", componentsCache,
				vfComponent);
	}

	@Test
	public void testBuildAddSubstitutionMappingsRequirements() {
		Map<String, Component> componentsCache = new HashMap<>();
		Map<String, List<RequirementDefinition>> requirementsMap = new HashMap<String, List<RequirementDefinition>>();
		List<RequirementDefinition> requirementsArray = new ArrayList<RequirementDefinition>();
		RequirementDefinition definition = new RequirementDefinition();
		definition.setOwnerId("id");
		definition.setName("name");
		definition.setParentName("parentName");
		List<String> path = new ArrayList<>();
		path.add("path1");
		path.add("path2");
		definition.setPath(path);
		requirementsArray.add(definition);
		requirementsMap.put("key", requirementsArray);
		vfComponent.setRequirements(requirementsMap);
		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Deencapsulation.invoke(testSubject, "buildAddSubstitutionMappingsRequirements", componentsCache, vfComponent,
				requirementsMap);
	}

	@Test
	public void testBuildAddSubstitutionMappingsCapabilities() {
		Map<String, Component> componentsCache = new HashMap<>();
		Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();

		Deencapsulation.invoke(testSubject, "buildAddSubstitutionMappingsCapabilities", componentsCache, vfComponent,
				capabilities);
	}

	@Test
	public void testBuildAddSubstitutionMappingsCapabilities_1() {
		Map<String, Component> componentsCache = new HashMap<>();
		Map<String, List<CapabilityDefinition>> capabilitiesMap = new HashMap<String, List<CapabilityDefinition>>();
		List<CapabilityDefinition> capabilitiesArray = new ArrayList<CapabilityDefinition>();
		CapabilityDefinition definition = new CapabilityDefinition();
		definition.setOwnerId("id");
		definition.setName("name");
		definition.setParentName("parentName");
		List<String> path = new ArrayList<>();
		path.add("path1");
		path.add("path2");
		definition.setPath(path);
		capabilitiesArray.add(definition);
		capabilitiesMap.put("key", capabilitiesArray);
		vfComponent.setCapabilities(capabilitiesMap);
		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Deencapsulation.invoke(testSubject, "buildAddSubstitutionMappingsCapabilities", componentsCache, vfComponent,
				capabilitiesMap);
	}

	@Test
	public void testConvertProxyCapabilities() {
		Map<String, Component> componentsCache = new HashMap<>();
		Map<String, DataTypeDefinition> dataTypes = new HashMap<>();

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		testSubject.convertProxyCapabilities(componentsCache, instance, dataTypes);
	}

	@Test
	public void testConvertProxyRequirementsNoRequirements() {
		Map<String, Component> componentsCache = new HashMap<>();

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		instance.setComponentUid("componentUid");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		List<Map<String, ToscaRequirement>> proxyRequirements =
				testSubject.convertProxyRequirements(componentsCache, instance);
		Assert.assertEquals(0, proxyRequirements.size());
	}

	@Test
	public void testConvertProxyRequirementsNotSubstitutedName() {
		Map<String, Component> componentsCache = new HashMap<>();
		RequirementDefinition r = new RequirementDefinition();
		r.setName("port0.dependency");
		r.setPreviousName("dependency");
		r.setCapability("tosca.capabilities.Node");
		r.setNode("tosca.nodes.Root");
		r.setRelationship("tosca.relationships.DependsOn");
		r.setMinOccurrences(RequirementDataDefinition.MIN_OCCURRENCES);
		r.setMaxOccurrences(RequirementDataDefinition.MAX_OCCURRENCES);
		r.setOwnerId("id");
		r.setParentName("parentName");

		Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
		List<RequirementDefinition> requirementDefinitions = new ArrayList<>();
		requirementDefinitions.add(r);
		requirements.put("dependency", requirementDefinitions);

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		instance.setComponentUid("componentUid");
		instance.setRequirements(requirements);
		instance.setNormalizedName("port0");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		List<Map<String, ToscaRequirement>> proxyRequirements =
				testSubject.convertProxyRequirements(componentsCache, instance);
		Map<String, ToscaRequirement> proxyRequirement = proxyRequirements.get(0);
		Assert.assertEquals("dependency", proxyRequirement.keySet().iterator().next());
	}

	@Test
	public void testConvertProxyRequirementsSubstitutedName() {
		Map<String, Component> componentsCache = new HashMap<>();
		RequirementDefinition r = new RequirementDefinition();
		r.setName("dependency");
		r.setPreviousName("dependency");
		r.setCapability("tosca.capabilities.Node");
		r.setNode("tosca.nodes.Root");
		r.setRelationship("tosca.relationships.DependsOn");
		r.setMinOccurrences(RequirementDataDefinition.MIN_OCCURRENCES);
		r.setMaxOccurrences(RequirementDataDefinition.MAX_OCCURRENCES);
		r.setOwnerId("id");
		r.setParentName("parentName");

		Map<String, List<RequirementDefinition>> requirements = new HashMap<>();
		List<RequirementDefinition> requirementDefinitions = new ArrayList<>();
		requirementDefinitions.add(r);
		requirements.put("dependency", requirementDefinitions);

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		instance.setComponentUid("componentUid");
		instance.setRequirements(requirements);
		instance.setNormalizedName("port0");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		testSubject.convertProxyRequirements(componentsCache, instance);
	}

	@Test
	public void testConvertProxyCapabilitiesWhenCapabilitiesNotNull() {
		Map<String, Component> componentsCache = new HashMap<>();
		Map<String, DataTypeDefinition> dataTypes = new HashMap<>();

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Map<String, List<CapabilityDefinition>> capabilitiesMap = new HashMap<String, List<CapabilityDefinition>>();
		List<CapabilityDefinition> capabilitiesArray = new ArrayList<CapabilityDefinition>();
		CapabilityDefinition definition = new CapabilityDefinition();
		definition.setOwnerId("id");
		capabilitiesArray.add(definition);
		capabilitiesMap.put("key", capabilitiesArray);
		vfComponent.setUniqueId("id");

		instance.setCapabilities(capabilitiesMap);
		instance.setComponentUid("uid");

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		testSubject.convertProxyCapabilities(componentsCache, instance, dataTypes);
	}

	@Test
	public void testConvertSubstitutionMappingCapabilities() {
		Map<String, Component> componentsCache = new HashMap<>();

		testSubject.convertSubstitutionMappingCapabilities(componentsCache, vfComponent);

		Map<String, List<CapabilityDefinition>> capabilitiesMap = new HashMap<String, List<CapabilityDefinition>>();
		List<CapabilityDefinition> capabilitiesArray = new ArrayList<CapabilityDefinition>();
		CapabilityDefinition definition = new CapabilityDefinition();
		definition.setOwnerId("id");
		definition.setName("name");
		definition.setParentName("parentName");
		List<String> path = new ArrayList<>();
		path.add("path1");
		path.add("id");
		definition.setPath(path);
		capabilitiesArray.add(definition);
		capabilitiesMap.put("key", capabilitiesArray);
		vfComponent.setCapabilities(capabilitiesMap);

		List<ComponentInstance> instances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("uid");
		instances.add(instance);
		vfComponent.setComponentInstances(instances);

		testSubject.convertSubstitutionMappingCapabilities(componentsCache, vfComponent);
	}


	@Test
	public void testAppendNameRecursively() {
		Map<String, Component> componentsCache = new HashMap<>();
		StringBuilder builder = new StringBuilder();
		List<String> path = new ArrayList<>();

		path.add("id");
		Iterator<String> iter = path.iterator();
		List<ComponentInstance> resourceInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		instance.setComponentUid("uid");
		resourceInstances.add(instance);
		vfComponent.setComponentInstances(resourceInstances);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		Deencapsulation.invoke(testSubject, "appendNameRecursively", componentsCache, vfComponent, iter, builder);

	}

	@Test
	public void testGetFilter() {
		ComponentInstance instance = new ComponentInstance();
		instance.setIsProxy(true);

		Deencapsulation.invoke(testSubject, "getFilter", instance);
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

        List<String> reducedMap = new CapabilityRequirementConverter().getReducedPathByOwner( pathList , uniqueId );

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
		// pathList.add("a77df84e-83eb-4edc-9823-d1f9f6549693.c79e9a4a-b172-4323-a2e2-1c48d6603241.lb_swu_direct_4_rvmi");
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
}
