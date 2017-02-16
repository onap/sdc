/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityOperation;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.impl.LifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.operations.impl.ServiceOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.model.operations.impl.util.ResourceCreationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityInstData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.be.unittests.utils.FactoryUtils;
import org.openecomp.sdc.be.unittests.utils.FactoryUtils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ComponentInstanceOperationSpringTest extends ModelTestBase {
	private static Logger log = LoggerFactory.getLogger(ComponentInstanceOperationSpringTest.class.getName());
	@Resource(name = "component-instance-operation")
	private ComponentInstanceOperation componentInstanceOperation;

	@Resource(name = "component-instance-operation")
	private ComponentInstanceOperation resourceInstanceOperation;

	@Resource(name = "capability-type-operation")
	private CapabilityTypeOperation capabilityTypeOperation;

	@Resource(name = "capability-operation")
	public CapabilityOperation capabilityOperation;

	@Resource(name = "service-operation")
	private ServiceOperation serviceOperation;

	@Resource(name = "resource-operation")
	private ResourceOperation resourceOperation;

	@Resource(name = "property-operation")
	private PropertyOperation propertyOperation;

	@Resource(name = "lifecycle-operation")
	private LifecycleOperation lifecycleOperation;

	TitanGenericDao titanGenericDao;

	private static String CATEGORY_NAME = "category/mycategory";

	User rfcUser;

	@BeforeClass
	public static void setupBeforeClass() {
		ModelTestBase.init();

	}

	@Before
	public void cleanUp() {
		titanGenericDao = componentInstanceOperation.titanGenericDao;
		Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
		TitanGraph graph = graphResult.left().value();

		Iterable<TitanVertex> vertices = graph.query().vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				TitanVertex vertex = iterator.next();
				vertex.remove();
			}

		}
		titanGenericDao.commit();
		deleteAndCreateCategory(CATEGORY_NAME);
		UserData modifierData = deleteAndCreateUser(ResourceCreationUtils.MODIFIER_ATT_UID + "rfc",
				ResourceCreationUtils.MODIFIER_FIRST_NAME, ResourceCreationUtils.MODIFIER_LAST_NAME, "ADMIN");
		rfcUser = convertUserDataToUser(modifierData);
	}

	@Test
	public void testAddCapabilityPropertyValuesToResourceInstance() {
		String rootName = "Root123";
		org.openecomp.sdc.be.model.Resource rootResource = createResource(rfcUser.getUserId(), CATEGORY_NAME, rootName,
				"1.0", null, false, true);

		// certification request
		Either<? extends org.openecomp.sdc.be.model.Component, StorageOperationStatus> requestCertificationResult = lifecycleOperation
				.requestCertificationComponent(NodeTypeEnum.Resource, rootResource, rfcUser, rfcUser, false);
		assertTrue(requestCertificationResult.isLeft());

		org.openecomp.sdc.be.model.Resource resultResource = (org.openecomp.sdc.be.model.Resource) requestCertificationResult
				.left().value();

		// start certification
		Either<? extends org.openecomp.sdc.be.model.Component, StorageOperationStatus> startCertificationResult = lifecycleOperation
				.startComponentCertification(NodeTypeEnum.Resource, resultResource, rfcUser, rfcUser, false);
		assertEquals(true, startCertificationResult.isLeft());

		Either<? extends org.openecomp.sdc.be.model.Component, StorageOperationStatus> certifiedResourceRes = lifecycleOperation
				.certifyComponent(NodeTypeEnum.Resource, rootResource, rfcUser, rfcUser, false);
		assertTrue(certifiedResourceRes.isLeft());

		CapabilityTypeDefinition capabilityType = buildCapabilityType();
		Either<CapabilityTypeDefinition, StorageOperationStatus> capabilityTypeRes = capabilityTypeOperation
				.addCapabilityType(capabilityType);
		assertTrue(capabilityTypeRes.isLeft());

		CapabilityData capData = FactoryUtils.createCapabilityData();
		CapabilityDefinition capabilityDefinitionRoot = FactoryUtils
				.convertCapabilityDataToCapabilityDefinitionRoot(capData);

		Either<CapabilityDefinition, StorageOperationStatus> addCapabilityRootRes = capabilityOperation.addCapability(
				(String) certifiedResourceRes.left().value().getUniqueId(), capabilityDefinitionRoot.getName(),
				capabilityDefinitionRoot);
		assertTrue(addCapabilityRootRes.isLeft());

		String resourceName = "tosca.nodes.Apache.2.0";

		CapabilityDefinition capabilityDefinition = FactoryUtils
				.convertCapabilityDataToCapabilityDefinitionAddProperties(capData);
		org.openecomp.sdc.be.model.Resource resource = createResource(rfcUser.getUserId(), CATEGORY_NAME, resourceName,
				"0.1", rootName, false, true);

		Either<CapabilityDefinition, StorageOperationStatus> addCapabilityRes = capabilityOperation
				.addCapability((String) resource.getUniqueId(), capabilityDefinition.getName(), capabilityDefinition);
		assertTrue(addCapabilityRes.isLeft());
		List<ComponentInstanceProperty> properties = addCapabilityRes.left().value().getProperties();
		assertTrue(properties.size() == 2);

		Either<org.openecomp.sdc.be.model.Resource, StorageOperationStatus> clonedResourceRes = resourceOperation
				.cloneComponent(resource, "0.2", false);
		assertTrue(clonedResourceRes.isLeft());
		org.openecomp.sdc.be.model.Resource clonedResource = clonedResourceRes.left().value();

		ComponentInstance instance = buildResourceInstance(clonedResource.getUniqueId(), "1", "tosca.nodes.Apache");

		Service origService = createService(rfcUser.getUserId(), CATEGORY_NAME, "my-service", "1.0", true);
		Either<Service, StorageOperationStatus> service2 = serviceOperation.getService(origService.getUniqueId(),
				false);
		assertTrue(service2.isLeft());
		origService = service2.left().value();

		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String json = prettyGson.toJson(origService);
		log.debug(json);

		Service fullService = origService;

		Either<ComponentInstance, TitanOperationStatus> status = resourceInstanceOperation
				.addComponentInstanceToContainerComponent((String) origService.getUniqueId(), NodeTypeEnum.Service, "1",
						true, instance, NodeTypeEnum.Resource, false);
		assertTrue(status.isLeft());

		ComponentInstance resourceInstance = status.left().value();
		CapabilityDefinition capability = addCapabilityRes.left().value();
		capability.setName(capabilityDefinition.getName());
		List<ComponentInstanceProperty> propertyValues = FactoryUtils.createComponentInstancePropertyList();
		capability.setProperties(propertyValues);

		Either<Map<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> addCPVsToRiRes = componentInstanceOperation
				.addCapabilityPropertyValuesToResourceInstance(resourceInstance.getUniqueId(), capability, true);
		assertTrue(addCPVsToRiRes.isLeft());

		Either<Service, StorageOperationStatus> createService = serviceOperation.cloneService(fullService, "2.0",
				false);
		assertTrue(createService.isLeft());
		Map<String, List<CapabilityDefinition>> capabilitiesMap = createService.left().value().getCapabilities();
		assertTrue(capabilitiesMap != null && capabilitiesMap.size() == 1);
		Map<String, CapabilityDefinition> capabilities = capabilitiesMap.values().iterator().next().stream()
				.collect(Collectors.toMap(CapabilityDefinition::getName, Function.identity()));
		assertTrue(capabilities.containsKey("Cap1") && capabilities.containsKey("Cap2"));

		// String outputFile = exportGraphMl();

	}

	public String exportGraphMl() {
		String result = null;
		String outputFile = "C:\\Output" + File.separator + "exportGraph." + System.currentTimeMillis() + ".graphml";
		TitanGraph graph = titanGenericDao.getGraph().left().value();
		try {
			try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
				graph.io(IoCore.graphml()).writer().normalize(true).create().writeGraph(os, graph);
			}
			result = outputFile;
			graph.tx().commit();
		} catch (Exception e) {
			graph.tx().rollback();
			e.printStackTrace();
		}
		return result;

	}

	private CapabilityTypeDefinition buildCapabilityType() {
		CapabilityTypeDefinition capabilityType = new CapabilityTypeDefinition();
		Map<String, PropertyDefinition> properties = new HashMap();

		capabilityType.setType(Constants.DEFAULT_CAPABILITY_TYPE);
		capabilityType.setProperties(properties);

		PropertyDefinition host = new PropertyDefinition();
		host.setUniqueId(UUID.randomUUID().toString());
		host.setName("host");
		host.setDefaultValue("captypehost");
		host.setType("string");

		host.setSchema(new SchemaDefinition());
		host.getSchema().setProperty(new PropertyDataDefinition());
		host.getSchema().getProperty().setType("string");

		PropertyDefinition port = new PropertyDefinition();
		port.setName("port");
		port.setDefaultValue("captypeport");
		port.setUniqueId(UUID.randomUUID().toString());
		port.setType("string");

		port.setSchema(new SchemaDefinition());
		port.getSchema().setProperty(new PropertyDataDefinition());
		port.getSchema().getProperty().setType("string");

		PropertyDefinition rootproperty = new PropertyDefinition();
		rootproperty.setName("captypeproperty");
		rootproperty.setDefaultValue("captypevalue");
		rootproperty.setUniqueId(UUID.randomUUID().toString());
		rootproperty.setType("string");

		rootproperty.setSchema(new SchemaDefinition());
		rootproperty.getSchema().setProperty(new PropertyDataDefinition());
		rootproperty.getSchema().getProperty().setType("string");

		properties.put("host", host);
		properties.put("port", port);
		properties.put("captypeproperty", rootproperty);
		return capabilityType;
	}

	private CapabilityInstData buildCapabilityInstanceData(String resourceInstanceId, CapabilityDefinition capability) {
		CapabilityInstData capabilityInstance = new CapabilityInstData();
		Long creationTime = System.currentTimeMillis();
		String uniqueId = UniqueIdBuilder.buildCapabilityInstanceUid(resourceInstanceId, capability.getName());
		capabilityInstance.setCreationTime(creationTime);
		capabilityInstance.setModificationTime(creationTime);
		capabilityInstance.setUniqueId(uniqueId);
		return capabilityInstance;
	}

	private ComponentInstance buildResourceInstance(String respurceUid, String instanceNumber, String name) {
		ComponentInstance resourceInstance = new ComponentInstance();
		resourceInstance.setName(name);
		resourceInstance.setDescription("desc1");
		resourceInstance.setPosX("20");
		resourceInstance.setPosY("40");
		resourceInstance.setComponentUid(respurceUid);
		resourceInstance.setCreationTime(System.currentTimeMillis());
		resourceInstance.setModificationTime(System.currentTimeMillis());
		resourceInstance.setNormalizedName(ResourceInstanceOperationTest.normaliseComponentInstanceName(name));
		return resourceInstance;
	}

	public ResourceMetadataData createResource(String resourceName, TitanGenericDao titanGenericDao) {
		ResourceMetadataData serviceData1 = new ResourceMetadataData();
		serviceData1.getMetadataDataDefinition().setUniqueId(resourceName);
		Either<ResourceMetadataData, TitanOperationStatus> createNode = titanGenericDao.createNode(serviceData1,
				ResourceMetadataData.class);
		assertTrue("check service created", createNode.isLeft());
		return createNode.left().value();
	}

	public ServiceMetadataData createServiceMetadataData(String serviceName, TitanGenericDao titanGenericDao) {
		ServiceMetadataData serviceData1 = new ServiceMetadataData();
		serviceData1.getMetadataDataDefinition().setUniqueId(serviceName);
		Either<ServiceMetadataData, TitanOperationStatus> createNode = titanGenericDao.createNode(serviceData1,
				ServiceMetadataData.class);
		assertTrue("check service created", createNode.isLeft());
		return createNode.left().value();
	}

	public Service createService(String userId, String category, String serviceName, String serviceVersion,
			boolean isHighestVersion) {
		Service service = buildServiceMetadata(userId, category, serviceName, serviceVersion);
		service.setHighestVersion(isHighestVersion);
		Either<Service, StorageOperationStatus> result = serviceOperation.createService(service, true);
		assertTrue(result.isLeft());
		Service resultService = result.left().value();
		assertEquals("check resource state", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				resultService.getLifecycleState());
		return resultService;
	}

	private Service buildServiceMetadata(String userId, String category, String serviceName, String serviceVersion) {
		Service service = new Service();
		service.setName(serviceName);
		service.setVersion(serviceVersion);
		service.setDescription("description 1");
		service.setCreatorUserId(userId);
		service.setContactId("contactId@sdc.com");
		CategoryDefinition categoryDef = new CategoryDefinition();
		categoryDef.setName(category);
		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(categoryDef);
		service.setCategories(categories);
		service.setIcon("images/my.png");
		List<String> tags = new ArrayList<String>();
		tags.add("TAG1");
		tags.add("TAG2");
		service.setTags(tags);
		return service;
	}

	private void deleteAndCreateCategory(String category) {
		String[] names = category.split("/");
		OperationTestsUtil.deleteAndCreateServiceCategory(category, titanGenericDao);
		OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], titanGenericDao);
	}

	private UserData deleteAndCreateUser(String userId, String firstName, String lastName, String role) {
		UserData userData = new UserData();
		userData.setUserId(userId);
		userData.setFirstName(firstName);
		userData.setLastName(lastName);
		if (role != null && !role.isEmpty()) {
			userData.setRole(role);
		} else {
			userData.setRole("ADMIN");
		}
		titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class);
		titanGenericDao.createNode(userData, UserData.class);
		titanGenericDao.commit();
		return userData;
	}

	public org.openecomp.sdc.be.model.Resource createResource(String userId, String category, String resourceName,
			String resourceVersion, String parentResourceName, boolean isAbstract, boolean isHighestVersion) {

		String propName1 = "disk_size";
		String propName2 = "num_cpus";

		List<String> derivedFrom = new ArrayList<String>();
		if (parentResourceName != null) {
			derivedFrom.add(parentResourceName);
		}
		org.openecomp.sdc.be.model.Resource resource = buildResourceMetadata(userId, category, resourceName,
				resourceVersion);

		resource.setAbstract(isAbstract);
		resource.setHighestVersion(isHighestVersion);

		Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();

		PropertyDefinition property1 = new PropertyDefinition();
		property1.setDefaultValue("10");
		property1.setDescription(
				"Size of the local disk, in Gigabytes (GB), available to applications running on the Compute node.");
		property1.setType(ToscaType.INTEGER.name().toLowerCase());
		List<PropertyConstraint> constraints = new ArrayList<PropertyConstraint>();
		GreaterThanConstraint propertyConstraint1 = new GreaterThanConstraint("0");
		log.debug("{}", propertyConstraint1);

		constraints.add(propertyConstraint1);

		LessOrEqualConstraint propertyConstraint2 = new LessOrEqualConstraint("10");
		constraints.add(propertyConstraint2);

		property1.setConstraints(constraints);

		properties.put(propName1, property1);

		PropertyDefinition property2 = new PropertyDefinition();
		property2.setDefaultValue("2");
		property2.setDescription("Number of (actual or virtual) CPUs associated with the Compute node.");
		property2.setType(ToscaType.INTEGER.name().toLowerCase());
		List<PropertyConstraint> constraints3 = new ArrayList<PropertyConstraint>();
		List<String> range = new ArrayList<String>();
		range.add("1");
		range.add("4");

		InRangeConstraint propertyConstraint3 = new InRangeConstraint(range);
		constraints3.add(propertyConstraint3);
		property2.setConstraints(constraints3);
		properties.put(propName2, property2);

		resource.setDerivedFrom(derivedFrom);

		resource.setProperties(convertMapToList(properties));

		Either<org.openecomp.sdc.be.model.Resource, StorageOperationStatus> result = resourceOperation
				.createResource(resource, true);

		assertTrue(result.isLeft());
		org.openecomp.sdc.be.model.Resource resultResource = result.left().value();
		assertEquals("check resource state", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				resultResource.getLifecycleState());

		String resourceId = resultResource.getUniqueId();

		Either<PropertyDefinition, StorageOperationStatus> either = propertyOperation.getPropertyOfResource(propName1,
				resourceId);

		assertTrue(either.isLeft());
		PropertyDefinition propertyDefinition = either.left().value();
		assertEquals("check property default value", property1.getDefaultValue(), propertyDefinition.getDefaultValue());
		assertEquals("check property description", property1.getDescription(), propertyDefinition.getDescription());
		assertEquals("check property type", property1.getType(), propertyDefinition.getType());
		assertEquals("check property unique id", property1.getUniqueId(), propertyDefinition.getUniqueId());
		assertEquals("check property consitraints size", property1.getConstraints().size(),
				propertyDefinition.getConstraints().size());

		return resultResource;
	}

	private org.openecomp.sdc.be.model.Resource buildResourceMetadata(String userId, String category,
			String resourceName, String resourceVersion) {

		org.openecomp.sdc.be.model.Resource resource = new org.openecomp.sdc.be.model.Resource();
		resource.setName(resourceName);
		resource.setVersion(resourceVersion);
		;
		resource.setDescription("description 1");
		resource.setAbstract(false);
		resource.setCreatorUserId(userId);
		resource.setContactId("contactId@sdc.com");
		resource.setVendorName("vendor 1");
		resource.setVendorRelease("1.0.0");
		resource.setToscaResourceName(resourceName);
		String[] categoryArr = category.split("/");
		resource.addCategory(categoryArr[0], categoryArr[1]);
		resource.setIcon("images/my.png");
		List<String> tags = new ArrayList<String>();
		tags.add("TAG1");
		tags.add("TAG2");
		resource.setTags(tags);
		return resource;
	}

	public static List<PropertyDefinition> convertMapToList(Map<String, PropertyDefinition> properties) {
		if (properties == null) {
			return null;
		}

		List<PropertyDefinition> definitions = new ArrayList<>();
		for (Entry<String, PropertyDefinition> entry : properties.entrySet()) {
			String name = entry.getKey();
			PropertyDefinition propertyDefinition = entry.getValue();
			propertyDefinition.setName(name);
			definitions.add(propertyDefinition);
		}

		return definitions;
	}

	private User convertUserDataToUser(UserData modifierData) {
		User modifier = new User();
		modifier.setUserId(modifierData.getUserId());
		modifier.setEmail(modifierData.getEmail());
		modifier.setFirstName(modifierData.getFirstName());
		modifier.setLastName(modifierData.getLastName());
		modifier.setRole(modifierData.getRole());
		return modifier;
	}
}
