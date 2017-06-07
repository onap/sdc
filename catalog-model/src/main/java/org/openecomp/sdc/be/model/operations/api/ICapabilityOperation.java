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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public interface ICapabilityOperation {

	public Either<CapabilityDefinition, StorageOperationStatus> addCapability(String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition);

	public Either<CapabilityDefinition, StorageOperationStatus> addCapability(String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition, boolean inTransaction);

	/**
	 * @param uniqueId
	 * @return
	 */
	public Either<CapabilityDefinition, StorageOperationStatus> getCapability(String uniqueId);

	public Either<CapabilityDefinition, StorageOperationStatus> getCapability(String uniqueId, boolean inTransaction);

	public Either<CapabilityDefinition, StorageOperationStatus> getCapability(String capabilityName, String resourceId);

	public Either<CapabilityDefinition, StorageOperationStatus> getCapability(String capabilityName, String resourceId, boolean inTransaction);

	public Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> getAllCapabilitiesPairs(String resourceId);

	public Either<Map<String, CapabilityDefinition>, StorageOperationStatus> deleteAllCapabilities(String resourceId, boolean inTransaction);

	public Either<CapabilityDefinition, TitanOperationStatus> getCapabilityByCapabilityData(CapabilityData capabilityData);

	public TitanOperationStatus getCapabilitySourcesList(String resourceId, List<String> derivedFromList);

	public Either<Map<String, PropertyData>, StorageOperationStatus> updatePropertiesOfCapability(String uniqueId, String capabilityType, List<PropertyDefinition> newProperties);

	public Either<Map<String, PropertyData>, StorageOperationStatus> updatePropertiesOfCapability(String uniqueId, String capabilityType, List<PropertyDefinition> newProperties, boolean inTransaction);

	StorageOperationStatus addCapability(TitanVertex metadataVertex, String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition, boolean inTransaction);

	Either<CapabilityTypeData, TitanOperationStatus> getCapabilityTypeOfCapability(String uniqueId);

}
