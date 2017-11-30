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

package org.openecomp.sdc.be.model.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.Configuration.ApplicationL1CacheCatalogInfo;
import org.openecomp.sdc.be.config.Configuration.ApplicationL2CacheConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.ComponentCassandraDao;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;
import org.openecomp.sdc.common.util.SerializationUtils;
import org.openecomp.sdc.common.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

@org.springframework.stereotype.Component("component-cache")
public class ComponentCache {

	private static Logger logger = LoggerFactory.getLogger(ComponentCache.class.getName());

	@javax.annotation.Resource
	ComponentCassandraDao componentCassandraDao;
	
	@Autowired
	ToscaOperationFacade toscaOperationFacade;

	private Map<ComponentTypeEnum, Map<String, Component>> catalogInMemoryCache = new HashMap<>();
	private final ReentrantReadWriteLock rwCatalogLock = new ReentrantReadWriteLock();
	private final Lock rCatalogLock = rwCatalogLock.readLock();
	private final Lock wCatalogLock = rwCatalogLock.writeLock();

	boolean enabled = true;
	int catalogInMemorySizePerResource = 300;
	int catalogInMemorySizePerService = 200;
	int catalogInMemorySizePerProduct = 100;
	boolean catalogInMemoryEnabled = true;
	Map<ComponentTypeEnum, Integer> limitMemoryCatalogSizePerType = new HashMap<>();

	@PostConstruct
	public void init() {

		Configuration configuration = ConfigurationManager.getConfigurationManager().getConfiguration();
		if (configuration != null) {
			ApplicationL2CacheConfig applicationL2Cache = configuration.getApplicationL2Cache();
			if (applicationL2Cache != null) {
				boolean isEnabled = applicationL2Cache.isEnabled();
				this.enabled = isEnabled;

				ApplicationL1CacheCatalogInfo catalog = applicationL2Cache.getCatalogL1Cache();
				if (catalog != null) {
					catalogInMemoryEnabled = catalog.getEnabled();
					catalogInMemorySizePerResource = catalog.getResourcesSizeInCache();
					catalogInMemorySizePerService = catalog.getServicesSizeInCache();
					catalogInMemorySizePerProduct = catalog.getProductsSizeInCache();
				}
			}
		}

		ComponentTypeEnum[] typesForCache = { ComponentTypeEnum.RESOURCE, ComponentTypeEnum.SERVICE,
				ComponentTypeEnum.PRODUCT };
		for (ComponentTypeEnum typeEnum : typesForCache) {
			Map<String, Component> map = new HashMap<>();
			catalogInMemoryCache.put(typeEnum, map);
		}

		limitMemoryCatalogSizePerType.put(ComponentTypeEnum.RESOURCE, catalogInMemorySizePerResource);
		limitMemoryCatalogSizePerType.put(ComponentTypeEnum.SERVICE, catalogInMemorySizePerService);
		limitMemoryCatalogSizePerType.put(ComponentTypeEnum.PRODUCT, catalogInMemorySizePerProduct);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Either<Component, ActionStatus> getComponent(String componentUid, Long lastModificationTime,
			Function<Component, Component> filterFieldsFunc) {

		Either<ImmutablePair<Component, ComponentCacheData>, ActionStatus> componentFromCache = getComponentFromCache(
				componentUid, lastModificationTime, filterFieldsFunc);

		if (componentFromCache.isRight()) {
			return Either.right(componentFromCache.right().value());
		}

		return Either.left(componentFromCache.left().value().left);

	}

	public Either<List<ComponentCacheData>, ActionStatus> getAllComponentIdTimeAndType() {
		if (false == isEnabled()) {
			return Either.right(ActionStatus.NOT_ALLOWED);
		}

		Either<List<ComponentCacheData>, ActionStatus> componentRes = componentCassandraDao
				.getAllComponentIdTimeAndType();

		return componentRes;

	}

	/**
	 * get components for catalog
	 *
	 * @param components
	 * @param componentTypeEnum
	 * @return
	 */
	@Deprecated
	public Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> getComponentsForCatalog(
			Set<String> components, ComponentTypeEnum componentTypeEnum) {

		if (false == isEnabled()) {
			logger.debug("In getComponentsForCatalog for type {}. Cache is disabled.",
					componentTypeEnum.name().toLowerCase());
			return Either.right(ActionStatus.NOT_ALLOWED);
		}
		logger.debug("In getComponentsForCatalog for type {}", componentTypeEnum.name().toLowerCase());

		Function<List<Component>, List<Component>> filterFieldsFunc = x -> filterForCatalog(x);

		Set<String> leftComponentsForSearch = new HashSet<>();
		leftComponentsForSearch.addAll(components);

		// get components from inmemory cache
		List<Component> componentsFromMemory = null;
		if (true == catalogInMemoryEnabled) {
			componentsFromMemory = getDataFromInMemoryCache(components, componentTypeEnum);
			logger.debug("The number of components of type {} fetched from memory is {}",
					componentTypeEnum.name().toLowerCase(),
					componentsFromMemory == null ? 0 : componentsFromMemory.size());
			if (componentsFromMemory != null) {
				componentsFromMemory.forEach(p -> leftComponentsForSearch.remove(p.getUniqueId()));
			}
		} else {
			logger.debug("Catalog InMemory cache is disabled");
		}

		logger.debug("Number of components from type {} needed to fetch is {}", componentTypeEnum.name().toLowerCase(),
				leftComponentsForSearch.size());

		// get components from cassandra cache and filter each component
		Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result = getComponents(
				leftComponentsForSearch, filterFieldsFunc);

		if (result.isLeft()) {
			// add inmemory components to the valid components(not dirty)
			List<Component> foundComponents = result.left().value().getLeft();
			if (componentsFromMemory != null) {
				foundComponents.addAll(componentsFromMemory);
			}
			if (true == catalogInMemoryEnabled) {
				updateCatalogInMemoryCacheWithCertified(foundComponents, componentTypeEnum);
			}
		}

		return result;
	}

	/**
	 * @param foundComponents
	 * @param componentTypeEnum
	 */
	private void updateCatalogInMemoryCacheWithCertified(List<Component> foundComponents,
			ComponentTypeEnum componentTypeEnum) {

		try {
			wCatalogLock.lock();

			long start = System.currentTimeMillis();
			Map<String, Component> map = catalogInMemoryCache.get(componentTypeEnum);
			int mapSizeBefore = map.size();
			map.clear();
			Map<String, Component> collect = foundComponents.stream()
					.filter(p -> p.getLifecycleState() == LifecycleStateEnum.CERTIFIED)
					.limit(limitMemoryCatalogSizePerType.get(componentTypeEnum))
					.collect(Collectors.toMap(p -> p.getUniqueId(), p -> p));
			map.putAll(collect);
			logger.debug(
					"Size of in memory cache for catalog {}(certified only): Before {}, After {}. Replacement Time is {} ms.",
					componentTypeEnum.name().toLowerCase(), mapSizeBefore, map.size(),
					System.currentTimeMillis() - start);
		} finally {
			wCatalogLock.unlock();
		}

	}

	private List<Component> getDataFromInMemoryCache(Set<String> components, ComponentTypeEnum componentTypeEnum) {
		List<Component> foundComponents = new ArrayList<>();

		try {

			rCatalogLock.lock();

			Map<String, Component> map = catalogInMemoryCache.get(componentTypeEnum);
			for (String compUid : components) {
				Component component = map.get(compUid);
				if (component != null) {
					foundComponents.add(component);
				}
			}

		} finally {
			rCatalogLock.unlock();
		}

		return foundComponents;
	}

	/**
	 *
	 * get full components from cassandra. On each component apply filter
	 * function in order to remove unused members
	 *
	 * @param components
	 * @param filterFieldsFunc
	 * @return <found components, found dirty components, not found components
	 *         list> or Error
	 */
	public Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> getComponents(
			Set<String> components, Function<List<Component>, List<Component>> filterFieldsFunc) {

		if (false == isEnabled()) {
			logger.debug("Component Cache is disabled");
			return Either.right(ActionStatus.NOT_ALLOWED);
		}

		Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> componentsFull = getComponentsFull(
				components);

		if (componentsFull.isRight()) {
			return Either.right(componentsFull.right().value());
		}

		ImmutableTriple<List<Component>, List<Component>, Set<String>> immutableTriple = componentsFull.left().value();
		List<Component> foundResources = immutableTriple.left;
		List<Component> foundDirtyResources = immutableTriple.middle;
		Set<String> notFoundResources = immutableTriple.right;

		List<Component> filterdFoundResources = filterFieldsFunc.apply(foundResources);
		List<Component> filterdFoundDirtyResources = filterFieldsFunc.apply(foundDirtyResources);

		ImmutableTriple<List<Component>, List<Component>, Set<String>> result = new ImmutableTriple<List<Component>, List<Component>, Set<String>>(
				filterdFoundResources, filterdFoundDirtyResources, notFoundResources);

		return Either.left(result);

	}

	public Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> getComponentsForLeftPanel(
			ComponentTypeEnum componentTypeEnum, String internalComponentType, Set<String> filteredResources) {

		logger.debug("In getComponentsForLeftPanel componentTypeEnum = {}, internalComponentType = {}",
				componentTypeEnum, internalComponentType);

		Function<List<Component>, List<Component>> filterFieldsFunc = x -> filterForLeftPanel(x);

		return getComponents(filteredResources, filterFieldsFunc);

	}

	private List<Component> filterForLeftPanel(List<Component> components) {

		List<Component> result = new ArrayList<>();
		if (components != null) {
			components.forEach(p -> result.add(filterFieldsForLeftPanel(p)));
		}

		return result;
	}

	private List<Component> filterForCatalog(List<Component> components) {

		List<Component> result = new ArrayList<>();
		if (components != null) {
			components.forEach(p -> result.add(filterFieldsForCatalog(p)));
		}

		return result;
	}

	private Component filterFieldsForLeftPanel(Component component) {

		Component result = null;
		ComponentTypeEnum componentTypeEnum = component.getComponentType();
		switch (componentTypeEnum) {
		case RESOURCE:
			result = new Resource();
			copyFieldsForLeftPanel(component, result);
			break;
		case SERVICE:
			result = new Service();
			copyFieldsForLeftPanel(component, result);
			break;
		default:
			break;
		}

		return result;
	}

	private Component filterFieldsForCatalog(Component component) {

		Component result = null;
		ComponentTypeEnum componentTypeEnum = component.getComponentType();
		switch (componentTypeEnum) {
		case RESOURCE:
			result = new Resource();
			copyFieldsForCatalog(component, result);
			break;
		case SERVICE:
			result = new Service();
			copyFieldsForCatalog(component, result);
			break;
		case PRODUCT:
			result = new Product();
			copyFieldsForCatalog(component, result);
		default:
			break;
		}

		return result;
	}

	/**
	 * Copy relevant fields to the filtered component for left panel
	 *
	 * @param component
	 * @param filteredComponent
	 */
	private void copyFieldsForLeftPanel(Component component, Component filteredComponent) {

		ComponentTypeEnum componentTypeEnum = component.getComponentType();
		filteredComponent.setCategories(component.getCategories());
		filteredComponent.setComponentType(component.getComponentType());
		if (ComponentTypeEnum.RESOURCE.equals(component.getComponentType())
				&& ResourceTypeEnum.VL.equals(((ResourceMetadataDataDefinition) component
						.getComponentMetadataDefinition().getMetadataDataDefinition()).getResourceType())) {
			filteredComponent.setCapabilities(component.getCapabilities());
			filteredComponent.setRequirements(component.getRequirements());
		}
		filteredComponent.setVersion(component.getVersion());
		filteredComponent.setDescription(component.getDescription());
		filteredComponent.setUniqueId(component.getUniqueId());
		filteredComponent.setIcon(component.getIcon());
		filteredComponent.setTags(component.getTags());
		// filteredComponent.setAllVersions(component.getAllVersions());
		filteredComponent.setLifecycleState(component.getLifecycleState());
		// filteredComponent.setHighestVersion(component.isHighestVersion());
		filteredComponent.setInvariantUUID(component.getInvariantUUID());
		filteredComponent.setUUID(component.getUUID());
		filteredComponent.setSystemName(component.getSystemName());
		filteredComponent.setName(component.getName());

		if (componentTypeEnum == ComponentTypeEnum.RESOURCE) {
			Resource resource = (Resource) component;
			Resource filteredResource = (Resource) filteredComponent;
			filteredResource.setToscaResourceName(resource.getToscaResourceName());
			// filteredResource.setAbstract(resource.isAbstract());
			// filteredResource.setVendorName(resource.getVendorName());
			// filteredResource.setVendorRelease(resource.getVendorRelease());
			filteredResource.setResourceType(resource.getResourceType());
		} else if (componentTypeEnum == ComponentTypeEnum.SERVICE) {
			// Service service = (Service)component;
			// Service filteredService = (Service)filteredComponent;
			// filteredService.setDistributionStatus(service.getDistributionStatus());
		}
	}

	private void copyFieldsForCatalog(Component component, Component filteredComponent) {

		ComponentTypeEnum componentTypeEnum = component.getComponentType();
		filteredComponent.setCategories(component.getCategories());
		filteredComponent.setComponentType(component.getComponentType());
		filteredComponent.setVersion(component.getVersion());
		filteredComponent.setDescription(component.getDescription());
		filteredComponent.setUniqueId(component.getUniqueId());
		filteredComponent.setIcon(component.getIcon());
		filteredComponent.setTags(component.getTags());
		// filteredComponent.setAllVersions(component.getAllVersions());
		filteredComponent.setLifecycleState(component.getLifecycleState());
		// filteredComponent.setHighestVersion(component.isHighestVersion());
		// filteredComponent.setInvariantUUID(component.getInvariantUUID());
		filteredComponent.setSystemName(component.getSystemName());
		filteredComponent.setName(component.getName());
		filteredComponent.setLastUpdateDate(component.getLastUpdateDate());

		if (componentTypeEnum == ComponentTypeEnum.RESOURCE) {
			Resource resource = (Resource) component;
			Resource filteredResource = (Resource) filteredComponent;
			filteredResource.setToscaResourceName(resource.getToscaResourceName());
			// filteredResource.setAbstract(resource.isAbstract());
			// filteredResource.setVendorName(resource.getVendorName());
			// filteredResource.setVendorRelease(resource.getVendorRelease());
			filteredResource.setResourceType(resource.getResourceType());
		} else if (componentTypeEnum == ComponentTypeEnum.SERVICE) {
			Service service = (Service) component;
			Service filteredService = (Service) filteredComponent;
			filteredService.setDistributionStatus(service.getDistributionStatus());
		}
	}

	/**
	 * get components from cache of a given list ou unique ids.
	 *
	 * for each component data from cassandra, unzip the data if needed and
	 * deserialize the unzipped data to java object(Component).
	 *
	 * @param filteredResources
	 * @return ImmutableTripple or ActionStatus. | |-- components |-- dirty
	 *         components - components with dirty flag = true. |-- set of non
	 *         cached components
	 *
	 */
	private Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> getComponentsFull(
			Set<String> filteredResources) {

		if (false == isEnabled()) {
			logger.debug("Component Cache is disabled");
			return Either.right(ActionStatus.NOT_ALLOWED);
		}

		List<Component> foundResources = new LinkedList<>();
		List<Component> foundDirtyResources = new LinkedList<>();
		Set<String> notFoundResources = new HashSet<>();
		ImmutableTriple<List<Component>, List<Component>, Set<String>> result = new ImmutableTriple<List<Component>, List<Component>, Set<String>>(
				foundResources, foundDirtyResources, notFoundResources);

		long cassandraFetchStart = System.currentTimeMillis();
		List<String> uidsList = new ArrayList<>();
		uidsList.addAll(filteredResources);
		Either<List<ComponentCacheData>, ActionStatus> componentsFromCache = componentCassandraDao
				.getComponents(uidsList);

		long cassandraFetchEnd = System.currentTimeMillis();
		logger.debug("Fetch time from cassandara of all components took {} ms",
				(cassandraFetchEnd - cassandraFetchStart));
		if (componentsFromCache.isRight()) {
			BeEcompErrorManager.getInstance().logInternalFlowError("FetchFromCache",
					"Failed to fetch components from cache", ErrorSeverity.ERROR);
			return Either.right(componentsFromCache.right().value());
		}

		List<ComponentCacheData> list = componentsFromCache.left().value();
		logger.debug("Number of components fetched from cassandra is {}", (list == null ? 0 : list.size()));
		if (list != null && false == list.isEmpty()) {

			List<ComponentCacheData> filteredData = list.stream().filter(p -> filteredResources.contains(p.getId()))
					.collect(Collectors.toList());
			logger.debug("Number of components filterd is {}", filteredData == null ? 0 : filteredData.size());

			if (filteredData != null) {
				long desStart = System.currentTimeMillis();

				for (ComponentCacheData componentCacheData : filteredData) {

					logger.debug("Process uid {} from cache", componentCacheData.getId());

					String compUid = componentCacheData.getId();

					Either<? extends Component, Boolean> deserializeExt = convertComponentCacheToComponent(
							componentCacheData);

					if (deserializeExt.isLeft()) {
						Component component = deserializeExt.left().value();
						if (false == componentCacheData.getIsDirty()) {
							foundResources.add(component);
						} else {
							foundDirtyResources.add(component);
						}
					} else {
						notFoundResources.add(compUid);
					}

				}
				long desEnd = System.currentTimeMillis();
				logger.debug("Deserialization and unzip of {} components took {} ms", filteredData.size(),
						(desEnd - desStart));
			}
		}
		List<String> foundResourcesUid = foundResources.stream().map(p -> p.getUniqueId()).collect(Collectors.toList());
		List<String> foundDirtyResourcesUid = foundDirtyResources.stream().map(p -> p.getUniqueId())
				.collect(Collectors.toList());
		logger.debug("Number of processed components from cache is {}",
				(foundResourcesUid.size() + foundDirtyResourcesUid.size()));
		Set<String> notCachedResources = filteredResources.stream()
				.filter(p -> false == foundResourcesUid.contains(p) && false == foundDirtyResourcesUid.contains(p))
				.collect(Collectors.toSet());
		notFoundResources.addAll(notCachedResources);

		if (logger.isDebugEnabled()) {
			logger.debug("Number of components fetched is {}", foundResources.size());
			logger.debug("Number of components fetched dirty is {}", foundDirtyResources.size());
			logger.debug("Number of components non cached is {}", notCachedResources.size());
		}

		return Either.left(result);
	}

	private Either<? extends Component, Boolean> convertComponentCacheToComponent(
			ComponentCacheData componentCacheData) {

		String compUid = componentCacheData.getId();

		byte[] dataAsArray = componentCacheData.getDataAsArray();

		if (true == componentCacheData.getIsZipped()) {
			long startUnzip = System.nanoTime();
			dataAsArray = ZipUtil.unzip(dataAsArray);
			long endUnzip = System.nanoTime();
			logger.trace("Unzip component {} took {} microsecond", compUid, (endUnzip - startUnzip) / 1000);
		}

		long startDes = System.nanoTime();

		Either<? extends Component, Boolean> deserializeExt = deserializeComponent(componentCacheData, dataAsArray);

		long endDes = System.nanoTime();
		logger.trace("Deserialize component {} took {} microsecond", compUid, (endDes - startDes) / 1000);
		return deserializeExt;
	}

	private Either<? extends Component, Boolean> deserializeComponent(ComponentCacheData componentCacheData,
			byte[] dataAsArray) {
		String type = componentCacheData.getType();
		NodeTypeEnum typeEnum = NodeTypeEnum.getByNameIgnoreCase(type);

		Either<? extends Component, Boolean> deserializeExt = Either.right(false);
		switch (typeEnum) {
		case Resource:
			deserializeExt = SerializationUtils.deserializeExt(dataAsArray, Resource.class, componentCacheData.getId());
			break;
		case Service:
			deserializeExt = SerializationUtils.deserializeExt(dataAsArray, Service.class, componentCacheData.getId());
			break;
		case Product:
			deserializeExt = SerializationUtils.deserializeExt(dataAsArray, Product.class, componentCacheData.getId());
			break;
		default:
			break;
		}
		return deserializeExt;
	}

	public Either<Component, ActionStatus> getComponent(String componentUid) {

		return getComponent(componentUid, null, Function.identity());

	}

	public Either<Component, ActionStatus> getComponent(String componentUid, Long lastModificationTime) {

		return getComponent(componentUid, lastModificationTime, Function.identity());

	}

	public boolean setComponent(String componentUid, Long lastModificationTime, NodeTypeEnum nodeTypeEnum) {

		boolean result = false;

		if (false == isEnabled()) {
			logger.debug("Component Cache is disabled");
			return false;
		}

		Either<Component, StorageOperationStatus> either = toscaOperationFacade.getToscaElement(componentUid);
		if (either.isLeft()) {
			Component component = either.left().value();
			result = saveComponent(componentUid, lastModificationTime, nodeTypeEnum, component);
		} else {
			logger.debug("Failed to get component {} of type {} from graph. Status is {}", componentUid,
					nodeTypeEnum.name().toLowerCase(), either.right().value());
		}

		return result;

	}

	private boolean saveComponent(String componentUid, Long lastModificationTime, NodeTypeEnum nodeTypeEnum,
			Component component) {

		logger.trace("Going to save component {} of type {} in cache", componentUid, nodeTypeEnum.name().toLowerCase());

		boolean result = false;

		Either<byte[], Boolean> serializeExt = SerializationUtils.serializeExt(component);
		if (serializeExt.isLeft()) {
			byte[] serializedData = serializeExt.left().value();
			byte[] zipBytes;
			try {
				zipBytes = ZipUtil.zipBytes(serializedData);
				ComponentCacheData componentCacheData = new ComponentCacheData();
				componentCacheData.setDataAsArray(zipBytes);
				componentCacheData.setIsZipped(true);
				componentCacheData.setId(componentUid);
				componentCacheData.setModificationTime(new Date(lastModificationTime));
				componentCacheData.setType(component.getComponentType().name().toLowerCase());

				CassandraOperationStatus status = componentCassandraDao.saveComponent(componentCacheData);

				if (status == CassandraOperationStatus.OK) {
					result = true;
				}

			} catch (IOException e) {
				logger.debug("Failed to prepare component {} of type {} for cache", componentUid,
						nodeTypeEnum.name().toLowerCase());
				if (logger.isTraceEnabled()) {
					logger.trace("Failed to prepare component {} of type {} for cache",componentUid,nodeTypeEnum.name().toLowerCase());
				}
			}
		} else {
			logger.debug("Failed to serialize component {} of type {} for cache", componentUid,
					nodeTypeEnum.name().toLowerCase());
		}
		return result;
	}

	public boolean setComponent(Component component, NodeTypeEnum nodeTypeEnum) {

		boolean result = false;

		if (false == isEnabled()) {
			logger.debug("Component Cache is disabled");
			return false;
		}

		String componentUid = component.getUniqueId();
		Long lastUpdateDate = component.getLastUpdateDate();

		result = saveComponent(componentUid, lastUpdateDate, nodeTypeEnum, component);

		return result;

	}

	/**
	 * get components from cache of a given list ou unique ids.
	 * 
	 * for each component data from cassandra, unzip the data if needed and
	 * deserialize the unzipped data to java object(Component).
	 * 
	 * @param filteredResources
	 * @return ImmutableTripple or ActionStatus. | |-- components |-- set of non
	 *         cached components
	 * 
	 */
	private Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> getComponentsFull(
			Map<String, Long> filteredResources) {

		if (false == isEnabled()) {
			logger.debug("Component Cache is disabled");
			return Either.right(ActionStatus.NOT_ALLOWED);
		}

		List<Component> foundResources = new LinkedList<>();
		Set<String> notFoundResources = new HashSet<>();
		ImmutablePair<List<Component>, Set<String>> result = new ImmutablePair<List<Component>, Set<String>>(
				foundResources, notFoundResources);

		long cassandraFetchStart = System.currentTimeMillis();

		Either<ImmutablePair<List<ComponentCacheData>, Set<String>>, ActionStatus> componentsFromCache = componentCassandraDao
				.getComponents(filteredResources);

		long cassandraFetchEnd = System.currentTimeMillis();
		logger.debug("Fetch time from cassandara of all components took {} ms",
				(cassandraFetchEnd - cassandraFetchStart));
		if (componentsFromCache.isRight()) {
			BeEcompErrorManager.getInstance().logInternalFlowError("FetchFromCache",
					"Failed to fetch components from cache", ErrorSeverity.ERROR);
			return Either.right(componentsFromCache.right().value());
		}

		ImmutablePair<List<ComponentCacheData>, Set<String>> immutablePair = componentsFromCache.left().value();
		List<ComponentCacheData> list = immutablePair.getLeft();
		logger.debug("Number of components fetched from cassandra is {}", (list == null ? 0 : list.size()));
		if (list != null && false == list.isEmpty()) {

			// List<ComponentCacheData> filteredData = list.stream().filter(p ->
			// filteredResources.contains(p.getId())).collect(Collectors.toList());
			logger.debug("Number of components filterd is {}", list == null ? 0 : list.size());

			if (list != null) {
				long desStart = System.currentTimeMillis();

				for (ComponentCacheData componentCacheData : list) {

					logger.debug("Process uid {} from cache", componentCacheData.getId());

					String compUid = componentCacheData.getId();

					Either<? extends Component, Boolean> deserializeExt = convertComponentCacheToComponent(
							componentCacheData);

					if (deserializeExt.isLeft()) {
						Component component = deserializeExt.left().value();
						foundResources.add(component);
					} else {
						notFoundResources.add(compUid);
					}

				}
				long desEnd = System.currentTimeMillis();
				logger.debug("Deserialization and unzip of {} components took {} ms", list.size(), (desEnd - desStart));
			}
		}
		logger.debug("Number of processed components from cache is {}", foundResources.size());

		Set<String> notFoundInCache = immutablePair.getRight();
		notFoundResources.addAll(notFoundInCache);

		if (logger.isDebugEnabled()) {
			logger.debug("Number of components fetched is {}", foundResources.size());
			logger.debug("Number of components non cached is {}", notFoundResources.size());
		}

		return Either.left(result);
	}

	/**
	 * get components for catalog
	 * 
	 * @param components
	 * @param componentTypeEnum
	 * @return
	 */
	public Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> getComponentsForCatalog(
			Map<String, Long> components, ComponentTypeEnum componentTypeEnum) {

		if (false == isEnabled()) {
			logger.debug("In getComponentsForCatalog for type {}. Cache is disabled.",
					componentTypeEnum.name().toLowerCase());
			return Either.right(ActionStatus.NOT_ALLOWED);
		}
		logger.debug("In getComponentsForCatalog for type {}", componentTypeEnum.name().toLowerCase());

		Function<List<Component>, List<Component>> filterFieldsFunc = x -> filterForCatalog(x);

		Map<String, Long> leftComponentsForSearch = new HashMap<>();
		leftComponentsForSearch.putAll(components);

		// get components from inmemory cache
		List<Component> componentsFromMemory = null;
		if (true == catalogInMemoryEnabled) {
			componentsFromMemory = getDataFromInMemoryCache(components.keySet(), componentTypeEnum);
			logger.debug("The number of components of type {} fetched from memory is {}",
					componentTypeEnum.name().toLowerCase(),
					componentsFromMemory == null ? 0 : componentsFromMemory.size());
			if (componentsFromMemory != null) {
				List<String> ignoredComponents = new ArrayList<>();
				for (Component componentFromMem : componentsFromMemory) {
					if (componentFromMem.getLastUpdateDate().longValue() != components
							.get(componentFromMem.getUniqueId()).longValue()) {
						// Ignore the component from memory
						ignoredComponents.add(componentFromMem.getUniqueId());
					}
				}

				logger.debug("Number of components from type {} ignored from memory cache is {}",
						componentTypeEnum.name().toLowerCase(), ignoredComponents.size());
				// remove from memory result the components which are not valid
				componentsFromMemory = componentsFromMemory.stream()
						.filter(p -> false == ignoredComponents.contains(p.getUniqueId())).collect(Collectors.toList());
				// Remove from leftComponentsForSearch the valid components from
				// memory
				componentsFromMemory.forEach(p -> leftComponentsForSearch.remove(p.getUniqueId()));

			}
		} else {
			logger.debug("Catalog InMemory cache is disabled");
		}

		logger.debug("Number of components from type {} needed to fetch is {}", componentTypeEnum.name().toLowerCase(),
				leftComponentsForSearch.size());

		// get components from cassandra cache and filter each component
		Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result = getComponents(
				leftComponentsForSearch, filterFieldsFunc);

		if (result.isLeft()) {
			// add inmemory components to the valid components(not dirty)
			List<Component> foundComponents = result.left().value().getLeft();
			if (componentsFromMemory != null) {
				foundComponents.addAll(componentsFromMemory);
			}
			if (true == catalogInMemoryEnabled) {
				updateCatalogInMemoryCacheWithCertified(foundComponents, componentTypeEnum);
			}
		}

		return result;
	}

	/**
	 * @param components
	 *            - Map of <componentUniqueId, last update date>
	 * @param filterFieldsFunc
	 * @return
	 */
	public Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> getComponents(Map<String, Long> components,
			Function<List<Component>, List<Component>> filterFieldsFunc) {

		if (false == isEnabled()) {
			logger.debug("Component Cache is disabled");
			return Either.right(ActionStatus.NOT_ALLOWED);
		}

		Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> componentsFull = getComponentsFull(
				components);

		if (componentsFull.isRight()) {
			return Either.right(componentsFull.right().value());
		}

		ImmutablePair<List<Component>, Set<String>> immutablePair = componentsFull.left().value();
		List<Component> foundResources = immutablePair.left;
		Set<String> notFoundResources = immutablePair.right;

		List<Component> filterdFoundResources = filterFieldsFunc.apply(foundResources);

		ImmutablePair<List<Component>, Set<String>> result = new ImmutablePair<List<Component>, Set<String>>(
				filterdFoundResources, notFoundResources);

		return Either.left(result);

	}

	/**
	 * get the component and its modification time from cache
	 * 
	 * @param componentUid
	 * @param filterFieldsFunc
	 * @return
	 */
	public Either<ImmutablePair<Component, Long>, ActionStatus> getComponentAndTime(String componentUid,
			Function<Component, Component> filterFieldsFunc) {

		Either<ImmutablePair<Component, ComponentCacheData>, ActionStatus> componentFromCache = getComponentFromCache(
				componentUid, null, filterFieldsFunc);

		if (componentFromCache.isRight()) {
			return Either.right(componentFromCache.right().value());
		}

		ImmutablePair<Component, ComponentCacheData> immutablePair = componentFromCache.left().value();

		ImmutablePair<Component, Long> result = new ImmutablePair<Component, Long>(immutablePair.left,
				immutablePair.right.getModificationTime().getTime());

		return Either.left(result);
	}

	private Either<ImmutablePair<Component, ComponentCacheData>, ActionStatus> getComponentFromCache(
			String componentUid, Long lastModificationTime, Function<Component, Component> filterFieldsFunc) {
		if (false == isEnabled()) {
			return Either.right(ActionStatus.NOT_ALLOWED);
		}

		Either<ComponentCacheData, ActionStatus> componentRes = componentCassandraDao.getComponent(componentUid);

		if (componentRes.isRight()) {
			return Either.right(componentRes.right().value());
		}

		ComponentCacheData componentCacheData = componentRes.left().value();

		if (lastModificationTime != null) {
			long cacheCompModificationTime = componentCacheData.getModificationTime().getTime();
			if (lastModificationTime != cacheCompModificationTime) {
				logger.debug(
						"Component {} found in cache but its modification time {} does not match to the timestamp in cache {}.",
						componentUid, lastModificationTime, cacheCompModificationTime);
				return Either.right(ActionStatus.INVALID_CONTENT);
			}
		}

		Either<? extends Component, Boolean> convertRes = convertComponentCacheToComponent(componentCacheData);
		if (convertRes.isRight()) {
			return Either.right(ActionStatus.CONVERT_COMPONENT_ERROR);
		}

		Component component = convertRes.left().value();

		Component filteredComponent = component;
		if (filterFieldsFunc != null) {
			filteredComponent = filterFieldsFunc.apply(component);
		}

		ImmutablePair<Component, ComponentCacheData> result = new ImmutablePair<Component, ComponentCacheData>(
				filteredComponent, componentCacheData);

		return Either.left(result);
	}

	public ActionStatus deleteComponentFromCache(String id) {
		if (false == isEnabled()) {
			return ActionStatus.NOT_ALLOWED;
		}
		CassandraOperationStatus status = this.componentCassandraDao.deleteComponent(id);
		if (CassandraOperationStatus.OK.equals(status)) {
			return ActionStatus.OK;
		} else {
			logger.debug("delete component failed with error {}", status);
			return ActionStatus.GENERAL_ERROR;
		}
	}

}
