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

import org.openecomp.sdc.be.model.*;

import fj.data.Either;

public interface IServiceOperation extends IComponentOperation {

	public Either<Service, StorageOperationStatus> createService(Service service);

	public Either<Service, StorageOperationStatus> createService(Service service, boolean inTransaction);

	public Either<Service, StorageOperationStatus> getService(String uniqueId);

	public Either<Service, StorageOperationStatus> getService(String uniqueId, boolean inTransaction);

	public Either<Service, StorageOperationStatus> getService(String uniqueId, ComponentParametersView componentParametersView, boolean inTransaction);

	public Either<Service, StorageOperationStatus> deleteService(String uniqueId);

	public Either<Service, StorageOperationStatus> deleteService(String uniqueId, boolean inTransaction);

	public Either<Boolean, StorageOperationStatus> validateServiceNameExists(String serviceName);

	public Either<List<Service>, StorageOperationStatus> getFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates, boolean inTransaction);

	public Either<Service, StorageOperationStatus> updateService(Service service, boolean inTransaction);

	public Either<Set<Service>, StorageOperationStatus> getCatalogData(Map<String, Object> propertiesToMatch, boolean inTransaction);

	public Either<List<Service>, StorageOperationStatus> getTesterFollowed(String userId, Set<LifecycleStateEnum> lifecycleStates, boolean inTransaction);

	public Either<Set<Service>, StorageOperationStatus> getCertifiedServicesWithDistStatus(Map<String, Object> propertiesToMatch, Set<DistributionStatusEnum> distStatus, boolean inTransaction);

	public Either<Service, StorageOperationStatus> updateDestributionStatus(Service service, User user, DistributionStatusEnum distributionStatus);

	public Either<List<Service>, StorageOperationStatus> getServiceCatalogData(boolean inTransaction);

	public Either<List<Service>, StorageOperationStatus> getServiceCatalogDataLatestCertifiedAndNotCertified(boolean inTransaction);

	public Either<Service, StorageOperationStatus> getServiceByNameAndVersion(String name, String version, Map<String, Object> additionalParams, boolean inTransaction);

	public Either<Service, StorageOperationStatus> getServiceByNameAndVersion(String name, String version);

	public Either<Service, StorageOperationStatus> getServiceBySystemNameAndVersion(String name, String version, boolean inTransaction);

	public Either<List<Service>, StorageOperationStatus> getServiceListByUuid(String uuid, boolean inTransaction);

	public Either<List<Service>, StorageOperationStatus> getLatestServiceByUuid(String uuid, boolean inTransaction);

	public Either<List<Service>, StorageOperationStatus> getServiceListBySystemName(String systemName, boolean inTransaction);

	Either<List<Service> , StorageOperationStatus> getAll();
}
