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

package org.openecomp.sdc.asdctool.impl.migration.v1604;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.AdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.model.operations.impl.LifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.ProductOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.operations.impl.ServiceOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ProductMetadataData;
import org.openecomp.sdc.be.resources.data.RelationshipInstData;
import org.openecomp.sdc.be.resources.data.RequirementData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.TagData;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public class ServiceMigration {

	private static final String[] NORMATIVE_OLD_NAMES = { "tosca.nodes.network.Network", "tosca.nodes.network.Port", "tosca.nodes.BlockStorage", "tosca.nodes.Compute", "tosca.nodes.Container.Application", "tosca.nodes.Container.Runtime",
			"tosca.nodes.Database", "tosca.nodes.DBMS", "tosca.nodes.LoadBalancer", "tosca.nodes.ObjectStorage", "tosca.nodes.Root", "tosca.nodes.SoftwareComponent", "tosca.nodes.WebApplication", "tosca.nodes.WebServer", };

	private static Logger log = LoggerFactory.getLogger(ServiceMigration.class.getName());

	@Autowired
	protected TitanGenericDao titanGenericDao;
	@Autowired
	protected ResourceOperation resourceOperation;
	@Autowired
	protected ServiceOperation serviceOperation;
	@Autowired
	protected ProductOperation productOperation;
	@Autowired
	protected LifecycleOperation lifecycleOperaion;
	@Autowired
	protected PropertyOperation propertyOperation;
	@Autowired
	protected AdditionalInformationOperation additionalInformationOperation;
	@Autowired
	protected ComponentInstanceOperation componentInstanceOperaion;
	@Autowired
	protected IElementOperation elementOperation;

	public boolean migrate1602to1604(String appConfigDir) {

		boolean result = false;

		try {

			if (!addResourceCounterToResources()) {
				log.debug("Failed to update resource instance counter on resources");
				result = false;
				return result;
			}
			if (!updateComponentInstanceType()) {
				log.debug("Failed to update component instance type");
				result = false;
				return result;
			}
			// fix VF
			if (!fixDerivedVf()) {
				log.debug("Failed to fix VFs");
				result = false;
				return result;
			}
			// update instances and relation
			if (!updateCalculatedEdges()) {
				log.debug("Failed to update calculated edges for VF instances");
				result = false;
				return result;
			}
			// update instances and relation
			if (!updateRelations()) {
				log.debug("Failed to update Instance And Relations in services");
				result = false;
				return result;
			}
			if (!updateCategories(appConfigDir)) {
				log.debug("Failed to update categories");
				result = false;
				return result;
			}

			if (!AllowMultipleHeats.removeAndUpdateHeatPlaceHolders(titanGenericDao, log, true)) {
				log.error("Failed to update heat place holders");
				result = false;
				return result;
			}

			if (!AddGroupUuid.addGroupUuids(titanGenericDao, log, true)) {
				log.error("Failed to update group UUIDs");
				result = false;
				return result;
			}

			result = true;
		} finally {
			if (!result) {
				titanGenericDao.rollback();
			} else {
				titanGenericDao.commit();
			}
		}
		return result;
	}

	private boolean updateCategories(String appConfigDir) {
		// String categoryMigrationFile = appConfigDir + File.separator +
		// "categoryMigration.yaml";
		String categoryMigrationFile = appConfigDir + "categoryMigration.yaml";

		Map<String, List<MigrationCategory>> categoriesFromYml;
		try {
			categoriesFromYml = createCategoriesFromYml(categoryMigrationFile);
			if (categoriesFromYml == null || categoriesFromYml.isEmpty()) {
				log.debug("updateCategories failed to load categories form migration file {}", categoryMigrationFile);
				return false;
			}
		} catch (Exception e) {
			log.debug("Failed to load category migration file : {}", categoryMigrationFile, e);
			return false;
		}
		for (Map.Entry<String, List<MigrationCategory>> entry : categoriesFromYml.entrySet()) {
			ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(entry.getKey());
			if (componentType != null) {
				switch (componentType) {
				case RESOURCE:
					if (updateResourceCategories(entry.getValue()) == false) {
						log.debug("updateCategories failed to update resource categories");
						return false;
					}
					break;
				case SERVICE:
					if (updateServiceCategories(entry.getValue()) == false) {
						log.debug("updateCategories failed to update service categories");
						return false;
					}
					break;
				default:
					log.debug("updateCategories no changes for categories from type {}", componentType);
				}
			} else {
				log.debug("updateCategories failed not supported component file in migration categories file" + entry.getKey());
				return false;
			}
		}
		return true;
	}

	private boolean updateServiceCategories(List<MigrationCategory> categories) {
		log.debug("updateServiceCategories STARTED");
		Either<List<CategoryDefinition>, ActionStatus> serviceCategories = elementOperation.getAllCategories(NodeTypeEnum.ServiceNewCategory, true);
		if (serviceCategories.isRight()) {
			log.debug("updateServiceCategories failed fetch all service categories ,error " + serviceCategories.right().value());
			return false;
		}
		for (MigrationCategory newCat : categories) {

			if (newCat.getOldName() == null) {
				// add new
				boolean exist = false;
				for (CategoryDefinition catInDB : serviceCategories.left().value()) {
					if (newCat.getName().equals(catInDB.getName())) {
						exist = true;
						break;
					}
				}
				if (!exist) {
					CategoryDefinition categoryDefinition = new CategoryDefinition(newCat);
					Either<CategoryDefinition, ActionStatus> result = elementOperation.createCategory(categoryDefinition, NodeTypeEnum.ServiceNewCategory, true);
					if (result.isRight()) {
						log.debug("Failed to create service category {} error {}", categoryDefinition, result.right().value());
						return false;
					}
					log.debug("service category {} created", categoryDefinition);
				}
			} else {
				// update exist
				for (CategoryDefinition catInDB : serviceCategories.left().value()) {
					if (newCat.getOldName().equals(catInDB.getName())) {
						Either<CategoryData, TitanOperationStatus> updateSingleResult = updateSingleResourceCategory(newCat, NodeTypeEnum.ServiceNewCategory);
						if (updateSingleResult.isRight()) {
							return false;
						}
						break;
					}
				}
			}
		}
		log.debug("updateServiceCategories ENDED");
		return true;
	}

	private Either<CategoryData, TitanOperationStatus> updateSingleResourceCategory(MigrationCategory newCat, NodeTypeEnum nodetype) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(GraphPropertiesDictionary.NAME.getProperty(), newCat.getOldName());
		Either<List<CategoryData>, TitanOperationStatus> categoryEither = titanGenericDao.getByCriteria(nodetype, properties, CategoryData.class);
		if (categoryEither.isRight() && categoryEither.right().value() != TitanOperationStatus.NOT_FOUND) {
			log.debug("Failed to get {} categories, for name {} error {}", nodetype, newCat.getOldName(), categoryEither.right().value());
			return Either.right(categoryEither.right().value());
		}
		List<CategoryData> categoryList = (categoryEither.isLeft() ? categoryEither.left().value() : null);
		if (categoryList == null) {
			log.debug("No {} categories, for name {} error {}", nodetype, newCat.getOldName());
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		CategoryData categoryData = categoryList.get(0);
		categoryData.getCategoryDataDefinition().setName(newCat.getName());
		categoryData.getCategoryDataDefinition().setIcons(newCat.getIcons());
		categoryData.getCategoryDataDefinition().setNormalizedName(ValidationUtils.normalizeCategoryName4Uniqueness(newCat.getName()));
		Either<CategoryData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(categoryData, CategoryData.class);
		if (updateNode.isRight()) {
			log.debug("Failed to update {} category {} error {}", nodetype, categoryData, updateNode.right().value());
			return Either.right(updateNode.right().value());
		}
		log.debug("Update {} category {} ", nodetype, categoryData);
		return Either.left(updateNode.left().value());
	}

	private boolean updateResourceCategories(List<MigrationCategory> categories) {
		log.debug("updateResourceCategories STARTED");
		Either<List<CategoryDefinition>, ActionStatus> resourceCategories = elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, true);
		if (resourceCategories.isRight()) {
			log.debug("updateResourceCategories failed fetch all resource categories ,error " + resourceCategories.right().value());
			return false;
		}
		for (MigrationCategory newCat : categories) {
			if (newCat.getOldName() == null) {
				// add new
				// check if already created in previous running
				boolean exist = false;
				for (CategoryDefinition catInDB : resourceCategories.left().value()) {
					if (newCat.getName().equals(catInDB.getName())) {
						exist = true;
					}
				}
				if (!exist) {
					CategoryDefinition categoryDefinition = new CategoryDefinition(newCat);
					Either<CategoryDefinition, ActionStatus> resultCat = elementOperation.createCategory(categoryDefinition, NodeTypeEnum.ResourceNewCategory, true);
					if (resultCat.isRight()) {
						log.debug("Failed to create resource category {} error {}", categoryDefinition, resultCat.right().value());
						return false;
					}
					log.debug("resource category {} created", categoryDefinition);

					List<MigrationSubCategory> nSubCat = newCat.getSubcategories();
					List<MigrationSubCategory> newSubcat = nSubCat;
					List<MigrationSubCategory> subcategories = newSubcat;
					for (MigrationSubCategory msubcat : subcategories) {
						SubCategoryDefinition subcat = new SubCategoryDefinition(msubcat);
						Either<SubCategoryDefinition, ActionStatus> resultSubcat = elementOperation.createSubCategory(resultCat.left().value().getUniqueId(), subcat, NodeTypeEnum.ResourceSubcategory, true);
						if (resultSubcat.isRight()) {
							log.debug("Failed to create resource sub category {} error {}", subcat, resultSubcat.right().value());
							return false;
						}
						log.debug("resource sub category {} created for category {}", categoryDefinition, resultCat.left().value().getName());
					}
				}
			} else {
				// update exist
				for (CategoryDefinition catInDB : resourceCategories.left().value()) {
					if (newCat.getOldName().equals(catInDB.getName())) {
						Either<CategoryData, TitanOperationStatus> updateSingleResult = updateSingleResourceCategory(newCat, NodeTypeEnum.ResourceNewCategory);
						if (updateSingleResult.isRight()) {
							return false;
						}

						CategoryData categoryData = updateSingleResult.left().value();
						for (MigrationSubCategory migSubCat : newCat.getSubcategories()) {
							if (migSubCat.getOldName() == null) {
								// create new one
								boolean existSub = false;
								for (SubCategoryDefinition subCatInDb : catInDB.getSubcategories()) {
									if (subCatInDb.getName().equals(migSubCat.getName())) {
										existSub = true;
									}
								}
								if (!existSub) {
									SubCategoryDefinition subcat = new SubCategoryDefinition(migSubCat);

									Either<SubCategoryDefinition, ActionStatus> resultSubcat = elementOperation.createSubCategory((String) categoryData.getUniqueId(), subcat, NodeTypeEnum.ResourceSubcategory, true);
									if (resultSubcat.isRight()) {
										log.debug("Failed to create resource sub category {} error {}", subcat, resultSubcat.right().value());
										return false;
									}
									log.debug("resource sub category {} created for category {}", categoryData, resultSubcat.left().value().getName());
								}
							} else {
								if (updateSingleSubCategory(newCat, migSubCat, updateSingleResult.left().value()) == false) {
									return false;
								}
							}
						}
						break;
					}
				}
			}
		}
		return true;
	}

	private boolean updateSingleSubCategory(MigrationCategory newCat, MigrationSubCategory migSubCat, CategoryData categoryData) {

		Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, TitanOperationStatus> subcategories = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceNewCategory), (String) categoryData.getUniqueId(),
				GraphEdgeLabels.SUB_CATEGORY, NodeTypeEnum.ResourceSubcategory, SubCategoryData.class);

		if (subcategories.isRight()) {
			log.debug("Failed to get resource sub categories, for name {} error {}", newCat.getOldName(), subcategories.right().value());
			return false;
		}

		for (ImmutablePair<SubCategoryData, GraphEdge> pair : subcategories.left().value()) {
			if (pair.getKey().getSubCategoryDataDefinition().getName().equals(migSubCat.getOldName())) {
				SubCategoryData subCategoryData = pair.getKey();
				subCategoryData.getSubCategoryDataDefinition().setName(migSubCat.getName());
				subCategoryData.getSubCategoryDataDefinition().setIcons(migSubCat.getIcons());
				subCategoryData.getSubCategoryDataDefinition().setNormalizedName(ValidationUtils.normalizeCategoryName4Uniqueness(migSubCat.getName()));
				Either<SubCategoryData, TitanOperationStatus> updateSubNode = titanGenericDao.updateNode(subCategoryData, SubCategoryData.class);
				if (updateSubNode.isRight()) {
					log.debug("Failed to update resource sub category {} error {}", subCategoryData, updateSubNode.right().value());
					return false;
				}
				log.debug("Update resource subcategory category {} ", subCategoryData);
				break;
			}
		}
		return true;
	}

	private Map<String, List<MigrationCategory>> createCategoriesFromYml(String categoriesTypesYml) {
		String yamlAsString;
		try {
			yamlAsString = new String(readAllBytes(get(categoriesTypesYml)));
		} catch (Exception e) {
			log.debug("Failed to load category import file exception : ", e);
			return null;
		}

		log.debug("received yaml: {}", yamlAsString);

		Map<String, Object> toscaJson = (Map<String, Object>) new Yaml().load(yamlAsString);
		Map<String, List<MigrationCategory>> allCategories = new HashMap<>();

		Iterator<Entry<String, Object>> categoryEntryItr = toscaJson.entrySet().iterator();
		while (categoryEntryItr.hasNext()) {
			Entry<String, Object> categoryTypeEntry = categoryEntryItr.next();
			String categoryType = categoryTypeEntry.getKey();
			List<MigrationCategory> categoriesPerType = null;
			Map<String, Object> categoryPerType = null;
			switch (categoryType) {
			case ComponentTypeEnum.SERVICE_PARAM_NAME:
				categoryPerType = (Map<String, Object>) categoryTypeEntry.getValue();
				categoriesPerType = createServiceCategories(categoryPerType);
				break;
			case ComponentTypeEnum.RESOURCE_PARAM_NAME:
				categoryPerType = (Map<String, Object>) categoryTypeEntry.getValue();
				categoriesPerType = createResourceCategories(categoryPerType);
				break;
			case ComponentTypeEnum.PRODUCT_PARAM_NAME:
				// TODO
				break;
			default:
				log.debug("Not supported category type - {}", categoryType);
				break;
			}
			if (categoriesPerType != null) {
				allCategories.put(categoryType, categoriesPerType);
			}
		}
		return allCategories;
	}

	private List<MigrationCategory> createServiceCategories(Map<String, Object> categories) {
		List<MigrationCategory> categroiesDef = new ArrayList<>();
		String catName = null;
		List<String> icons = null;
		String oldName = null;
		for (Entry<String, Object> entry : categories.entrySet()) {
			MigrationCategory catDef = new MigrationCategory();
			Map<String, Object> category = (Map<String, Object>) entry.getValue();
			catName = (String) category.get("name");
			catDef.setName(catName);
			icons = (List<String>) category.get("icons");
			catDef.setIcons(icons);
			String normalizedName = ValidationUtils.normalizeCategoryName4Uniqueness(catName);
			catDef.setNormalizedName(normalizedName);
			oldName = (String) category.get("oldName");
			catDef.setOldName(oldName);
			categroiesDef.add(catDef);
		}

		return categroiesDef;
	}

	private List<MigrationCategory> createResourceCategories(Map<String, Object> categoryPerType) {
		List<MigrationCategory> categroiesDef = new ArrayList<>();
		for (Map.Entry<String, Object> entry : categoryPerType.entrySet()) {
			Map<String, Object> category = (Map<String, Object>) entry.getValue();
			MigrationCategory catDef = new MigrationCategory();
			String catName = (String) category.get("name");
			catDef.setName(catName);
			String normalizedName = ValidationUtils.normalizeCategoryName4Uniqueness(catName);
			catDef.setNormalizedName(normalizedName);
			String oldName = (String) category.get("oldName");
			catDef.setOldName(oldName);

			Map<String, Object> subcategories = (Map<String, Object>) category.get("subcategories");
			List<MigrationSubCategory> subcateDef = new ArrayList<>();
			for (Entry<String, Object> subcategory : subcategories.entrySet()) {
				Map<String, Object> subcategoryInfo = (Map<String, Object>) subcategory.getValue();
				MigrationSubCategory subDef = new MigrationSubCategory();
				String subcategoryName = (String) subcategoryInfo.get("name");
				subDef.setName(subcategoryName);
				List<String> subcategoryIcons = (List<String>) subcategoryInfo.get("icons");
				subDef.setIcons(subcategoryIcons);
				normalizedName = ValidationUtils.normalizeCategoryName4Uniqueness(subcategoryName);
				subDef.setNormalizedName(normalizedName);
				oldName = (String) subcategoryInfo.get("oldName");
				subDef.setOldName(oldName);

				subcateDef.add(subDef);
			}

			catDef.setSubcategories(subcateDef);
			categroiesDef.add(catDef);
		}
		return categroiesDef;
	}

	private boolean updateCalculatedEdges() {
		log.debug("update calculated edges STARTED");

		Either<List<ComponentInstanceData>, TitanOperationStatus> allInstances = titanGenericDao.getByCriteria(NodeTypeEnum.ResourceInstance, null, ComponentInstanceData.class);
		if (allInstances.isRight() && !allInstances.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			log.debug("updateCalculatedEdges failed fetch all resource instances ,error " + allInstances.right().value());
			return false;
		}
		if (allInstances.isRight() && allInstances.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			log.debug("updateCalculatedEdges - no VFs");
			return true;
		}
		List<ComponentInstanceData> listOfInstances = allInstances.left().value();
		for (ComponentInstanceData instance : listOfInstances) {
			// check if already have calculated edges
			log.debug("start handle instance {}", instance.getUniqueId());
			boolean needProcess = true;
			Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> vfci = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), instance.getUniqueId(),
					GraphEdgeLabels.CALCULATED_CAPABILITY, NodeTypeEnum.Capability, CapabilityData.class);
			if (vfci.isRight()) {
				if (!vfci.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					log.debug("createCalculatedCapabilitiesForInstance failed to fetch instance for resource " + instance.getComponentInstDataDefinition().getComponentUid() + " error " + vfci.right().value());
					return false;
				}
			} else {
				if (vfci.left().value().size() > 0) {
					needProcess = false;
				}
			}
			Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> vfciReq = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), instance.getUniqueId(),
					GraphEdgeLabels.CALCULATED_REQUIREMENT, NodeTypeEnum.Requirement, RequirementData.class);
			if (vfciReq.isRight()) {
				if (!vfciReq.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					log.debug("createCalculatedCapabilitiesForInstance failed to fetch instance for resource " + instance.getComponentInstDataDefinition().getComponentUid() + " error " + vfciReq.right().value());
					return false;
				}
			} else {
				if (vfciReq.left().value().size() > 0) {
					needProcess = false;
				}
			}
			Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> vfciReqFF = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), instance.getUniqueId(),
					GraphEdgeLabels.CALCULATED_REQUIREMENT_FULLFILLED, NodeTypeEnum.Requirement, RequirementData.class);
			if (vfciReqFF.isRight()) {

				if (!vfciReqFF.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					log.debug("createCalculatedCapabilitiesForInstance failed to fetch instance for resource " + instance.getComponentInstDataDefinition().getComponentUid() + " error " + vfciReqFF.right().value());
					return false;
				}
			} else {
				if (vfciReqFF.left().value().size() > 0) {
					needProcess = false;
				}
			}

			if (needProcess == false) {
				log.debug("updateCalculatedEdges : for instance {} calculated capabilty/requirement already created", instance.getUniqueId());
				continue;
			}
			String originId = instance.getComponentInstDataDefinition().getComponentUid();
			Either<Resource, StorageOperationStatus> resourceE = resourceOperation.getResource(originId, true);
			if (resourceE.isRight()) {
				log.debug("updateCalculatedEdges failed to fetch origin resource with id {} error {}", originId, resourceE.right().value());
				return false;
			}
			Resource resource = resourceE.left().value();
			Map<String, List<RequirementDefinition>> requirements = resource.getRequirements();
			if (createCalculatedRequirementsForInstance(instance, requirements) != true) {
				return false;
			}
			Map<String, List<CapabilityDefinition>> capabilities = resource.getCapabilities();
			if (createCalculatedCapabilitiesForInstance(instance, capabilities) != true) {
				return false;
			}
			log.debug("finish handle instance {}", instance.getUniqueId());
		}
		log.debug("update calculated edges ENDED");
		return true;
	}

	private boolean createCalculatedCapabilitiesForInstance(ComponentInstanceData instance, Map<String, List<CapabilityDefinition>> capabilities) {
		for (Map.Entry<String, List<CapabilityDefinition>> entry : capabilities.entrySet()) {
			for (CapabilityDefinition capability : entry.getValue()) {
				Either<CapabilityData, TitanOperationStatus> capNode = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), capability.getUniqueId(), CapabilityData.class);
				if (capNode.isRight()) {
					log.debug("createCalculatedCapabilitiesForInstance failed to fetch capability node  with id " + capability.getUniqueId() + " error " + capNode.right().value());
					return false;
				}
				Map<String, Object> props = new HashMap<>();
				props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), capability.getName());
				if (fillEdgeProperties(instance, props) != true) {
					return false;
				}

				Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(instance, capNode.left().value(), GraphEdgeLabels.CALCULATED_CAPABILITY, props);
				if (createRelation.isRight()) {
					TitanOperationStatus titanOperationStatus = createRelation.right().value();
					log.debug("Failed to create calculated requirement from component instance {} to requirement {}, error: {}", instance.getUniqueId(), capNode.left().value().getUniqueId(), titanOperationStatus);
					return false;
				}
				log.debug("CALCULATED_CAPABILITY was created from {} to {} with props : {}", capNode.left().value().getUniqueId(), instance.getUniqueId(), props);
			}
		}
		return true;
	}

	private boolean fillEdgeProperties(ComponentInstanceData instance, Map<String, Object> props) {
		if (instance.getComponentInstDataDefinition().getOriginType().equals(OriginTypeEnum.VF)) {
			Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> vfci = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource),
					instance.getComponentInstDataDefinition().getComponentUid(), GraphEdgeLabels.RESOURCE_INST, NodeTypeEnum.ResourceInstance, ComponentInstanceData.class);
			if (vfci.isRight()) {
				log.debug("createCalculatedCapabilitiesForInstance failed to fetch instance for resource " + instance.getComponentInstDataDefinition().getComponentUid() + " error " + vfci.right().value());
				return false;
			}
			ImmutablePair<ComponentInstanceData, GraphEdge> immutablePair = vfci.left().value().get(0);
			String vfciId = immutablePair.getLeft().getUniqueId();
			props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), vfciId);
			props.put(GraphEdgePropertiesDictionary.SOURCE.getProperty(), immutablePair.getLeft().getComponentInstDataDefinition().getComponentUid());

		} else {
			props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), instance.getUniqueId());
			props.put(GraphEdgePropertiesDictionary.SOURCE.getProperty(), instance.getComponentInstDataDefinition().getComponentUid());
		}
		return true;
	}

	private boolean createCalculatedRequirementsForInstance(ComponentInstanceData instance, Map<String, List<RequirementDefinition>> requirements) {
		for (Map.Entry<String, List<RequirementDefinition>> entry : requirements.entrySet()) {
			for (RequirementDefinition requirement : entry.getValue()) {
				Either<RequirementData, TitanOperationStatus> reqNode = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Requirement), requirement.getUniqueId(), RequirementData.class);
				if (reqNode.isRight()) {
					log.debug("updateCalculatedEdges failed to fetch requirement node  with id " + requirement.getUniqueId() + " error " + reqNode.right().value());
					return false;
				}
				Map<String, Object> props = new HashMap<>();
				props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), requirement.getName());

				if (fillEdgeProperties(instance, props) != true) {
					return false;
				}

				Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(instance, reqNode.left().value(), GraphEdgeLabels.CALCULATED_REQUIREMENT, props);
				if (createRelation.isRight()) {
					TitanOperationStatus titanOperationStatus = createRelation.right().value();
					log.debug("Failed to create calculated requirement from component instance {} to requirement {}, error: {}", instance.getUniqueId(), reqNode.left().value().getUniqueId(), titanOperationStatus);
					return false;
				}
				log.debug("CALCULATED_REQUIREMENT was created from {} to {} with props : {}", reqNode.left().value().getUniqueId(), instance.getUniqueId(), props);
			}
		}
		return true;
	}

	private boolean updateRelations() {
		log.debug("update relations and edges STARTED");
		Either<List<RelationshipInstData>, TitanOperationStatus> allRelations = titanGenericDao.getByCriteria(NodeTypeEnum.RelationshipInst, null, RelationshipInstData.class);
		if (allRelations.isRight()) {
			if (allRelations.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				log.debug("updateRelations : No relations to update. updateRelations ENDED");
				return true;
			}
			log.debug("updateRelations : failed to fetch all relation nodes , error ", allRelations.right().value());
			return false;
		}
		for (RelationshipInstData rel : allRelations.left().value()) {
			// rel.set
			if (rel.getCapabilityOwnerId() != null && rel.getRequirementOwnerId() != null) {
				log.debug("updateRelations : for relation {} all fields alredy fixed -> {}", rel.getUniqueId(), rel);
				continue;
			}
			// update capability parameters
			if (updateCapabiltyFieldsInRelation(rel) != true) {
				return false;
			}

			// update requirement parameters and set calculated edge to full
			// filled
			if (updateRequirementFieldsInRelation(rel) != true) {
				return false;
			}

			Either<RelationshipInstData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(rel, RelationshipInstData.class);
			if (updateNode.isRight()) {
				log.debug("updateRelations : failed to update relation node with id {} , error {}", rel.getUniqueId(), updateNode.right().value());
				return false;
			}
			log.debug("Relations was updated with values {}", rel);
		}
		log.debug("update relations and edges ENDED");
		return true;
	}

	private boolean updateRequirementFieldsInRelation(RelationshipInstData rel) {
		Either<ImmutablePair<ComponentInstanceData, GraphEdge>, TitanOperationStatus> reqInst = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.RelationshipInst), rel.getUniqueId(), GraphEdgeLabels.RELATIONSHIP_INST,
				NodeTypeEnum.ResourceInstance, ComponentInstanceData.class);
		if (reqInst.isRight()) {
			log.debug("updateRelations : failed to fetch capability component instance for relation {}, error {}", rel.getUniqueId(), reqInst.right().value());
			return false;
		}
		ComponentInstanceData requirementInstanceData = reqInst.left().value().getLeft();
		ComponentInstanceDataDefinition reqRI = requirementInstanceData.getComponentInstDataDefinition();
		if (reqRI.getOriginType().equals(OriginTypeEnum.VF)) {
			Either<ImmutablePair<ComponentInstanceData, GraphEdge>, TitanOperationStatus> vfcInstInOrigVf = titanGenericDao.getChildByEdgeCriteria(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), reqRI.getComponentUid(),
					GraphEdgeLabels.RESOURCE_INST, NodeTypeEnum.ResourceInstance, ComponentInstanceData.class, null);
			if (vfcInstInOrigVf.isRight()) {
				log.debug("updateRelations : failed to fetch VFC instance in origin VF with id  " + reqRI.getComponentUid() + ", error ", vfcInstInOrigVf.right().value());
				return false;
			}
			rel.setRequirementOwnerId(vfcInstInOrigVf.left().value().getLeft().getUniqueId());
		} else {
			rel.setRequirementOwnerId(reqRI.getUniqueId());
		}
		// get vertex
		Either<TitanVertex, TitanOperationStatus> vertexReqRI = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), requirementInstanceData.getUniqueId());
		if (vertexReqRI.isRight()) {
			log.debug("updateRelations : failed to fetch veterx for instance  {}, error {}", requirementInstanceData.getUniqueId(), vertexReqRI.right().value());
			return false;
		}
		String[] splitIds = rel.getUniqueId().split("\\.");
		String reqName = splitIds[splitIds.length - 1];
		Map<String, Object> props = new HashMap<>();
		props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), reqName);
		Either<List<Edge>, TitanOperationStatus> edgesForNode = titanGenericDao.getOutgoingEdgesByCriteria(vertexReqRI.left().value(), GraphEdgeLabels.CALCULATED_REQUIREMENT, props);
		if (edgesForNode.isRight()) {
			log.debug("updateRelations : failed to fetch edges for instance {}  error {}", requirementInstanceData.getUniqueId(), edgesForNode.right().value());
			return false;
		}
		Edge edge = edgesForNode.left().value().get(0);
		String reqId = (String) titanGenericDao.getProperty((TitanVertex) edge.inVertex(), GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		rel.setRequirementId(reqId);

		// change edge label
		TitanEdge newEdge = (TitanEdge) vertexReqRI.left().value().addEdge(GraphEdgeLabels.CALCULATED_REQUIREMENT_FULLFILLED.getProperty(), edge.inVertex());
		titanGenericDao.setProperties(newEdge, titanGenericDao.getProperties(edge));
		edge.remove();

		log.debug("Edge was changed to CALCULATED_REQUIREMENT_FULLFILLED for relation between {} and {}", reqId, requirementInstanceData.getUniqueId());

		return true;
	}

	public boolean updateCapabiltyFieldsInRelation(RelationshipInstData rel) {
		// update capability parameters
		Either<ImmutablePair<ComponentInstanceData, GraphEdge>, TitanOperationStatus> capInst = titanGenericDao.getChildByEdgeCriteria(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.RelationshipInst), rel.getUniqueId(),
				GraphEdgeLabels.CAPABILITY_NODE, NodeTypeEnum.ResourceInstance, ComponentInstanceData.class, null);
		if (capInst.isRight()) {
			log.debug("updateRelations : failed to fetch capabilty component instance for relation {}, error {}", rel.getUniqueId(), capInst.right().value());
			return false;
		}
		ComponentInstanceData capabiltyInstanceData = capInst.left().value().getLeft();
		ComponentInstanceDataDefinition capRI = capabiltyInstanceData.getComponentInstDataDefinition();
		if (capRI.getOriginType().equals(OriginTypeEnum.VF)) {
			Either<ImmutablePair<ComponentInstanceData, GraphEdge>, TitanOperationStatus> vfcInstInOrigVf = titanGenericDao.getChildByEdgeCriteria(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), capRI.getComponentUid(),
					GraphEdgeLabels.RESOURCE_INST, NodeTypeEnum.ResourceInstance, ComponentInstanceData.class, null);
			if (vfcInstInOrigVf.isRight()) {
				log.debug("updateRelations : failed to fetch VFC instance in origin VF with id  " + capRI.getComponentUid() + ", error ", vfcInstInOrigVf.right().value());
				return false;
			}
			rel.setCapabilityOwnerId(vfcInstInOrigVf.left().value().getLeft().getUniqueId());
		} else {
			rel.setCapabilityOwnerId(capRI.getUniqueId());
		}

		// get vertex
		Either<TitanVertex, TitanOperationStatus> vertexCapRI = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), capabiltyInstanceData.getUniqueId());
		if (vertexCapRI.isRight()) {
			log.debug("updateRelations : failed to fetch veterx for instance {} , error {}", capabiltyInstanceData.getUniqueId(), vertexCapRI.right().value());
			return false;
		}
		// String[] splitIds = rel.getUniqueId().split("\\.");
		String capName = (String) capInst.left().value().getRight().getProperties().get(GraphEdgePropertiesDictionary.NAME.getProperty());// splitIds[splitIds.length
																																			// - 1];
		Map<String, Object> props = new HashMap<>();
		props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), capName);
		Either<List<Edge>, TitanOperationStatus> edgesForNode = titanGenericDao.getOutgoingEdgesByCriteria(vertexCapRI.left().value(), GraphEdgeLabels.CALCULATED_CAPABILITY, props);
		if (edgesForNode.isRight()) {
			log.debug("updateRelations : failed to fetch edges for instance {} , error {}", capabiltyInstanceData.getUniqueId(), edgesForNode.right().value());
			return false;
		}
		Edge edge = edgesForNode.left().value().get(0);
		String capId = (String) titanGenericDao.getProperty((TitanVertex) edge.inVertex(), GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		rel.setCapabiltyId(capId);

		return true;
	}

	private Either<List<String>, StorageOperationStatus> handleVfGroup(ResourceMetadataData metadata) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());
		props.put(GraphPropertiesDictionary.NAME.getProperty(), metadata.getMetadataDataDefinition().getName());

		List<String> finished = new ArrayList<>();

		Either<List<ResourceMetadataData>, TitanOperationStatus> allVFByName = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		if (allVFByName.isRight()) {
			log.debug("fixDerivedFv failed fetch all VF resources,error {}", allVFByName.right().value());
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}
		Set<String> nonDuplicatedId = new HashSet<>();
		String uuid10 = null;
		for (ResourceMetadataData mdata : allVFByName.left().value()) {
			String version = mdata.getMetadataDataDefinition().getVersion();
			if (version.equals("1.0")) {
				uuid10 = mdata.getMetadataDataDefinition().getUUID();
				// break;
			}
			nonDuplicatedId.add((String) mdata.getUniqueId());
		}
		if (uuid10 == null) {
			uuid10 = allVFByName.left().value().get(0).getMetadataDataDefinition().getUUID();
		}
		props.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());
		props.put(GraphPropertiesDictionary.UUID.getProperty(), uuid10);

		Either<List<ResourceMetadataData>, TitanOperationStatus> allVFByUUID = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		if (allVFByUUID.isRight()) {
			log.debug("fixDerivedFv failed fetch all VF resources by UUID {} ,error {}", uuid10, allVFByUUID.right().value());
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}
		for (ResourceMetadataData mdata : allVFByUUID.left().value()) {
			nonDuplicatedId.add((String) mdata.getUniqueId());
		}
		Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
		if (graph.isRight()) {
			log.debug("fixDerivedFv failed - No titan graph ,error {}", graph.right().value());
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}
		// Map<String, String> derivedMapping = new HashMap<>();
		for (String resourceId : nonDuplicatedId) {
			// StorageOperationStatus handleSingleVfResult =
			// handleSingleVf(finished, derivedMapping, resourceId);
			StorageOperationStatus handleSingleVfResult = handleSingleVf(finished, resourceId);
			if (!handleSingleVfResult.equals(StorageOperationStatus.OK)) {
				log.debug("fixDerivedFv failed - handleSingleVfResult failed for resource {} ,error {}", resourceId, handleSingleVfResult);
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
		}
		return Either.left(finished);
	}

	// private StorageOperationStatus handleSingleVf(List<String> finished,
	// Map<String, String> derivedMapping, String resourceId) {
	private StorageOperationStatus handleSingleVf(List<String> finished, String resourceId) {
		Either<TitanVertex, TitanOperationStatus> vertexByProperty = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), resourceId);
		if (vertexByProperty.isRight()) {
			log.debug("fixDerivedFv failed to fetch resource by id {} ,error {}", resourceId, vertexByProperty.right().value());
			return StorageOperationStatus.GENERAL_ERROR;
		}
		Vertex vertexR = vertexByProperty.left().value();
		Iterator<Vertex> vertexDIter = vertexR.vertices(Direction.OUT, GraphEdgeLabels.DERIVED_FROM.getProperty());
		if (vertexDIter != null && vertexDIter.hasNext()) {
			// move edges
			// must be only one
			TitanVertex vertexD = (TitanVertex) vertexDIter.next();
			String idDerived = (String) titanGenericDao.getProperty(vertexD, GraphPropertiesDictionary.UNIQUE_ID.getProperty());

			// TODO clone resource

			// TODO add instance of new resource to VF

			// add to vf instance of vfc
			finished.add(resourceId);
		} else {
			log.debug("No derived edges for resource  id {}", resourceId);
		}
		return StorageOperationStatus.OK;
	}

	private boolean updateComponentInstanceType() {
		log.debug("update component instances type STARTED");
		Either<List<ComponentInstanceData>, TitanOperationStatus> allInstances = titanGenericDao.getByCriteria(NodeTypeEnum.ResourceInstance, null, ComponentInstanceData.class);
		if (allInstances.isRight()) {
			if (allInstances.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				log.debug("updateComponentInstanceType:  no instances ti update ");
				return true;
			}
			log.debug("updateComponentInstanceType failed fetch all resource instances ,error " + allInstances.right().value());
			return false;
		}

		List<ComponentInstanceData> listOfInstances = allInstances.left().value();
		for (ComponentInstanceData instance : listOfInstances) {
			String originId = instance.getComponentInstDataDefinition().getComponentUid();
			Either<ComponentMetadataData, TitanOperationStatus> nodeResource = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), originId, ComponentMetadataData.class);
			if (nodeResource.isRight()) {
				log.debug("updateComponentInstanceType failed to fetch origin resource with id {} error {}", originId, nodeResource.right().value());
				return false;
			}
			ResourceTypeEnum resourceType = ((ResourceMetadataDataDefinition) nodeResource.left().value().getMetadataDataDefinition()).getResourceType();
			if (resourceType == null) {
				log.debug("updateComponentInstanceType failed, no resource type for origin resource with id " + originId);
				return false;
			}
			OriginTypeEnum originType;
			switch (resourceType) {
			case VF:
				originType = OriginTypeEnum.VF;
				break;
			case VFC:
				originType = OriginTypeEnum.VFC;
				break;
			case VL:
				originType = OriginTypeEnum.VL;
				break;
			case CP:
				originType = OriginTypeEnum.CP;
				break;
			case CVFC:
				originType = OriginTypeEnum.CVFC;
				break;
			default:
				log.debug("updateComponentInstanceType failed, no supported resource type {} for origin resource with id {}", resourceType, originId);
				return false;
			}
			instance.getComponentInstDataDefinition().setOriginType(originType);

			Either<ComponentInstanceData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(instance, ComponentInstanceData.class);
			if (updateNode.isRight()) {
				log.debug("updateComponentInstanceType failed, failed to update component instance node with id  " + instance.getUniqueId() + " error " + updateNode.right().value());
				return false;
			}
			log.debug("For instance with id {} the origin type was detected as {}", instance.getUniqueId(), originType);
		}
		log.debug("update component instances type ENDED");
		return true;
	}

	private boolean addResourceCounterToResources() {

		Either<List<ResourceMetadataData>, TitanOperationStatus> allResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, null, ResourceMetadataData.class);
		if (allResources.isRight()) {
			if (allResources.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				log.debug("addResourceCounterToResources - no resources");
				return true;
			}
			log.debug("addResourceCounterToResources failed fetch all resources,error {}", allResources.right().value());
			return false;
		}
		for (ResourceMetadataData resource : allResources.left().value()) {
			Either<TitanVertex, TitanOperationStatus> vertexByProperty = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resource.getUniqueId());
			if (vertexByProperty.isRight()) {
				log.error("failed to add instanceCounter to VF {} . error is: {}", resource.getUniqueId(), vertexByProperty.right().value().name());
				return false;
			}
			Vertex vfVertex = vertexByProperty.left().value();
			if (!vfVertex.property(GraphPropertiesDictionary.INSTANCE_COUNTER.getProperty()).isPresent()) {
				vfVertex.property(GraphPropertiesDictionary.INSTANCE_COUNTER.getProperty(), 0);
			}
		}
		return true;
	}

	private boolean fixDerivedVf() {

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), ResourceTypeEnum.VF.name());
		Either<List<ResourceMetadataData>, TitanOperationStatus> allVF = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		if (allVF.isRight()) {
			if (allVF.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				log.debug("fixDerivedVf - no VFs");
				return true;
			}
			log.debug("fixDerivedFv failed fetch all VF resources,error {}", allVF.right().value());
			return false;
		}

		Map<String, String> vfUuidToVfcUuid = new HashMap<String, String>();
		for (ResourceMetadataData metadata : allVF.left().value()) {
			Either<Resource, StorageOperationStatus> eitherResource = resourceOperation.getResource(metadata.getMetadataDataDefinition().getUniqueId(), true);
			if (eitherResource.isRight()) {
				log.error("failed to migrate VF {} from version 1602 to version 1604. error is: {}", metadata.getMetadataDataDefinition().getUniqueId(), eitherResource.right().value().name());
				return false;
			}
			Resource vfResource = eitherResource.left().value();
			if (vfResource.getDerivedFrom() == null || vfResource.getDerivedFrom().isEmpty()) {
				continue;
			}
			Boolean isVfDeleted = vfResource.getIsDeleted();
			String vfUUID = vfResource.getUUID();
			String vfcUUID = vfUuidToVfcUuid.getOrDefault(vfUUID, null);
			if (vfcUUID == null) {
				vfcUUID = UUID.randomUUID().toString();
				vfUuidToVfcUuid.put(vfUUID, vfcUUID);
			}

			// handle lifecycle
			String vfUniqueId = vfResource.getUniqueId();
			LifecycleStateEnum vfcTargetState = vfResource.getLifecycleState();
			if (vfcTargetState.equals(LifecycleStateEnum.READY_FOR_CERTIFICATION) || vfcTargetState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS)) {
				User user = new User();
				user.setUserId(vfResource.getLastUpdaterUserId());
				Either<? extends Component, StorageOperationStatus> checkinComponent = lifecycleOperaion.checkinComponent(NodeTypeEnum.Resource, vfResource, user, user, true);
				if (checkinComponent.isRight()) {
					log.error("failed to checkin VF {}. error={}", vfUniqueId, checkinComponent.right().value().name());
					return false;
				}
			} else if (vfcTargetState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
				vfcTargetState = LifecycleStateEnum.NOT_CERTIFIED_CHECKIN;
			}

			// delete VF Properties
			List<PropertyDefinition> properties = vfResource.getProperties();
			if (properties != null && !properties.isEmpty()) {
				Either<Map<String, PropertyDefinition>, StorageOperationStatus> deleteAllProperties = propertyOperation.deleteAllPropertiesAssociatedToNode(NodeTypeEnum.Resource, vfUniqueId);
				if (deleteAllProperties.isRight() && !deleteAllProperties.right().value().equals(StorageOperationStatus.NOT_FOUND) && !deleteAllProperties.right().value().equals(StorageOperationStatus.OK)) {
					log.error("failed to delete properties of VF {} . error is: {}", metadata.getMetadataDataDefinition().getUniqueId(), deleteAllProperties.right().value().name());
					return false;
				}
			}
			// delete VF Additional Info
			List<AdditionalInformationDefinition> additionalInformation = vfResource.getAdditionalInformation();
			if (additionalInformation != null && !additionalInformation.isEmpty()) {
				Either<AdditionalInformationDefinition, StorageOperationStatus> deleteAllAdditionalInformationParameters = additionalInformationOperation.deleteAllAdditionalInformationParameters(NodeTypeEnum.Resource, vfUniqueId, true);
				if (deleteAllAdditionalInformationParameters.isRight() && !deleteAllAdditionalInformationParameters.right().value().equals(StorageOperationStatus.OK)
						&& !deleteAllAdditionalInformationParameters.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
					log.error("failed to delete properties of VF {} . error is: {}", metadata.getMetadataDataDefinition().getUniqueId(), deleteAllAdditionalInformationParameters.right().value().name());
					return false;
				}
			}
			// delete VF derivedFrom
			GraphRelation derivedFromRelation = new GraphRelation(GraphEdgeLabels.DERIVED_FROM.getProperty());
			derivedFromRelation.setFrom(new RelationEndPoint(NodeTypeEnum.Resource, UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), vfUniqueId));
			Either<GraphRelation, TitanOperationStatus> deleteDerivedFromRelation = titanGenericDao.deleteOutgoingRelation(derivedFromRelation);
			if (deleteDerivedFromRelation.isRight()) {
				log.error("failed to delete derivedFrom relation of VF {} . error is: {}", metadata.getMetadataDataDefinition().getUniqueId(), deleteDerivedFromRelation.right().value().name());
				return false;
			}

			// create VFC
			Either<Resource, StorageOperationStatus> createVFC = createVFC(metadata, vfResource, vfcUUID, vfcTargetState);
			if (createVFC.isRight()) {
				log.error("failed to split VF {} to VFC. error is: {}", metadata.getMetadataDataDefinition().getUniqueId(), createVFC.right().value().name());
				return false;
			}
			Resource vfcResource = createVFC.left().value();
			if (!createVfcInstanceOnVf(vfcResource, vfUniqueId)) {
				return false;
			}
			// update VFC to deleted if required
			if (isVfDeleted != null && isVfDeleted) {
				Either<Component, StorageOperationStatus> markResourceToDelete = resourceOperation.markComponentToDelete(vfcResource, true);
				if (markResourceToDelete.isRight()) {
					log.error("failed to mark isDeleted on VFC {} . error is: {}", vfcResource.getUniqueId(), markResourceToDelete.right().value().name());
					return false;
				}
			}

		}
		return true;
	}

	private Either<Resource, StorageOperationStatus> createVFC(ResourceMetadataData metadata, Resource vfcResource, String uuid, LifecycleStateEnum vfcTargetState) {

		Boolean highestVersion = vfcResource.isHighestVersion();
		// Resource vfcResource = new Resource((ResourceMetadataDefinition)
		// vfResource.getComponentMetadataDefinition());
		// String componentName = vfcResource.getName()+"VFC";
		// vfcResource.setName(componentName);
		// vfcResource.setNormalizedName(ValidationUtils.normaliseComponentName(componentName));
		// vfcResource.setSystemName(ValidationUtils.convertToSystemName(componentName));
		vfcResource.setUniqueId(null);
		vfcResource.setUUID(uuid);
		vfcResource.setAllVersions(null);
		vfcResource.setArtifacts(null);
		vfcResource.setDeploymentArtifacts(null);
		vfcResource.setComponentInstances(null);
		vfcResource.setComponentInstancesProperties(null);
		vfcResource.setComponentInstancesRelations(null);
		vfcResource.setResourceType(ResourceTypeEnum.VFC);
		vfcResource.setIsDeleted(false);

		vfcResource.setLifecycleState(vfcTargetState);
		// vfcResource.setDerivedFrom(vfResource.getDerivedFrom());
		// vfcResource.setProperties(vfResource.getProperties());
		// vfcResource.setAdditionalInformation(vfResource.getAdditionalInformation());
		// vfcResource.setCategories(vfResource.getCategories());
		// vfcResource.setTags(vfResource.getTags());

		Either<Resource, StorageOperationStatus> createResource = resourceOperation.createResource(vfcResource, true);
		if (createResource.isRight()) {
			return createResource;
		}
		Resource afterCreateResource = createResource.left().value();
		Either<TitanVertex, TitanOperationStatus> vertexByProperty = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), afterCreateResource.getUniqueId());
		if (vertexByProperty.isRight()) {
			return createResource;
		}
		Vertex newVfcVertex = vertexByProperty.left().value();
		newVfcVertex.property(GraphPropertiesDictionary.UUID.getProperty(), uuid);
		if (!highestVersion) {
			newVfcVertex.property(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), false);
		}
		return createResource;
	}

	private boolean createVfcInstanceOnVf(Resource vfcResource, String vfUniqueId) {
		// create VFC instance on VF
		ComponentInstance componentInstance = new ComponentInstance();
		componentInstance.setComponentUid(vfcResource.getUniqueId());
		componentInstance.setPosX("550");
		componentInstance.setPosY("300");
		componentInstance.setName(vfcResource.getName());
		componentInstance.setIcon(vfcResource.getIcon());
		componentInstance.setToscaComponentName(vfcResource.getToscaResourceName());
		Either<String, Boolean> handleNameLogic = handleNameLogic(componentInstance, vfUniqueId, vfcResource.getName());
		if (handleNameLogic.isRight()) {
			log.error("failed to create logical name for vfc instance");
			return false;
		}
		Either<ComponentInstance, StorageOperationStatus> createComponentInstance = componentInstanceOperaion.createComponentInstance(vfUniqueId, NodeTypeEnum.Resource, handleNameLogic.left().value(), componentInstance, NodeTypeEnum.Resource, true);

		if (createComponentInstance.isRight()) {
			log.error("failed to create vfc instance on vf {}. error: {}", vfUniqueId, createComponentInstance.right().value().name());
			return false;
		}
		return true;
	}

	private Either<String, Boolean> handleNameLogic(ComponentInstance componentInstance, String containerComponentId, String resourceName) {

		Either<Integer, StorageOperationStatus> componentInNumberStatus = resourceOperation.increaseAndGetComponentInstanceCounter(containerComponentId, true);

		if (componentInNumberStatus.isRight()) {
			log.debug("Failed to get component instance number for container component {} ", containerComponentId);
			return Either.right(false);
		}
		String resourceInNumber = componentInNumberStatus.left().value().toString();
		componentInstance.setComponentName(resourceName);
		componentInstance.setName(resourceName);
		String logicalName = componentInstanceOperaion.createComponentInstLogicalName(resourceInNumber, resourceName);

		Boolean eitherValidation = validateComponentInstanceName(logicalName, componentInstance, true);
		if (!eitherValidation) {
			return Either.right(false);
		}

		return Either.left(resourceInNumber);
	}

	private Boolean validateComponentInstanceName(String resourceInstanceName, ComponentInstance resourceInstance, boolean isCreate) {

		if (!ValidationUtils.validateStringNotEmpty(resourceInstanceName)) {
			return false;
		}
		resourceInstance.setNormalizedName(ValidationUtils.normalizeComponentInstanceName(resourceInstanceName));
		if (!isCreate) {
			if (!ValidationUtils.validateResourceInstanceNameLength(resourceInstanceName)) {
				return false;
			}
			if (!ValidationUtils.validateResourceInstanceName(resourceInstanceName)) {
				return false;
			}
		}

		return true;

	}

	public boolean migrate1604to1607(String appConfigDir) {
		log.debug("Started the migration procedure from version 1604 to version 1607 ...");
		log.debug("Getting all resources with resources");
		boolean result = false;
		Either<Boolean, StorageOperationStatus> resourceEither = null;
		try {
			Either<List<ResourceMetadataData>, TitanOperationStatus> allResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, null, ResourceMetadataData.class);
			if (allResources.isRight()) {
				log.error("Couldn't get resources from DB, error: {}", allResources.right().value());
				result = false;
				return result;
			}
			List<ResourceMetadataData> resourcesList = allResources.left().value();
			if (resourcesList == null) {
				log.error("Couldn't get resources from DB, no resources found");
				result = false;
				return result;
			}
			log.debug("Found {} resources", resourcesList.size());
			for (ResourceMetadataData resource : resourcesList) {
				String resourceName = resource.getMetadataDataDefinition().getName();
				log.debug("Checking resource {}", resourceName);
				if (isNormative(resourceName)) {
					resourceEither = changeNormativeTypeName(resource);
					if (resourceEither.isRight()) {
						log.error("DB error during name changing");
						result = false;
						return result;
					}
				}
				if (((ResourceMetadataDataDefinition) resource.getMetadataDataDefinition()).getResourceType().name().equals("VF")) {
					resourceEither = setVfToscaResourceName(resource);
					if (resourceEither.isRight()) {
						log.error("DB error during tosca resource name setting");
						result = false;
						return result;
					}
				}
			}
			result = addInvariantUUIDs(appConfigDir);
		} finally {
			if (!result) {
				titanGenericDao.rollback();
				log.debug("**********************************************");
				log.debug("The migration procedure from version 1604 to version 1607 FAILED!!");
				log.debug("**********************************************");
			} else {
				titanGenericDao.commit();
				log.debug("**********************************************");
				log.debug("The migration procedure from version 1604 to version 1607 ended successfully!");
				log.debug("**********************************************");
			}
		}

		return result;
	}

	private boolean addInvariantUUIDs(String appConfigDir) {
		log.debug("Started adding of InvariantUUID ...");
		log.debug("Getting all resources with highest version");

		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

		List<ComponentMetadataData> fullComponentList = new ArrayList<ComponentMetadataData>();

		// getting resources
		Either<List<ResourceMetadataData>, TitanOperationStatus> allHighestVersionResources = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, props, ResourceMetadataData.class);
		if (allHighestVersionResources.isRight()) {
			log.error("Couldn't get resources with highest version from DB, error: {}", allHighestVersionResources.right().value());
			return false;
		}
		List<ResourceMetadataData> allHighestVersionResourcesAL = allHighestVersionResources.left().value();
		if (allHighestVersionResourcesAL == null) {
			log.error("Couldn't get resources with highest version from DB, no resources found");
			return false;
		}
		log.debug("Found {} resources", allHighestVersionResourcesAL.size());
		fullComponentList.addAll(allHighestVersionResourcesAL);

		// getting services
		Either<List<ServiceMetadataData>, TitanOperationStatus> allHighestVersionServices = titanGenericDao.getByCriteria(NodeTypeEnum.Service, props, ServiceMetadataData.class);
		if (allHighestVersionServices.isRight()) {
			log.error("Couldn't get services with highest version from DB, error: {}", allHighestVersionServices.right().value());
			return false;
		}
		List<ServiceMetadataData> allHighestVersionServicesAL = allHighestVersionServices.left().value();
		if (allHighestVersionServicesAL == null) {
			log.error("Couldn't get services with highest version from DB, no services found");
			return false;
		}
		log.debug("Found {} services", allHighestVersionServicesAL.size());
		fullComponentList.addAll(allHighestVersionServicesAL);

		List<ComponentMetadataData> reducedComponentsAL = reduceHighestVersionResourcesList(fullComponentList);

		// getting products
		Either<List<ProductMetadataData>, TitanOperationStatus> allHighestVersionProducts = titanGenericDao.getByCriteria(NodeTypeEnum.Product, props, ProductMetadataData.class);
		if (allHighestVersionProducts.isRight()) {
			log.error("Couldn't get products with highest version from DB, error: {}", allHighestVersionProducts.right().value());
			return false;
		}
		List<ProductMetadataData> allHighestVersionProductsAL = allHighestVersionProducts.left().value();
		if (allHighestVersionProductsAL == null) {
			log.error("Couldn't get products with highest version from DB, no products found");
			return false;
		}
		log.debug("Found {} products", allHighestVersionProductsAL.size());

		List<ComponentMetadataData> fullProductList = new ArrayList<ComponentMetadataData>();
		fullProductList.addAll(allHighestVersionProductsAL);
		List<ComponentMetadataData> reducedProductAL = reduceHighestVersionResourcesList(fullProductList);

		for (ComponentMetadataData product : reducedProductAL) {
			if (!setProductInvariantUUIDIfExists((ProductMetadataData) product)) {
				return false;
			}
		}
		reducedComponentsAL.addAll(reducedProductAL);

		log.debug("Reduced list of Highest Version Components contains {} components", reducedComponentsAL.size());
		for (ComponentMetadataData componentMetaData : reducedComponentsAL) {

			String invariantUUID = componentMetaData.getMetadataDataDefinition().getInvariantUUID();
			log.debug("old invariantUUID {}", invariantUUID);
			if (invariantUUID == null || invariantUUID.isEmpty()) {
				invariantUUID = UniqueIdBuilder.buildInvariantUUID();
				componentMetaData.getMetadataDataDefinition().setInvariantUUID(invariantUUID);
			}
			log.debug("new invariantUUID {}", componentMetaData.getMetadataDataDefinition().getInvariantUUID());
			Either<ComponentMetadataData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(componentMetaData, ComponentMetadataData.class);
			if (updateNode.isRight()) {
				log.error("DB error during while updating component {}, error: {}", componentMetaData.getMetadataDataDefinition().getName(), updateNode.right().value());
				return false;
			}
			log.debug("updated invariantUUID {}", updateNode.left().value().getMetadataDataDefinition().getInvariantUUID());
			if (!isOnlyVersion(componentMetaData)) {
				ComponentOperation componentOperation = null;
				switch (NodeTypeEnum.getByName(componentMetaData.getLabel())) {
				case Resource:
					componentOperation = resourceOperation;
					break;
				case Service:
					componentOperation = serviceOperation;
					break;
				case Product:
					componentOperation = productOperation;
					break;
				default:
					break;
				}
				Either<Component, StorageOperationStatus> getComponentResult = componentOperation.getComponent((String) componentMetaData.getUniqueId(), true);
				if (getComponentResult.isRight()) {
					log.error("DB error during while getting component with uniqueID {}, error: {}", componentMetaData.getUniqueId(), getComponentResult.right().value());
					return false;
				}
				Component component = getComponentResult.left().value();
				if (component == null) {
					log.error("The component received from DB is empty");
					return false;
				}

				Map<String, String> allVersions = component.getAllVersions();
				log.debug("found {} versions for component {}", allVersions.size(), component.getName());
				Either<Boolean, StorageOperationStatus> resEither = updateAllVersions(allVersions, invariantUUID);
				if (resEither.isRight()) {
					log.error("DB error during invariantUUID adding");
					return false;
				}
			}
		}
		return true;
	}

	private boolean isOnlyVersion(ComponentMetadataData componentMetaData) {
		String version = componentMetaData.getMetadataDataDefinition().getVersion();
		if (version.equals("0.1"))
			return true;
		return false;
	}

	private boolean setProductInvariantUUIDIfExists(ProductMetadataData product) {
		Either<TitanVertex, TitanOperationStatus> getVertexRes = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), product.getUniqueId());
		if (getVertexRes.isRight()) {
			log.error("DB error during retrieving product vertex {}", product.getMetadataDataDefinition().getName());
			return false;
		}
		Vertex productVertex = getVertexRes.left().value();
		String invariantUUID = productVertex.value(GraphPropertiesDictionary.CONSTANT_UUID.getProperty());
		if (invariantUUID != null && !invariantUUID.isEmpty()) {
			product.getMetadataDataDefinition().setInvariantUUID(invariantUUID);
		}
		return true;
	}

	private Either<Boolean, StorageOperationStatus> updateAllVersions(Map<String, String> allVersions, String invariantUUID) {

		if (allVersions != null) {
			for (String uniqueID : allVersions.values()) {
				Either<ComponentMetadataData, TitanOperationStatus> getNodeResult = titanGenericDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), uniqueID, ComponentMetadataData.class);
				if (getNodeResult.isRight()) {
					log.error("DB error during while getting component with uniqueID {}, error: {}", uniqueID, getNodeResult.right().value());
					return Either.right(StorageOperationStatus.GENERAL_ERROR);
				}
				ComponentMetadataData component = getNodeResult.left().value();
				component.getMetadataDataDefinition().setInvariantUUID(invariantUUID);
				Either<ComponentMetadataData, TitanOperationStatus> updateNodeResult = titanGenericDao.updateNode(component, ComponentMetadataData.class);
				log.debug("updated child invariantUUID {}", updateNodeResult.left().value().getMetadataDataDefinition().getInvariantUUID());
				if (updateNodeResult.isRight()) {
					log.error("DB error during while updating component {}, error: {}", component.getMetadataDataDefinition().getName(), updateNodeResult.right().value());
					return Either.right(StorageOperationStatus.GENERAL_ERROR);
				}
			}
		}
		return Either.left(true);
	}

	private List<ComponentMetadataData> reduceHighestVersionResourcesList(List<ComponentMetadataData> allHighestVersionResources) {
		List<ComponentMetadataData> resultList = null;
		Map<String, ComponentMetadataData> resultHM = new HashMap<String, ComponentMetadataData>();
		for (ComponentMetadataData resource : allHighestVersionResources) {
			if (resource.getMetadataDataDefinition().getInvariantUUID() != null && !resource.getMetadataDataDefinition().getInvariantUUID().isEmpty()) {
				log.debug("invariantUUID {} ", resource.getMetadataDataDefinition().getInvariantUUID());
				continue;
			}
			String curUUID = resource.getMetadataDataDefinition().getUUID();
			if (resultHM.containsKey(curUUID)) {
				int isHighest = resultHM.get(curUUID).getMetadataDataDefinition().getVersion().compareTo(resource.getMetadataDataDefinition().getVersion());
				if (isHighest > 0) {
					log.debug("version {} is great than {} ", resultHM.get(curUUID).getMetadataDataDefinition().getVersion(), resource.getMetadataDataDefinition().getVersion());
					continue;
				}
			}
			resultHM.put(curUUID, resource);
		}
		resultList = new ArrayList<ComponentMetadataData>(resultHM.values());
		return resultList;
	}

	private boolean isNormative(String resourceName) {
		for (int i = 0; i < NORMATIVE_OLD_NAMES.length; ++i) {
			if (NORMATIVE_OLD_NAMES[i].equals(resourceName))
				return true;
		}
		return false;
	}

	private Either<Boolean, StorageOperationStatus> changeNormativeTypeName(ResourceMetadataData resource) {

		String resourceName = resource.getMetadataDataDefinition().getName();

		if (resourceName != null && !resourceName.isEmpty()) {
			log.debug("Found normative type to change - {}", resourceName);
			String oldName = resourceName;
			String[] splitedName = resourceName.split("\\.");
			String newName = splitedName[splitedName.length - 1];
			String newSystemName = ValidationUtils.convertToSystemName(newName);
			String newNormalizedName = ValidationUtils.normaliseComponentName(newName);
			log.debug("Setting name to be {}", newName);

			resource.getMetadataDataDefinition().setName(newName);
			log.debug("Setting system name to be {}", newSystemName);
			resource.getMetadataDataDefinition().setSystemName(newSystemName);
			log.debug("Setting normalized name to be {}", newNormalizedName);
			resource.getMetadataDataDefinition().setNormalizedName(newNormalizedName);
			log.debug("Updating tag in metadata to be {}", newName);
			resource.getMetadataDataDefinition().getTags().remove(oldName);
			resource.getMetadataDataDefinition().getTags().add(newName);

			log.debug("Creating tag node with name {}", newName);
			TagData tagData = new TagData();
			tagData.setName(newName);
			Either<TagData, TitanOperationStatus> createNode = titanGenericDao.createNode(tagData, TagData.class);
			if (createNode.isRight()) {
				log.error("Error while creating tag node {}, error: {}.", newName, createNode.right().value());
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
			
			Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(resource, ResourceMetadataData.class);
			if (updateNode.isRight()) {
				log.error("DB error during while updating normative type {}, error: {}", resource.getMetadataDataDefinition().getName(), updateNode.right().value());
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
			log.debug("Normative type {} was successfully updated", resource.getMetadataDataDefinition().getName());
			return Either.left(true);
		}

		return Either.left(false);
	}

	private Either<Boolean, StorageOperationStatus> generateAndSetToscaResourceName(ResourceMetadataData resource, String toscaResourceName) {
		if (toscaResourceName == null) {
			toscaResourceName = CommonBeUtils.generateToscaResourceName(((ResourceMetadataDataDefinition) resource.getMetadataDataDefinition()).getResourceType().name(), resource.getMetadataDataDefinition().getSystemName());
		}
		Either<Boolean, StorageOperationStatus> validateToscaResourceNameExists = resourceOperation.validateToscaResourceNameExists(toscaResourceName);
		if (validateToscaResourceNameExists.isRight()) {
			StorageOperationStatus storageOperationStatus = validateToscaResourceNameExists.right().value();
			log.error("Couldn't validate toscaResourceName uniqueness - error: {}", storageOperationStatus);
			return Either.right(storageOperationStatus);
		}
		if (validateToscaResourceNameExists.left().value()) {
			log.debug("Setting tosca resource name to be {}", toscaResourceName);
			((ResourceMetadataDataDefinition) resource.getMetadataDataDefinition()).setToscaResourceName(toscaResourceName);
			return Either.left(true);
		} else {
			// As agreed with Renana - cannot be fixed automatically
			log.warn("toscaResourceName {} is not unique! Cannot set it. Continuing...");
			return Either.left(false);
		}
	}

	public boolean testRemoveHeatPlaceHolders(String appConfigDir) {

		if (!AllowMultipleHeats.removeAndUpdateHeatPlaceHolders(titanGenericDao, log, false)) {
			log.error("Failed to update heat place holders");
			return false;
		}
		return true;
	}

	private Either<Boolean, StorageOperationStatus> setVfToscaResourceName(ResourceMetadataData resource) {
		String resourceName = resource.getMetadataDataDefinition().getName();
		String resourceType = ((ResourceMetadataDataDefinition) resource.getMetadataDataDefinition()).getResourceType().name();
		String toscaResourceName = CommonBeUtils.generateToscaResourceName(resourceType, resource.getMetadataDataDefinition().getSystemName());
		log.debug("Setting tosca resource name {} to VF {}", toscaResourceName, resourceName);
		((ResourceMetadataDataDefinition) resource.getMetadataDataDefinition()).setToscaResourceName(toscaResourceName);

		Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(resource, ResourceMetadataData.class);
		if (updateNode.isRight()) {
			log.error("DB error during while updating VF tosca resource name {}, error: {}", resource.getMetadataDataDefinition().getName(), updateNode.right().value());
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}
		log.debug("Tosca resource name of VF {} was successfully updated", resource.getMetadataDataDefinition().getName());
		return Either.left(true);
	}

	public boolean testAddGroupUuids(String appConfigDir) {

		if (!AddGroupUuid.addGroupUuids(titanGenericDao, log, false)) {
			log.error("Failed to update group UUIDs");
			return false;
		}
		return true;
	}
}
