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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import com.thinkaurelius.titan.core.TitanTransaction;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.category.CategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.category.GroupingDataDefinition;
import org.openecomp.sdc.be.datatypes.category.SubCategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ProductMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IProductOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ProductMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.GroupingData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.TitanGraph;

import fj.data.Either;

@org.springframework.stereotype.Component("product-operation")
public class ProductOperation extends ComponentOperation implements IProductOperation {

	private static Logger log = LoggerFactory.getLogger(ProductOperation.class.getName());

	public ProductOperation() {
		log.debug("ProductOperation created");
	}

	@Override
	protected ComponentMetadataData getMetaDataFromComponent(Component component) {
		return getProductMetadataDataFromProduct((Product) component);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> getComponent(String id, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) getProduct(id, inTransaction);
	}

	// public <T> Either<T, StorageOperationStatus> getComponent_tx(String id,
	// boolean inTransaction) {
	// return (Either<T, StorageOperationStatus>) getProduct_tx(id,
	// inTransaction);
	// }

	@SuppressWarnings("unchecked")
	@Override
	protected <T> Either<T, StorageOperationStatus> getComponentByNameAndVersion(String name, String version, Map<String, Object> additionalParams, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) getByNamesAndVersion(GraphPropertiesDictionary.NAME.getProperty(), name, version, additionalParams, inTransaction);
	}

	@Override
	public <T> Either<T, StorageOperationStatus> getLightComponent(String id, boolean inTransaction) {
		return getLightComponent(id, NodeTypeEnum.Product, inTransaction);
	}

	@Override
	public <T> Either<List<T>, StorageOperationStatus> getFilteredComponents(Map<FilterKeyEnum, String> filters, boolean inTransaction) {
		return getFilteredComponents(filters, inTransaction, NodeTypeEnum.Product);
	}

	private Product convertProductDataToProduct(ProductMetadataData productData) {
		ProductMetadataDefinition productMetadataDefinition = new ProductMetadataDefinition((ProductMetadataDataDefinition) productData.getMetadataDataDefinition());

		Product product = new Product(productMetadataDefinition);

		return product;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> updateComponent(T component, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) updateComponent((Component) component, inTransaction, titanGenericDao, Product.class, NodeTypeEnum.Product);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Either<Component, StorageOperationStatus> deleteComponent(String id, boolean inTransaction) {
		return (Either<Component, StorageOperationStatus>) (Either<?, StorageOperationStatus>) deleteProduct(id, inTransaction);
	}

	@Override
	public Either<List<ArtifactDefinition>, StorageOperationStatus> getAdditionalArtifacts(String resourceId, boolean recursively, boolean inTransaction) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends org.openecomp.sdc.be.model.Component> Either<T, StorageOperationStatus> getComponent(String id, Class<T> clazz) {
		return (Either<T, StorageOperationStatus>) getProduct(id, false);
	}

	@Override
	/**
	 * Deletes the product node
	 */
	public Either<Product, StorageOperationStatus> deleteProduct(String productId, boolean inTransaction) {

		Either<Product, StorageOperationStatus> result = Either.right(StorageOperationStatus.GENERAL_ERROR);

		try {

			Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
			if (graphResult.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
				return result;
			}

			Either<ProductMetadataData, TitanOperationStatus> productNode = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Product), productId, ProductMetadataData.class);
			if (productNode.isRight()) {
				TitanOperationStatus status = productNode.right().value();
				log.error("Failed to find product {}. status is {}", productId, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			Either<Product, StorageOperationStatus> productRes = getProduct(productId, true);
			if (productRes.isRight()) {
				StorageOperationStatus status = productRes.right().value();
				log.error("Failed to find product {}.status is {}", productId, status);
				result = Either.right(status);
				return result;
			}
			Product product = productRes.left().value();

			Either<List<ComponentInstance>, StorageOperationStatus> deleteAllInstancesRes = componentInstanceOperation.deleteAllComponentInstances(productId, NodeTypeEnum.Product, true);
			log.debug("After deleting instances under product {}.Result is {}", productId, deleteAllInstancesRes);
			if (deleteAllInstancesRes.isRight()) {
				StorageOperationStatus status = deleteAllInstancesRes.right().value();
				if (status != StorageOperationStatus.NOT_FOUND) {
					log.error("Failed to delete instances under product {}.status is {}", productId, status);
					result = Either.right(status);
					return result;
				}
			}

			Either<ProductMetadataData, TitanOperationStatus> deleteProductNodeRes = titanGenericDao.deleteNode(productNode.left().value(), ProductMetadataData.class);
			if (deleteProductNodeRes.isRight()) {
				TitanOperationStatus status = deleteProductNodeRes.right().value();
				log.error("Failed to delete product node {}. status is {}", productId, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			result = Either.left(product);

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("deleteProduct operation : Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("deleteProduct operation : Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	@Override
	public Either<List<Product>, StorageOperationStatus> getProductCatalogData(boolean inTransaction) {

		long start = System.currentTimeMillis();
		try {
			/*
			 * Map<String, Object> propertiesToMatch = new HashMap<>();
			 * 
			 * propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty (), LifecycleStateEnum.CERTIFIED.name()); Either<List<ProductMetadataData>, TitanOperationStatus> lastVersionNodes = getLastVersion(NodeTypeEnum.Product,
			 * propertiesToMatch, ProductMetadataData.class); if (lastVersionNodes.isRight() && lastVersionNodes.right().value() != TitanOperationStatus.NOT_FOUND) { return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus
			 * (lastVersionNodes.right().value())); } List<ProductMetadataData> notCertifiedHighest = (lastVersionNodes.isLeft() ? lastVersionNodes.left().value() : new ArrayList<ProductMetadataData>());
			 * 
			 * propertiesToMatch.put(GraphPropertiesDictionary. IS_HIGHEST_VERSION.getProperty(), true); Either<List<ProductMetadataData>, TitanOperationStatus> componentsNodes = titanGenericDao.getByCriteria(NodeTypeEnum.Product, propertiesToMatch,
			 * ProductMetadataData.class); if (componentsNodes.isRight() && componentsNodes.right().value() != TitanOperationStatus.NOT_FOUND) { return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus
			 * (componentsNodes.right().value())); } List<ProductMetadataData> certifiedHighest = (componentsNodes.isLeft() ? componentsNodes.left().value() : new ArrayList<ProductMetadataData>()); Set<String> names = new HashSet<String>(); for
			 * (ProductMetadataData data : notCertifiedHighest) { String name = data.getMetadataDataDefinition().getName(); names.add(name); }
			 * 
			 * for (ProductMetadataData data : certifiedHighest) { String productName = data.getMetadataDataDefinition().getName(); if (!names.contains(productName)) { notCertifiedHighest.add(data); } }
			 */
			Either<List<ProductMetadataData>, TitanOperationStatus> listOfHighestComponents = this.getListOfHighestComponents(NodeTypeEnum.Product, ProductMetadataData.class);
			if (listOfHighestComponents.isRight() && listOfHighestComponents.right().value() != TitanOperationStatus.NOT_FOUND) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(listOfHighestComponents.right().value()));
			}

			List<ProductMetadataData> notCertifiedHighest = listOfHighestComponents.left().value();

			List<Product> result = new ArrayList<>();

			if (notCertifiedHighest != null && false == notCertifiedHighest.isEmpty()) {

				// fetch from cache
				long startFetchAllFromCache = System.currentTimeMillis();

				Map<String, Long> components = notCertifiedHighest.stream().collect(Collectors.toMap(p -> p.getMetadataDataDefinition().getUniqueId(), p -> p.getMetadataDataDefinition().getLastUpdateDate()));

				Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> componentsFromCacheForCatalog = this.getComponentsFromCacheForCatalog(components, ComponentTypeEnum.PRODUCT);
				if (componentsFromCacheForCatalog.isLeft()) {
					ImmutablePair<List<Component>, Set<String>> immutablePair = componentsFromCacheForCatalog.left().value();
					List<Component> list = immutablePair.getLeft();
					if (list != null) {
						for (Component component : list) {
							result.add((Product) component);
						}
						List<String> addedUids = list.stream().map(p -> p.getComponentMetadataDefinition().getMetadataDataDefinition().getUniqueId()).collect(Collectors.toList());
						notCertifiedHighest = notCertifiedHighest.stream().filter(p -> false == addedUids.contains(p.getMetadataDataDefinition().getUniqueId())).collect(Collectors.toList());
					}
				}
				long endFetchAllFromCache = System.currentTimeMillis();
				log.debug("Fetch all catalog products metadata from cache took {} ms", (endFetchAllFromCache - startFetchAllFromCache));
				log.debug("The number of products added to catalog from cache is {}", result.size());

				log.debug("The number of products needed to be fetch as light component is {}", notCertifiedHighest.size());

				for (ProductMetadataData data : notCertifiedHighest) {
					Either<Product, StorageOperationStatus> component = getLightComponent(data.getMetadataDataDefinition().getUniqueId(), inTransaction);
					if (component.isRight()) {
						log.debug("Failed to get product for id = {}, error : {}. skip product", data.getUniqueId(), component.right().value());
					} else {
						// get all versions
						Product product = component.left().value();
						// setAllVersions(product);

						result.add(product);
					}
				}
			}
			return Either.left(result);
		} finally {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
			log.debug("Fetch all catalog products took {} ms", System.currentTimeMillis() - start);
		}
	}

	@Override
	public Either<Product, StorageOperationStatus> createProduct(Product product) {
		return createProduct(product, false);
	}

	@Override
	public Either<Product, StorageOperationStatus> createProduct(Product product, boolean inTransaction) {
		Either<Product, StorageOperationStatus> result = null;

		try {

			ProductMetadataData productData = getProductMetadataDataFromProduct(product);
			addComponentInternalFields(productData);
			String uniqueId = (String) productData.getUniqueId();
			generateUUID(product);

			String userId = product.getCreatorUserId();

			Either<UserData, TitanOperationStatus> findUser = findUser(userId);

			if (findUser.isRight()) {
				TitanOperationStatus status = findUser.right().value();
				log.error("Cannot find user {} in the graph. status is {}", userId, status);
				return sendError(status, StorageOperationStatus.USER_NOT_FOUND);
			}

			UserData creatorUserData = findUser.left().value();
			UserData updaterUserData = creatorUserData;
			String updaterUserId = product.getLastUpdaterUserId();
			if (updaterUserId != null && !updaterUserId.equals(userId)) {
				findUser = findUser(updaterUserId);
				if (findUser.isRight()) {
					TitanOperationStatus status = findUser.right().value();
					log.error("Cannot find user {} in the graph. status is {}", userId, status);
					return sendError(status, StorageOperationStatus.USER_NOT_FOUND);
				} else {
					updaterUserData = findUser.left().value();
				}
			}

			log.trace("Creating tags for product {}", uniqueId);
			StorageOperationStatus storageOperationStatus = createTagsForComponent(product);
			if (storageOperationStatus != StorageOperationStatus.OK) {
				return Either.right(storageOperationStatus);
			}

			log.trace("Finding groupings for product {}", uniqueId);
			Either<List<GroupingData>, StorageOperationStatus> findGroupingsForComponent = findGroupingsForComponent(NodeTypeEnum.ProductGrouping, product);
			if (findGroupingsForComponent.isRight()) {
				return Either.right(findGroupingsForComponent.right().value());
			}
			List<GroupingData> groupingDataToAssociate = findGroupingsForComponent.left().value();

			log.debug("try to create product node on graph for id {}", uniqueId);
			Either<ProductMetadataData, TitanOperationStatus> createNode = titanGenericDao.createNode(productData, ProductMetadataData.class);
			if (createNode.isRight()) {
				TitanOperationStatus status = createNode.right().value();
				log.error("Error returned after creating product data node {}. Status returned is {}", productData, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}
			log.debug("product node created on graph for id {}", productData.getUniqueId());

			TitanOperationStatus associateMetadata = associateMetadataToComponent(productData, creatorUserData, updaterUserData, null, null);
			if (associateMetadata != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateMetadata));
				return result;
			}

			TitanOperationStatus associateCategories = associateCategoriesToProduct(productData, groupingDataToAssociate);
			if (associateCategories != TitanOperationStatus.OK) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateCategories));
				return result;
			}

			result = getProduct(uniqueId, true);
			if (result.isRight()) {
				log.error("Cannot get full product from the graph. status is {}", result.right().value());
				return Either.right(result.right().value());
			}

			if (log.isDebugEnabled()) {
				String json = prettyJson.toJson(result.left().value());
				log.debug("Product retrieved is {}", json);
			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	private TitanOperationStatus associateCategoriesToProduct(ProductMetadataData productData, List<GroupingData> groupingDataToAssociate) {
		for (GroupingData groupingData : groupingDataToAssociate) {
			GraphEdgeLabels groupingLabel = GraphEdgeLabels.CATEGORIZED_TO;
			Either<GraphRelation, TitanOperationStatus> result = titanGenericDao.createRelation(productData, groupingData, groupingLabel, null);
			log.debug("After associating grouping {} to product {}. Edge type is {}", groupingData, productData, groupingLabel);
			if (result.isRight()) {
				return result.right().value();
			}
		}
		log.trace("All groupings associated succesfully to product {}", productData);
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus dissociateCategoriesFromProduct(ProductMetadataData productData, List<GroupingData> groupingDataToDissociate) {
		for (GroupingData groupingData : groupingDataToDissociate) {
			GraphEdgeLabels groupingLabel = GraphEdgeLabels.CATEGORIZED_TO;
			Either<GraphRelation, TitanOperationStatus> result = titanGenericDao.deleteRelation(productData, groupingData, groupingLabel);
			log.debug("After dissociating grouping {} from product {}. Edge type is {}", groupingData, productData, groupingLabel);
			if (result.isRight()) {
				return result.right().value();
			}
		}
		log.trace("All groupings dissociated succesfully from product {}", productData);
		return TitanOperationStatus.OK;
	}

	private Either<Product, StorageOperationStatus> getProduct(String uniqueId, boolean inTransaction) {
		ComponentParametersView componentParametersView = new ComponentParametersView();
		return getProduct(uniqueId, componentParametersView, inTransaction);
	}

	private Either<Product, StorageOperationStatus> getProduct(String uniqueId, ComponentParametersView componentParametersView, boolean inTransaction) {
		Product product = null;
		Either<Product, StorageOperationStatus> result = null;
		try {

			NodeTypeEnum productNodeType = NodeTypeEnum.Product;
			NodeTypeEnum compInstNodeType = NodeTypeEnum.Service;

			Either<ProductMetadataData, StorageOperationStatus> getComponentByLabel = getComponentByLabelAndId(uniqueId, productNodeType, ProductMetadataData.class);
			if (getComponentByLabel.isRight()) {
				result = Either.right(getComponentByLabel.right().value());
				return result;
			}
			ProductMetadataData productData = getComponentByLabel.left().value();

			// Try to fetch resource from the cache. The resource will be
			// fetched only if the time on the cache equals to
			// the time on the graph.
			Either<Product, ActionStatus> componentFromCacheIfUpToDate = this.getComponentFromCacheIfUpToDate(uniqueId, productData, componentParametersView, Product.class, ComponentTypeEnum.PRODUCT);
			if (componentFromCacheIfUpToDate.isLeft()) {
				Product cachedProduct = componentFromCacheIfUpToDate.left().value();
				log.debug("Product {} with uid {} was fetched from cache.", cachedProduct.getName(), cachedProduct.getUniqueId());
				return Either.left(cachedProduct);
			}

			product = convertProductDataToProduct(productData);

			TitanOperationStatus status = null;
			if (false == componentParametersView.isIgnoreUsers()) {
				status = setComponentCreatorFromGraph(product, uniqueId, productNodeType);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}

				status = setComponentLastModifierFromGraph(product, uniqueId, productNodeType);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;

				}
			}
			if (false == componentParametersView.isIgnoreCategories()) {
				status = setComponentCategoriesFromGraph(product);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			}

			if (false == componentParametersView.isIgnoreComponentInstances() || false == componentParametersView.isIgnoreComponentInstancesProperties() || false == componentParametersView.isIgnoreCapabilities()
					|| false == componentParametersView.isIgnoreRequirements()) {
				status = setComponentInstancesFromGraph(uniqueId, product, productNodeType, compInstNodeType);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;

				}
			}
			if (false == componentParametersView.isIgnoreComponentInstancesProperties()) {
				status = setComponentInstancesPropertiesFromGraph(product);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			}
			if (false == componentParametersView.isIgnoreCapabilities()) {
				status = setCapabilitiesFromGraph(uniqueId, product, NodeTypeEnum.Product);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			}
			if (false == componentParametersView.isIgnoreRequirements()) {
				status = setRequirementsFromGraph(uniqueId, product, NodeTypeEnum.Product);
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			}

			if (false == componentParametersView.isIgnoreAllVersions()) {
				status = setAllVersions(product);
				if (status != TitanOperationStatus.OK) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
			}

			result = Either.left(product);
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					titanGenericDao.rollback();
				} else {
					titanGenericDao.commit();
				}
			}
		}
	}

	// private Either<Product, StorageOperationStatus> getProduct_tx(String
	// uniqueId, boolean inTransaction) {
	// Product product = null;
	// Either<Product, StorageOperationStatus> result = null;
	// try {
	//
	// NodeTypeEnum productNodeType = NodeTypeEnum.Product;
	// NodeTypeEnum compInstNodeType = NodeTypeEnum.Service;
	//
	// Either<ProductMetadataData, StorageOperationStatus> getComponentByLabel =
	// getComponentByLabelAndId_tx(uniqueId, productNodeType,
	// ProductMetadataData.class);
	// if (getComponentByLabel.isRight()) {
	// result = Either.right(getComponentByLabel.right().value());
	// return result;
	// }
	// ProductMetadataData productData = getComponentByLabel.left().value();
	// product = convertProductDataToProduct(productData);
	//
	// TitanOperationStatus status = setComponentCreatorFromGraph(product,
	// uniqueId, productNodeType);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	// }
	//
	// status = setComponentLastModifierFromGraph(product, uniqueId,
	// productNodeType);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	//
	// }
	// status = setComponentCategoriesFromGraph(product);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	// }
	//
	// status = setComponentInstancesFromGraph(uniqueId, product,
	// productNodeType, compInstNodeType);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	//
	// }
	//
	// status = setComponentInstancesPropertiesFromGraph(uniqueId, product);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	// }
	//
	// status = setCapabilitiesFromGraph(uniqueId, product,
	// NodeTypeEnum.Product);
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	// }
	//
	// status = setRequirementsFromGraph( uniqueId, product,
	// NodeTypeEnum.Product);;
	// if (status != TitanOperationStatus.OK) {
	// result =
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// return result;
	// }
	//
	//
	// status = setAllVersions(product);
	// if (status != TitanOperationStatus.OK) {
	// return
	// Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
	// }
	//
	// result = Either.left(product);
	// return result;
	//
	// } finally {
	// if (false == inTransaction) {
	// if (result == null || result.isRight()) {
	// titanGenericDao.rollback();
	// } else {
	// titanGenericDao.commit();
	// }
	// }
	// }
	// }

	private TitanOperationStatus setAllVersions(Product product) {
		Either<Map<String, String>, TitanOperationStatus> res = getVersionList(NodeTypeEnum.Product, product.getVersion(), product, ProductMetadataData.class);
		if (res.isRight()) {
			return res.right().value();
		}
		product.setAllVersions(res.left().value());
		return TitanOperationStatus.OK;
	}

	private Either<Product, StorageOperationStatus> sendError(TitanOperationStatus status, StorageOperationStatus statusIfNotFound) {
		Either<Product, StorageOperationStatus> result;
		if (status == TitanOperationStatus.NOT_FOUND) {
			result = Either.right(statusIfNotFound);
			return result;
		} else {
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			return result;
		}
	}

	@Override
	TitanOperationStatus setComponentCategoriesFromGraph(Component component) {
		Product product = (Product) component;
		// Building the cat->subcat->grouping triples
		Either<List<ImmutablePair<GroupingData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Product), product.getUniqueId(), GraphEdgeLabels.CATEGORIZED_TO,
				NodeTypeEnum.ProductGrouping, GroupingData.class);
		if (childrenNodes.isRight()) {
			if (childrenNodes.right().value() != TitanOperationStatus.NOT_FOUND) {
				log.debug("Error when finding groupings for this product, error {}", childrenNodes.right().value());
				return childrenNodes.right().value();
			} else {
				log.debug("No groupings found for this product - this might be normal");
				return TitanOperationStatus.OK;
			}
		}
		Map<CategoryDefinition, Map<SubCategoryDefinition, List<GroupingDefinition>>> categoriesDataStructure = new HashMap<>();

		List<ImmutablePair<GroupingData, GraphEdge>> valueList = childrenNodes.left().value();
		for (ImmutablePair<GroupingData, GraphEdge> groupPair : valueList) {
			GroupingData groupingData = groupPair.getLeft();
			Either<ImmutablePair<SubCategoryData, GraphEdge>, TitanOperationStatus> parentSubCat = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ProductGrouping), (String) groupingData.getUniqueId(),
					GraphEdgeLabels.GROUPING, NodeTypeEnum.ProductSubcategory, SubCategoryData.class);
			if (parentSubCat.isRight()) {
				log.debug("Cannot find subcategory for grouping {}", groupingData.getUniqueId());
				return parentSubCat.right().value();
			}
			SubCategoryData subCatData = parentSubCat.left().value().getLeft();
			Either<ImmutablePair<CategoryData, GraphEdge>, TitanOperationStatus> parentCat = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ProductSubcategory), (String) subCatData.getUniqueId(),
					GraphEdgeLabels.SUB_CATEGORY, NodeTypeEnum.ProductCategory, CategoryData.class);
			if (parentCat.isRight()) {
				log.debug("Cannot find category for subcategory {}", subCatData.getUniqueId());
				return parentCat.right().value();
			}

			// Building data structure of categories hierarchy
			CategoryDataDefinition categoryDefinition = parentCat.left().value().getLeft().getCategoryDataDefinition();
			SubCategoryDataDefinition subDefinition = subCatData.getSubCategoryDataDefinition();
			GroupingDataDefinition groupingDefinition = groupingData.getGroupingDataDefinition();

			CategoryDefinition categoryDef = new CategoryDefinition(categoryDefinition);
			SubCategoryDefinition subDef = new SubCategoryDefinition(subDefinition);
			GroupingDefinition groupingDef = new GroupingDefinition(groupingDefinition);

			log.debug("Found category {} -> subcategory {} -> grouping {} for product {}", categoryDefinition.getUniqueId(), subCatData.getUniqueId(), groupingData.getUniqueId(), product.getUniqueId());
			Map<SubCategoryDefinition, List<GroupingDefinition>> subMap = categoriesDataStructure.get(categoryDef);
			if (subMap == null) {
				subMap = new HashMap<>();
				categoriesDataStructure.put(categoryDef, subMap);
			}
			List<GroupingDefinition> groupList = subMap.get(subDef);
			if (groupList == null) {
				groupList = new ArrayList<>();
				subMap.put(subDef, groupList);
			}
			groupList.add(groupingDef);
		}
		convertToCategoriesList(product, categoriesDataStructure);
		return TitanOperationStatus.OK;
	}

	private void convertToCategoriesList(Product product, Map<CategoryDefinition, Map<SubCategoryDefinition, List<GroupingDefinition>>> categoriesDataStructure) {
		List<CategoryDefinition> categoryDataList = product.getCategories();
		if (categoryDataList == null) {
			categoryDataList = new ArrayList<CategoryDefinition>();
		}
		for (Entry<CategoryDefinition, Map<SubCategoryDefinition, List<GroupingDefinition>>> triple : categoriesDataStructure.entrySet()) {
			CategoryDefinition categoryDefinition = triple.getKey();
			List<SubCategoryDefinition> subList = new ArrayList<>();
			categoryDefinition.setSubcategories(subList);
			Map<SubCategoryDefinition, List<GroupingDefinition>> value = triple.getValue();

			for (Entry<SubCategoryDefinition, List<GroupingDefinition>> pair : value.entrySet()) {
				SubCategoryDefinition subCategoryDefinition = pair.getKey();
				List<GroupingDefinition> list = pair.getValue();
				subList.add(subCategoryDefinition);
				subCategoryDefinition.setGroupings(list);
			}
			categoryDataList.add(categoryDefinition);
		}
		product.setCategories(categoryDataList);
		log.debug("Fetched categories for product {}, categories: {}", product.getUniqueId(), Arrays.toString(categoryDataList.toArray()));
	}

	private ProductMetadataData getProductMetadataDataFromProduct(Product product) {
		ProductMetadataData productMetadata = new ProductMetadataData((ProductMetadataDataDefinition) product.getComponentMetadataDefinition().getMetadataDataDefinition());
		return productMetadata;
	}

	@Override
	public boolean isComponentExist(String id) {
		return isComponentExist(id, NodeTypeEnum.Product);
	}

	// @SuppressWarnings("unchecked")
	// @Override
	// public <T> Either<T, StorageOperationStatus> cloneComponent(T other,
	// String version, boolean inTransaction) {
	// return (Either<T, StorageOperationStatus>) cloneProduct((Product)other,
	// version, inTransaction);
	// }

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> cloneComponent(T other, String version, LifecycleStateEnum targetLifecycle, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) cloneProduct((Product) other, version, targetLifecycle, inTransaction);
	}

	private Either<Product, StorageOperationStatus> cloneProduct(Product other, String version, LifecycleStateEnum targetLifecycle, boolean inTransaction) {
		Either<Product, StorageOperationStatus> result = null;

		try {
			String origProductId = other.getUniqueId();
			other.setVersion(version);
			other.setUniqueId(null);

			Either<Integer, StorageOperationStatus> counterStatus = getComponentInstanceCoutner(origProductId, NodeTypeEnum.Product);
			if (counterStatus.isRight()) {
				StorageOperationStatus status = counterStatus.right().value();
				log.error("failed to get resource instance counter on product {}. status={}", origProductId, counterStatus);
				result = Either.right(status);
				return result;
			}

			Either<Product, StorageOperationStatus> createProductMD = createProduct(other, inTransaction);
			if (createProductMD.isRight()) {
				StorageOperationStatus status = createProductMD.right().value();
				log.debug("Failed to clone product. status= {}", status);
				result = Either.right(status);
				return result;
			}
			Product product = createProductMD.left().value();

			Either<ImmutablePair<List<ComponentInstance>, Map<String, String>>, StorageOperationStatus> cloneInstances = componentInstanceOperation.cloneAllComponentInstancesFromContainerComponent(origProductId, product,
					NodeTypeEnum.Product, NodeTypeEnum.Service, targetLifecycle, null);
			if (cloneInstances.isRight()) {
				result = Either.right(cloneInstances.right().value());
				return result;
			}

			Either<Integer, StorageOperationStatus> setResourceInstanceCounter = setComponentInstanceCounter(product.getUniqueId(), NodeTypeEnum.Product, counterStatus.left().value(), inTransaction);
			if (setResourceInstanceCounter.isRight()) {
				StorageOperationStatus status = setResourceInstanceCounter.right().value();
				log.error("failed to set resource instance counter on product {}. status={}", product.getUniqueId(), setResourceInstanceCounter);
				result = Either.right(status);
				return result;
			}

			result = this.getProduct(product.getUniqueId(), inTransaction);
			if (result.isRight()) {
				log.error("Cannot get full product from the graph. status is {}", result.right().value());
				return Either.right(result.right().value());
			}

			if (log.isDebugEnabled()) {
				String json = prettyJson.toJson(result.left().value());
				log.debug("Product retrieved is {}", json);
			}

			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	private Either<Product, StorageOperationStatus> getByNamesAndVersion(String nameKey, String nameValue, String version, Map<String, Object> additionalParams, boolean inTransaction) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(nameKey, nameValue);
		props.put(GraphPropertiesDictionary.VERSION.getProperty(), version);
		props.put(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.Product.getName());
		if (additionalParams != null && !additionalParams.isEmpty()) {
			props.putAll(additionalParams);
		}

		Either<List<ProductMetadataData>, TitanOperationStatus> byCriteria = titanGenericDao.getByCriteria(NodeTypeEnum.Product, props, ProductMetadataData.class);

		if (byCriteria.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(byCriteria.right().value()));
		}
		List<ProductMetadataData> dataList = byCriteria.left().value();
		if (dataList != null && !dataList.isEmpty()) {
			if (dataList.size() > 1) {
				log.debug("More that one instance of product for name {} and version {}", nameValue, version);
				return Either.right(StorageOperationStatus.GENERAL_ERROR);
			}
			ProductMetadataData productData = dataList.get(0);
			Either<Product, StorageOperationStatus> product = getProduct(productData.getMetadataDataDefinition().getUniqueId(), inTransaction);
			if (product.isRight()) {
				log.debug("Failed to fetch product, name {} id {}", productData.getMetadataDataDefinition().getName(), productData.getMetadataDataDefinition().getUniqueId());
			}
			return product;
		}
		return Either.right(StorageOperationStatus.NOT_FOUND);
	}

	@Override
	public Product getDefaultComponent() {
		return new Product();
	}

	@Override
	protected <T extends org.openecomp.sdc.be.model.Component> StorageOperationStatus updateDerived(org.openecomp.sdc.be.model.Component component, org.openecomp.sdc.be.model.Component currentComponent, ComponentMetadataData componentData,
			Class<T> clazz) {
		log.debug("Derived class isn't supported for product");
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<Integer, StorageOperationStatus> increaseAndGetComponentInstanceCounter(String componentId, boolean inTransaction) {
		return increaseAndGetComponentInstanceCounter(componentId, NodeTypeEnum.Product, inTransaction);
	}

	@Override
	protected StorageOperationStatus validateCategories(Component currentComponent, Component component, ComponentMetadataData componentData, NodeTypeEnum type) {
		// As agreed with Ella, update categories - delete old and create new
		StorageOperationStatus status = StorageOperationStatus.OK;
		List<CategoryDefinition> newcategories = component.getCategories();
		List<CategoryDefinition> currentcategories = currentComponent.getCategories();
		if (newcategories != null) {
			if (currentcategories != null && !currentcategories.isEmpty()) {
				Either<List<GroupingData>, StorageOperationStatus> findGroupingsForComponent = findGroupingsForComponent(NodeTypeEnum.ProductGrouping, currentComponent);
				if (findGroupingsForComponent.isRight()) {
					status = findGroupingsForComponent.right().value();
				}
				List<GroupingData> groupingDataToDissociate = findGroupingsForComponent.left().value();
				TitanOperationStatus titanStatus = dissociateCategoriesFromProduct((ProductMetadataData) componentData, groupingDataToDissociate);
				if (titanStatus != TitanOperationStatus.OK) {
					status = DaoStatusConverter.convertTitanStatusToStorageStatus(titanStatus);
				}
			}
			if (!newcategories.isEmpty()) {
				Either<List<GroupingData>, StorageOperationStatus> findGroupingsForComponent = findGroupingsForComponent(NodeTypeEnum.ProductGrouping, component);
				if (findGroupingsForComponent.isRight()) {
					status = findGroupingsForComponent.right().value();
				}
				List<GroupingData> groupingDataToAssociate = findGroupingsForComponent.left().value();
				TitanOperationStatus titanStatus = associateCategoriesToProduct((ProductMetadataData) componentData, groupingDataToAssociate);
				if (titanStatus != TitanOperationStatus.OK) {
					status = DaoStatusConverter.convertTitanStatusToStorageStatus(titanStatus);
				}
			}
		}
		return status;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Either<List<Product>, StorageOperationStatus> getFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates, boolean inTransaction) {
		return (Either<List<Product>, StorageOperationStatus>) (Either<?, StorageOperationStatus>) getFollowedComponent(userId, lifecycleStates, lastStateStates, inTransaction, titanGenericDao, NodeTypeEnum.Product);
	}

	@Override
	public Either<Component, StorageOperationStatus> getMetadataComponent(String id, boolean inTransaction) {
		return getMetadataComponent(id, NodeTypeEnum.Product, inTransaction);
	}

	@Override
	Component convertComponentMetadataDataToComponent(ComponentMetadataData componentMetadataData) {
		return convertProductDataToProduct((ProductMetadataData) componentMetadataData);
	}

	@Override
	public Either<Boolean, StorageOperationStatus> validateComponentNameExists(String productName) {
		return validateComponentNameUniqueness(productName, titanGenericDao, NodeTypeEnum.Product);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Either<Component, StorageOperationStatus> markComponentToDelete(Component componentToDelete, boolean inTransaction) {
		// markComponentToDelete is not defined yet for products
		return (Either<Component, StorageOperationStatus>) (Either<?, StorageOperationStatus>) deleteProduct(componentToDelete.getUniqueId(), inTransaction);
	}

	@Override
	public void rollback() {
		titanGenericDao.rollback();

	}

	@Override
	public void commit() {
		titanGenericDao.commit();
	}

	@Override
	public Either<Boolean, StorageOperationStatus> isComponentInUse(String componentId) {
		return isComponentInUse(componentId, NodeTypeEnum.Product);
	}

	@Override
	public Either<List<String>, StorageOperationStatus> getAllComponentsMarkedForDeletion() {
		// markForDeletion for products is not implemented yet
		return Either.left(new ArrayList<>());
	}

	public Either<Product, StorageOperationStatus> getProductByNameAndVersion(String productName, String productVersion, boolean inTransaction) {
		return getByNamesAndVersion(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), ValidationUtils.normaliseComponentName(productName), productVersion, null, inTransaction);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Either<T, StorageOperationStatus> getComponent(String id, ComponentParametersView componentParametersView, boolean inTransaction) {
		return (Either<T, StorageOperationStatus>) getProduct(id, false);
	}

	public Either<Product, StorageOperationStatus> updateProduct(Product product, boolean inTransaction, ComponentParametersView filterResultView) {
		return (Either<Product, StorageOperationStatus>) updateComponentFilterResult(product, inTransaction, titanGenericDao, product.getClass(), NodeTypeEnum.Service, filterResultView);
	}

	@Override
	protected <T> Either<T, StorageOperationStatus> updateComponentFilterResult(T component, boolean inTransaction, ComponentParametersView filterResultView) {
		return (Either<T, StorageOperationStatus>) updateProduct((Product) component, inTransaction, filterResultView);
	}
}
