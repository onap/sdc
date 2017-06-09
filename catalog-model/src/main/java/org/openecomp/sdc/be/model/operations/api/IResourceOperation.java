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

package org.openecomp.sdc.be.model.operations.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;

import fj.data.Either;

public interface IResourceOperation extends IComponentOperation {

	public TitanGenericDao getTitanGenericDao();

	// public StorageOperationStatus lockResource(Resource resource);
	//
	// public StorageOperationStatus unlockResource(Resource resource);

	public Either<Resource, StorageOperationStatus> createResource(Resource resource);

	public Either<Resource, StorageOperationStatus> createResource(Resource resource, boolean inTransaction);

	public Either<Resource, StorageOperationStatus> getResource(String resourceId);

	// public Either<Resource, StorageOperationStatus> getResource_tx(String
	// resourceId,boolean inTransaction);

	public Either<Resource, StorageOperationStatus> getResource(String resourceId, boolean inTransaction);

	/**
	 * the method retrieves all the certified resources, the returned values are only abstract or only none abstract according to the supplied parameters.
	 * 
	 * @param getAbstract
	 *            the value defines which resources to return only abstract or only none abstract
	 * @return
	 */
	public Either<List<Resource>, StorageOperationStatus> getAllCertifiedResources(boolean getAbstract);

	public Either<List<Resource>, StorageOperationStatus> getAllCertifiedResources(boolean getAbstract, Boolean isHighest);

	public Either<Boolean, StorageOperationStatus> validateResourceNameExists(String resourceName, ResourceTypeEnum resourceType);

	public Either<Resource, StorageOperationStatus> deleteResource(String resourceId);

	public Either<Resource, StorageOperationStatus> deleteResource(String resourceId, boolean inTransaction);

	public Either<Resource, StorageOperationStatus> updateResource(Resource resource);

	public Either<Resource, StorageOperationStatus> updateResource(Resource resource, boolean inTransaction);

	public Either<Integer, StorageOperationStatus> getNumberOfResourcesByName(String resourceName);

	// public Either<List<ArtifactDefinition>, StorageOperationStatus>
	// getResourceArtifactsForDelete(Resource resource);

	public Either<List<Resource>, StorageOperationStatus> getFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates, boolean inTransaction);

	public Either<Set<Resource>, StorageOperationStatus> getCatalogData(Map<String, Object> propertiesToMatch, boolean inTransaction);

	public Either<Resource, StorageOperationStatus> getLatestByName(String resourceName, boolean inTransaction);

	public Either<Resource, StorageOperationStatus> overrideResource(Resource resource, Resource resourceSaved, boolean inTransaction);

	public Either<List<Resource>, StorageOperationStatus> getTesterFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, boolean inTransaction);

	public Either<List<Resource>, StorageOperationStatus> getResourceListByUuid(String uuid, boolean inTransaction);

	public Either<List<Resource>, StorageOperationStatus> getLatestResourceByUuid(String uuid, boolean inTransaction);

	public Either<List<Resource>, StorageOperationStatus> getResourceListBySystemName(String systemName, boolean inTransaction);

	public Either<List<Resource>, StorageOperationStatus> getResourceCatalogData(boolean inTransaction);

	public Either<List<Resource>, StorageOperationStatus> getResourceCatalogDataVFLatestCertifiedAndNonCertified(boolean inTransaction);

	public Either<List<Resource>, StorageOperationStatus> getResourceByNameAndVersion(String name, String version, boolean inTransaction);

	public Either<List<Resource>, StorageOperationStatus> getResourceByNameAndVersion(String name, String version);

	public Either<Resource, StorageOperationStatus> getResourceBySystemNameAndVersion(String name, String version, Map<String, Object> additionalParams, boolean inTransaction);

	// public Either<List<Resource>, StorageOperationStatus>
	// getAllNotCheckoutResources(boolean getAbstract);

	// public Either<List<Resource>, StorageOperationStatus>
	// getAllNotCheckoutResources(boolean getAbstract, Boolean isHighest);

	public Either<List<String>, StorageOperationStatus> getAllResourcesMarkedForDeletion();

	public Either<Boolean, StorageOperationStatus> isResourceInUse(String resourceToDelete);

	public Either<Resource, StorageOperationStatus> getLatestByToscaResourceName(String toscaResourceName, boolean inTransaction);

	public Either<Boolean, StorageOperationStatus> validateToscaResourceNameExists(String templateName);
	
	public Either<Boolean, StorageOperationStatus> validateToscaResourceNameExtends(String templateNameCurrent, String templateNameExtends);

	/**
	 *
	 * @param resource the resource to look for its derived resources
	 * @return all resources which derives from the given resource
	 */
	Either<List<Resource>, StorageOperationStatus> getAllDerivedResources(Resource resource);

	/**
	 *
	 * @return all root resources (i.e all normatives with tosca name {@code Resource.ROOT_RESOURCE}
	 */
	Either<List<Resource>, StorageOperationStatus> getRootResources();

	/**
	 *
	 * @return all resources with type VF
	 */
	Either<List<Resource>, StorageOperationStatus> getVFResources();

	/**
	 *
	 * @return all resources
	 */
	Either<List<Resource>, StorageOperationStatus> getAll();

}
