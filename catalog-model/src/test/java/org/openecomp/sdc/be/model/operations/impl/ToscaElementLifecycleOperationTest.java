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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ToscaElementLifecycleOperationTest extends ModelTestBase {
	
	@javax.annotation.Resource
	protected TitanDao titanDao;

	@javax.annotation.Resource
	private NodeTypeOperation nodeTypeOperation;
	
	@javax.annotation.Resource
	private TopologyTemplateOperation topologyTemplateOperation;
	
	@javax.annotation.Resource
	private ToscaElementLifecycleOperation lifecycleOperation;
	
	String categoryName = "category";
	String subcategory = "mycategory";
	String outputDirectory = "C:\\Output";
	
	@Rule
	public TestName name = new TestName();

	@BeforeClass
	public static void initLifecycleOperation() {
		ModelTestBase.init();
	}
	
	private GraphVertex ownerVertex;
	private GraphVertex modifierVertex;
	private GraphVertex vfVertex;
	private GraphVertex serviceVertex;

	@Before
	public void setupBefore() {
		clearGraph();
		createUsers();
		createResourceCategory();
		createServiceCategory();
		createRootNodeType();
		createNodeType("firstVf");
		createTopologyTemplate("firstService");
	}

	
	@Test
	public void lifecycleTest() {
		Either<ToscaElement, StorageOperationStatus> res = lifecycleOperation
				.checkinToscaELement(LifecycleStateEnum.findState((String) vfVertex.getMetadataProperty(GraphPropertyEnum.STATE)), 
						vfVertex.getUniqueId(), modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		StorageOperationStatus status;
		
		assertTrue(res.isLeft());
		String id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), modifierVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		PropertyDataDefinition prop55 = new PropertyDataDefinition();
		prop55.setName("prop55");
		prop55.setDefaultValue("def55");
		 
		status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop55, JsonPresentationFields.NAME);
		assertTrue(status == StorageOperationStatus.OK);
		
		CapabilityDataDefinition cap1 = new CapabilityDataDefinition();
		cap1.setName("cap1");
		cap1.setDescription("create");
		cap1.setUniqueId(UniqueIdBuilder.buildCapabilityUid(id, "cap1"));
		 
		status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.CAPABILITIES, VertexTypeEnum.CAPABILTIES, cap1, JsonPresentationFields.NAME);
		assertTrue(status == StorageOperationStatus.OK);
		 
		res = lifecycleOperation.checkinToscaELement(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		prop55.setDefaultValue("AAAAAAAA");
		status = nodeTypeOperation.updateToscaDataOfToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop55, JsonPresentationFields.NAME);
		assertTrue(status == StorageOperationStatus.OK);
		
		cap1.setDescription("update");
		 
		status = nodeTypeOperation.updateToscaDataOfToscaElement(id, EdgeLabelEnum.CAPABILITIES, VertexTypeEnum.CAPABILTIES, cap1, JsonPresentationFields.NAME);
		assertTrue(status == StorageOperationStatus.OK);
		
		PropertyDataDefinition prop66 = new PropertyDataDefinition();
		prop66.setName("prop66");
		prop66.setDefaultValue("def66");
		 
		status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop66, JsonPresentationFields.NAME);
		assertTrue(status == StorageOperationStatus.OK);
		
		res = lifecycleOperation.requestCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.startCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.certifyToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), modifierVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		PropertyDataDefinition prop77 = new PropertyDataDefinition();
		prop77.setName("prop77");
		prop77.setDefaultValue("def77");
		 
		status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop77, JsonPresentationFields.NAME);
		assertTrue(status == StorageOperationStatus.OK);
		
		res = lifecycleOperation.checkinToscaELement(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		PropertyDataDefinition prop88 = new PropertyDataDefinition();
		prop88.setName("prop88");
		prop88.setDefaultValue("def88");
		 
		status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop88, JsonPresentationFields.NAME);
		assertTrue(status == StorageOperationStatus.OK);
		
		res = lifecycleOperation.requestCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.startCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.certifyToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		PropertyDataDefinition prop99 = new PropertyDataDefinition();
		prop99.setName("prop99");
		prop99.setDefaultValue("def99");
		 
		status = nodeTypeOperation.addToscaDataToToscaElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop99, JsonPresentationFields.NAME);
		assertTrue(status == StorageOperationStatus.OK);
		
		res = lifecycleOperation.requestCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.startCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		status = nodeTypeOperation.deleteToscaDataElement(id, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, "prop99", JsonPresentationFields.NAME);
		assertTrue(status == StorageOperationStatus.OK);
		
		//cancel certification
		res = lifecycleOperation.cancelOrFailCertification(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.startCertificationToscaElement(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		//fail certification
		res = lifecycleOperation.cancelOrFailCertification(id, modifierVertex.getUniqueId(), ownerVertex.getUniqueId(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		assertTrue(res.isLeft());
		id = res.left().value().getUniqueId();
		
		//exportGraphMl(titanDao.getGraph().left().value());
		
	}

	@Test
	public void serviceConformanceLevelTest() {
		Either<ToscaElement, StorageOperationStatus> res = lifecycleOperation
				.checkinToscaELement(LifecycleStateEnum.findState((String) serviceVertex.getMetadataProperty(GraphPropertyEnum.STATE)), 
						serviceVertex.getUniqueId(), modifierVertex.getUniqueId(), ownerVertex.getUniqueId());
		
		assertTrue(res.isLeft());
		String id = res.left().value().getUniqueId();
		
		res = lifecycleOperation.checkoutToscaElement(id, ownerVertex.getUniqueId(), modifierVertex.getUniqueId());
		assertTrue(res.isLeft());
		
		String conformanceLevel = res.left().value().getMetadataValue(JsonPresentationFields.CONFORMANCE_LEVEL).toString();
		assertEquals(conformanceLevel, ModelTestBase.configurationManager.getConfiguration().getToscaConformanceLevel());
	}
	
	private void createResourceCategory() {
		
		GraphVertex cat = new GraphVertex(VertexTypeEnum.RESOURCE_CATEGORY);
		Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
		String catId = UniqueIdBuilder.buildComponentCategoryUid(categoryName, VertexTypeEnum.RESOURCE_CATEGORY);
		cat.setUniqueId(catId);
		metadataProperties.put(GraphPropertyEnum.UNIQUE_ID,catId);
		metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.RESOURCE_CATEGORY.getName());
		metadataProperties.put(GraphPropertyEnum.NAME,categoryName);
		metadataProperties.put(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normalizeCategoryName4Uniqueness(categoryName));
		cat.setMetadataProperties(metadataProperties);
		cat.updateMetadataJsonWithCurrentMetadataProperties();
		
		GraphVertex subCat = new GraphVertex(VertexTypeEnum.RESOURCE_SUBCATEGORY);
		metadataProperties = new HashMap<>();
		String subCatId = UniqueIdBuilder.buildSubCategoryUid(cat.getUniqueId(), subcategory);
		subCat.setUniqueId(subCatId);
		metadataProperties.put(GraphPropertyEnum.UNIQUE_ID,subCatId);
		metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.RESOURCE_SUBCATEGORY.getName());
		metadataProperties.put(GraphPropertyEnum.NAME,subcategory);
		subCat.setMetadataProperties(metadataProperties);
		subCat.updateMetadataJsonWithCurrentMetadataProperties();
		
		Either<GraphVertex, TitanOperationStatus> catRes = titanDao.createVertex(cat);
		
		Either<GraphVertex, TitanOperationStatus> subCatRes = titanDao.createVertex(subCat);
		
		TitanOperationStatus status = titanDao.createEdge(catRes.left().value().getVertex(), subCatRes.left().value().getVertex(), EdgeLabelEnum.SUB_CATEGORY, new HashMap<>());
		assertEquals(TitanOperationStatus.OK, status);
	}
	
	private void createServiceCategory() {
		
		GraphVertex cat = new GraphVertex(VertexTypeEnum.SERVICE_CATEGORY);
		Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
		String catId = UniqueIdBuilder.buildComponentCategoryUid(categoryName, VertexTypeEnum.SERVICE_CATEGORY);
		cat.setUniqueId(catId);
		metadataProperties.put(GraphPropertyEnum.UNIQUE_ID,catId);
		metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.SERVICE_CATEGORY.getName());
		metadataProperties.put(GraphPropertyEnum.NAME,categoryName);
		metadataProperties.put(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normalizeCategoryName4Uniqueness(categoryName));
		cat.setMetadataProperties(metadataProperties);
		cat.updateMetadataJsonWithCurrentMetadataProperties();
				
		Either<GraphVertex, TitanOperationStatus> catRes = titanDao.createVertex(cat);
			
		assertTrue(catRes.isLeft());
	}
	
	private TopologyTemplate createTopologyTemplate(String name) {
		
		TopologyTemplate service = new TopologyTemplate();
		String uniqueId = UniqueIdBuilder.buildResourceUniqueId();
		service.setUniqueId(uniqueId);
		service.setCreatorUserId((String) ownerVertex.getMetadataProperty(GraphPropertyEnum.USERID));
		service.getMetadata().put(JsonPresentationFields.NAME.getPresentation(), name);
		service.getMetadata().put(JsonPresentationFields.UNIQUE_ID.getPresentation(), uniqueId);
		service.getMetadata().put(JsonPresentationFields.VERSION.getPresentation(), "0.1");
		service.getMetadata().put(JsonPresentationFields.TYPE.getPresentation(),ResourceTypeEnum.VF.name());
		service.getMetadata().put(JsonPresentationFields.COMPONENT_TYPE.getPresentation(),ComponentTypeEnum.RESOURCE);
		List<CategoryDefinition> categories = new ArrayList<>();
		CategoryDefinition cat = new CategoryDefinition();
		categories.add(cat);
		cat.setName(categoryName);
		service.setCategories(categories);
		
		service.setComponentType(ComponentTypeEnum.SERVICE);
		Either<TopologyTemplate, StorageOperationStatus> createRes = topologyTemplateOperation.createTopologyTemplate(service);
		assertTrue(createRes.isLeft());
		
		Either<GraphVertex, TitanOperationStatus> getNodeTyeRes= titanDao.getVertexById(createRes.left().value().getUniqueId());
		assertTrue(getNodeTyeRes.isLeft());
		
		serviceVertex = getNodeTyeRes.left().value();
		 
		return service;
	}
	
	private <T extends ToscaDataDefinition> NodeType createNodeType(String nodeTypeName) {
		
		NodeType vf = new NodeType();
		String uniqueId = UniqueIdBuilder.buildResourceUniqueId();
		vf.setUniqueId(uniqueId);
		vf.setCreatorUserId((String) ownerVertex.getMetadataProperty(GraphPropertyEnum.USERID));
		vf.getMetadata().put(JsonPresentationFields.NAME.getPresentation(), nodeTypeName);
		vf.getMetadata().put(JsonPresentationFields.UNIQUE_ID.getPresentation(), uniqueId);
		vf.getMetadata().put(JsonPresentationFields.VERSION.getPresentation(), "0.1");
		vf.getMetadata().put(JsonPresentationFields.TYPE.getPresentation(),ResourceTypeEnum.VF.name());
		vf.getMetadata().put(JsonPresentationFields.COMPONENT_TYPE.getPresentation(),ComponentTypeEnum.RESOURCE);
		List<CategoryDefinition> categories = new ArrayList<>();
		CategoryDefinition cat = new CategoryDefinition();
		categories.add(cat);
		cat.setName(categoryName);
		List<SubCategoryDefinition> subCategories = new ArrayList<>();
		SubCategoryDefinition subCat = new SubCategoryDefinition();
		subCat.setName(subcategory);
		subCategories.add(subCat);
		cat.setSubcategories(subCategories);
		vf.setCategories(categories);
		
		
		List<String> derivedFrom = new ArrayList<>();
		derivedFrom.add("root");
		vf.setDerivedFrom(derivedFrom);
		
//		 Map<String, PropertyDataDefinition> properties = new HashMap<>();
//		 PropertyDataDefinition prop1 = new PropertyDataDefinition();
//		 prop1.setName("prop1");
//		 prop1.setDefaultValue("def1");
//		 
//		 properties.put("prop1", prop1);
//		 
//		 PropertyDataDefinition prop2 = new PropertyDataDefinition();
//		 prop2.setName("prop2");
//		 prop2.setDefaultValue("def2");
//		 properties.put("prop2", prop2);
//		 
//		 PropertyDataDefinition prop3 = new PropertyDataDefinition();
//		 prop3.setName("prop3");
//		 prop3.setDefaultValue("def3");
//		 properties.put("prop3", prop3);
//		 
//		 vf.setProperties(properties);
		 vf.setComponentType(ComponentTypeEnum.RESOURCE);
		Either<NodeType, StorageOperationStatus> createVFRes = nodeTypeOperation.createNodeType(vf);
		assertTrue(createVFRes.isLeft());
		
		Either<GraphVertex, TitanOperationStatus> getNodeTyeRes= titanDao.getVertexById(createVFRes.left().value().getUniqueId());
		assertTrue(getNodeTyeRes.isLeft());
		
		vfVertex = getNodeTyeRes.left().value();
		
		
		
		 List<PropertyDataDefinition> addProperties = new ArrayList<>();
		 PropertyDataDefinition prop11 = new PropertyDataDefinition();
		 prop11.setName("prop11");
		 prop11.setDefaultValue("def11");
		 
		 addProperties.add(prop11);
		 
		 PropertyDataDefinition prop22 = new PropertyDataDefinition();
		 prop22.setName("prop22");
		 prop22.setDefaultValue("def22");
		 addProperties.add(prop22);
		 
		 StorageOperationStatus status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, addProperties, JsonPresentationFields.NAME);
		 assertTrue(status == StorageOperationStatus.OK);
		 
		 PropertyDataDefinition prop33 = new PropertyDataDefinition();
		 prop33.setName("prop33");
		 prop33.setDefaultValue("def33");
		 
		 status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex, EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop33, JsonPresentationFields.NAME);
		 assertTrue(status == StorageOperationStatus.OK);
		 
		 PropertyDataDefinition prop44 = new PropertyDataDefinition();
		 prop44.setName("prop44");
		 prop44.setDefaultValue("def44");
		 
		 status = nodeTypeOperation.addToscaDataToToscaElement(vfVertex.getUniqueId(), EdgeLabelEnum.PROPERTIES, VertexTypeEnum.PROPERTIES, prop44, JsonPresentationFields.NAME);
		 assertTrue(status == StorageOperationStatus.OK);
		 	
		 
		 PropertyDataDefinition capProp = new PropertyDataDefinition();
		 capProp.setName( "capProp");
		 capProp.setDefaultValue( "capPropDef");
		 
		 MapDataDefinition dataToCreate = new MapPropertiesDataDefinition();		 
		 dataToCreate.put("capProp", capProp);
		 
		 Map<String, MapDataDefinition> capProps = new HashMap();
		 capProps.put("capName", dataToCreate);	
		
		 Either<GraphVertex, StorageOperationStatus> res = nodeTypeOperation.assosiateElementToData(vfVertex,  VertexTypeEnum.CAPABILITIES_PROPERTIES, EdgeLabelEnum.CAPABILITIES_PROPERTIES, capProps);
		 
		// exportGraphMl(titanDao.getGraph().left().value());
		 
		 List<String> pathKeys = new ArrayList<>();
		 pathKeys.add("capName");
		 capProp.setDefaultValue( "BBBB");
		 status = nodeTypeOperation.updateToscaDataDeepElementOfToscaElement(vfVertex, EdgeLabelEnum.CAPABILITIES_PROPERTIES, VertexTypeEnum.CAPABILITIES_PROPERTIES,
				 capProp, pathKeys, JsonPresentationFields.NAME);
		return vf;
	}
	
	private void createRootNodeType() {
		
		NodeType vf = new NodeType();
		String uniqueId = UniqueIdBuilder.buildResourceUniqueId();
		vf.setUniqueId(uniqueId);
		vf.setComponentType(ComponentTypeEnum.RESOURCE);
		vf.setCreatorUserId((String) ownerVertex.getMetadataProperty(GraphPropertyEnum.USERID));
		vf.getMetadata().put(JsonPresentationFields.NAME.getPresentation(), "root");
		vf.getMetadata().put(JsonPresentationFields.UNIQUE_ID.getPresentation(), uniqueId);
		vf.getMetadata().put(JsonPresentationFields.VERSION.getPresentation(), "1.0");
		vf.getMetadata().put(JsonPresentationFields.TYPE.getPresentation(),ResourceTypeEnum.VFC.name());
		vf.getMetadata().put(JsonPresentationFields.LIFECYCLE_STATE.getPresentation(), LifecycleStateEnum.CERTIFIED.name());
		vf.getMetadata().put(JsonPresentationFields.TOSCA_RESOURCE_NAME.getPresentation(), "root");
		vf.getMetadata().put(JsonPresentationFields.HIGHEST_VERSION.getPresentation(), true);
		
		List<CategoryDefinition> categories = new ArrayList<>();
		CategoryDefinition cat = new CategoryDefinition();
		categories.add(cat);
		cat.setName(categoryName);
		List<SubCategoryDefinition> subCategories = new ArrayList<>();
		SubCategoryDefinition subCat = new SubCategoryDefinition();
		subCat.setName(subcategory);
		subCategories.add(subCat);
		cat.setSubcategories(subCategories);
		vf.setCategories(categories);
		
		
		List<String> derivedFrom = new ArrayList<>();
		vf.setDerivedFrom(derivedFrom);
		
		 Map<String, PropertyDataDefinition> properties = new HashMap<>();
		 PropertyDataDefinition prop1 = new PropertyDataDefinition();
		 prop1.setName("derived1");
		 prop1.setDefaultValue("deriveddef1");
		 
		 properties.put("derived1", prop1);
		 
		 PropertyDataDefinition prop2 = new PropertyDataDefinition();
		 prop2.setUniqueId("derived2");
		 prop2.setName("deriveddef2");
		 properties.put("derived2", prop2);
		 
		 PropertyDataDefinition prop3 = new PropertyDataDefinition();
		 prop3.setName("derived3");
		 prop3.setDefaultValue("deriveddef3");
		 properties.put("derived3", prop3);
		 
		 vf.setProperties(properties);
		 vf.setComponentType(ComponentTypeEnum.RESOURCE);
		Either<NodeType, StorageOperationStatus> createVFRes = nodeTypeOperation.createNodeType(vf);
		assertTrue(createVFRes.isLeft());
		
		Either<GraphVertex, TitanOperationStatus> getNodeTyeRes= titanDao.getVertexById(createVFRes.left().value().getUniqueId());
		assertTrue(getNodeTyeRes.isLeft());
	}

	private void createUsers() {
		
		GraphVertex ownerV = new GraphVertex(VertexTypeEnum.USER);
		ownerV.setUniqueId("user1");
				
		Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
		metadataProperties.put(GraphPropertyEnum.USERID, ownerV.getUniqueId());
		metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.USER.getName());
		metadataProperties.put(GraphPropertyEnum.NAME, "user1");
		ownerV.setMetadataProperties(metadataProperties);
		ownerV.updateMetadataJsonWithCurrentMetadataProperties();
		ownerV.setJson(new HashMap<>());
		Either<GraphVertex, TitanOperationStatus> createUserRes = titanDao.createVertex(ownerV);
		assertTrue(createUserRes.isLeft());
		
		ownerVertex = createUserRes.left().value();
		
		GraphVertex modifierV = new GraphVertex(VertexTypeEnum.USER);
		modifierV.setUniqueId("user2");
				
		metadataProperties = new HashMap<>();
		metadataProperties.put(GraphPropertyEnum.USERID, modifierV.getUniqueId());
		metadataProperties.put(GraphPropertyEnum.LABEL, VertexTypeEnum.USER.getName());
		metadataProperties.put(GraphPropertyEnum.NAME, "user2");
		modifierV.setMetadataProperties(metadataProperties);
		modifierV.updateMetadataJsonWithCurrentMetadataProperties();
		modifierV.setJson(new HashMap<>());
		createUserRes = titanDao.createVertex(modifierV);
		assertTrue(createUserRes.isLeft());
				
		modifierVertex = createUserRes.left().value();
			
		Either<GraphVertex, TitanOperationStatus> getOwnerRes = lifecycleOperation.findUser(ownerVertex.getUniqueId());
		assertTrue(getOwnerRes.isLeft());
	
	}

	@After
	public void teardown() {
		clearGraph();
	}

	private void clearGraph() {
		Either<TitanGraph, TitanOperationStatus> graphResult = titanDao.getGraph();
		TitanGraph graph = graphResult.left().value();

		Iterable<TitanVertex> vertices = graph.query().vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				TitanVertex vertex = iterator.next();
				vertex.remove();
			}
		}
		titanDao.commit();
	}

	private String exportGraphMl(TitanGraph graph) {
		String result = null;
		String outputFile = outputDirectory + File.separator + "exportGraph." + System.currentTimeMillis() + ".graphml";
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
	
}
