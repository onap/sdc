package org.openecomp.sdc.be.tosca;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
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
import org.openecomp.sdc.be.tosca.ToscaUtils.SubstituitionEntry;
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateCapability;

import fj.data.Either;
import mockit.Deencapsulation;

public class CapabiltyRequirementConvertorTest {

	@InjectMocks
	CapabiltyRequirementConvertor testSubject;

	@Mock
	ToscaOperationFacade toscaOperationFacade;

	CapabiltyRequirementConvertor capabiltyRequirementConvertor = Mockito.spy(new CapabiltyRequirementConvertor());
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
		CapabiltyRequirementConvertor.getInstance();
	}

	@Test
	public void testConvertComponentInstanceCapabilties() {
		Map<String, List<CapabilityDefinition>> capabilities = newCapabilities("port");
		vfInstance.setCapabilities(capabilities);
		ToscaNodeTemplate nodeTemplate = new ToscaNodeTemplate();
		Map<String, DataTypeDefinition> testDataTypes = new HashMap<String, DataTypeDefinition>();

		capabilities.get("att.Node").clear();
		testSubject.convertComponentInstanceCapabilties(vfInstance, testDataTypes, nodeTemplate);

		capabilities = newCapabilities("port");
		vfInstance.setCapabilities(capabilities);
		vfInstance.setComponentUid("uid");

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		testSubject.convertComponentInstanceCapabilties(vfInstance, testDataTypes, nodeTemplate);

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

		testSubject.convertComponentInstanceCapabilties(vfInstance, testDataTypes, nodeTemplate);

	}

	@Test
	public void testConvertOverridenProperty() {
		ComponentInstance instance = new ComponentInstance();
		Map<String, DataTypeDefinition> testDataTypes = new HashMap<String, DataTypeDefinition>();
		Map<String, ToscaTemplateCapability> capabilities = new HashMap<>();
		ComponentInstanceProperty p = new ComponentInstanceProperty();
		SchemaDefinition entrySchema = new SchemaDefinition();
		entrySchema.setProperty(new PropertyDataDefinition());
		p.setSchema(entrySchema);

		Deencapsulation.invoke(testSubject, "convertOverridenProperty", instance, testDataTypes, capabilities, p,
				"port");
	}

	@Test
	public void testConvertRequirements() {
		ToscaNodeType nodeType = new ToscaNodeType();

		testSubject.convertRequirements(vfComponent, nodeType);

	}

	@Test
	public void testConvertRequirements_1() {
		ToscaNodeType nodeType = new ToscaNodeType();
		Map<String, List<RequirementDefinition>> requirementsMap = new HashMap<String, List<RequirementDefinition>>();

		List<RequirementDefinition> requirementsArray = new ArrayList<RequirementDefinition>();
		RequirementDefinition definition = new RequirementDefinition();
		definition.setOwnerId("id");
		requirementsArray.add(definition);
		requirementsMap.put("key", requirementsArray);
		vfComponent.setRequirements(requirementsMap);
		vfComponent.setUniqueId("id");

		testSubject.convertRequirements(vfComponent, nodeType);

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
	public void testBuildSubstitutedNamePerInstance() {
		Map<String, Component> componentsCache = new HashMap<>();
		String name = "name";
		String ownerId = "id";
		List<String> path = new ArrayList<>();
		path.add("id");
		SubstituitionEntry entry = new SubstituitionEntry();

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		instance.setComponentUid("uid");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class),
				Mockito.any(ComponentParametersView.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		Deencapsulation.invoke(testSubject, "buildSubstitutedNamePerInstance", componentsCache, vfComponent, name, path,
				ownerId, entry);
	}

	@Test
	public void testConvertRequirement() {
		RequirementDefinition definition = new RequirementDefinition();
		List<String> path = new ArrayList<>();
		path.add("value");
		path.add("id");
		definition.setName("name");
		definition.setPath(path);

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Deencapsulation.invoke(testSubject, "convertRequirement", vfComponent, false, definition);
	}

	@Test
	public void testConvertRequirement_1() {
		RequirementDefinition definition = new RequirementDefinition();
		List<String> path = new ArrayList<>();
		path.add("id");
		definition.setName("name");
		definition.setPath(path);

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Deencapsulation.invoke(testSubject, "convertRequirement", vfComponent, false, definition);
	}

	@Test
	public void testConvertRequirement_2() {
		RequirementDefinition definition = new RequirementDefinition();
		List<String> path = new ArrayList<>();
		path.add("id");
		definition.setName("name");
		definition.setPath(path);

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		path.add("value");
		definition.setPath(path);
		definition.setMaxOccurrences("1000");

		Deencapsulation.invoke(testSubject, "convertRequirement", vfComponent, false, definition);
	}

	@Test
	public void testConvertCapabilities() {
		Map<String, DataTypeDefinition> dataTypes = new HashMap<>();

		testSubject.convertCapabilities(vfComponent, dataTypes);

		Map<String, List<CapabilityDefinition>> capabilitiesMap = new HashMap<String, List<CapabilityDefinition>>();
		List<CapabilityDefinition> capabilitiesArray = new ArrayList<CapabilityDefinition>();
		CapabilityDefinition definition = new CapabilityDefinition();
		definition.setOwnerId("id");
		capabilitiesArray.add(definition);
		capabilitiesMap.put("key", capabilitiesArray);
		vfComponent.setUniqueId("id");
		vfComponent.setCapabilities(capabilitiesMap);

		testSubject.convertCapabilities(vfComponent, dataTypes);
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

		testSubject.convertProxyCapabilities(componentsCache, vfComponent, vfComponent, instance, dataTypes);
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

		testSubject.convertProxyCapabilities(componentsCache, vfComponent, vfComponent, instance, dataTypes);
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
	public void testGetCapabilityPath() {
		CapabilityDefinition definition = new CapabilityDefinition();
		List<String> path = new ArrayList<>();
		path.add("value");
		path.add("id");
		definition.setName("name");
		definition.setPath(path);

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Deencapsulation.invoke(testSubject, "getCapabilityPath", definition, vfComponent);
	}

	@Test
	public void testGetCapabilityPath_1() {
		CapabilityDefinition definition = new CapabilityDefinition();
		List<String> path = new ArrayList<>();
		path.add("id");
		definition.setName("name");
		definition.setPath(path);

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Deencapsulation.invoke(testSubject, "getCapabilityPath", definition, vfComponent);
	}

	@Test
	public void testGetCapabilityPath_2() {
		CapabilityDefinition definition = new CapabilityDefinition();
		List<String> path = new ArrayList<>();
		path.add("id");
		definition.setName("name");
		definition.setPath(path);

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		path.add("value");
		definition.setPath(path);

		Deencapsulation.invoke(testSubject, "getCapabilityPath", definition, vfComponent);
	}

	@Test
	public void testConvertCapability_1() {
		Map<String, DataTypeDefinition> dataTypes = new HashMap<>();

		Map<String, List<CapabilityDefinition>> capabilitiesMap = new HashMap<String, List<CapabilityDefinition>>();
		List<CapabilityDefinition> capabilitiesArray = new ArrayList<CapabilityDefinition>();
		CapabilityDefinition definition = new CapabilityDefinition();
		List<ComponentInstanceProperty> properties = new ArrayList<>();
		properties.add(new ComponentInstanceProperty());
		definition.setOwnerId("id");
		definition.setName("name");
		definition.setProperties(properties);
		definition.setMaxOccurrences("1000");
		List<String> path = new ArrayList<>();
		path.add("value");
		path.add("id");
		definition.setPath(path);
		capabilitiesArray.add(definition);
		capabilitiesMap.put("key", capabilitiesArray);
		vfComponent.setUniqueId("id");
		vfComponent.setCapabilities(capabilitiesMap);

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		componentInstances.add(instance);

		vfComponent.setComponentInstances(componentInstances);

		Deencapsulation.invoke(testSubject, "convertCapabilty", vfComponent, new HashMap<>(), false, definition,
				dataTypes, "name");
	}

	@Test
	public void testBuildSubstitutedName() {
		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId("id");
		List<ComponentInstance> resourceInstances = new ArrayList<>();
		resourceInstances.add(instance);
		vfComponent.setComponentInstances(resourceInstances);

		List<String> path = new ArrayList<>();
		path.add("notId");

		Deencapsulation.invoke(testSubject, "buildSubstitutedName", new HashMap<>(), vfComponent, path, "name");
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

		List<String> reducedMap = new CapabiltyRequirementConvertor().getReducedPathByOwner(pathList, uniqueId);

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
