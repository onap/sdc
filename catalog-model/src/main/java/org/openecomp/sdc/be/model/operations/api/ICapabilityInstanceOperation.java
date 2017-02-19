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
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.resources.data.CapabilityInstData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

/**
 * public interface ICapabilityInstanceOperation provides methods for CRUD
 * operations for CapabilityInstance on component instance level
 * 
 * @author ns019t
 *
 */
public interface ICapabilityInstanceOperation {
	/**
	 * create capability instance of capability with property values for
	 * resource instance
	 * 
	 * @param resourceInstanceId
	 * @param capabilityId
	 * @param propertyValues
	 * @param validateCapabilityInstExistance
	 * @param capabilityName
	 * @return
	 */
	public Either<Map<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> createCapabilityInstanceOfCapabilityWithPropertyValuesForResourceInstance(
			String resourceInstanceId, String capabilityId, String capabilityName,
			List<ComponentInstanceProperty> propertyValues, boolean validateCapabilityInstExistance);

	/**
	 * 
	 * @param resourceInstanceVertex
	 * @param capabilityId
	 * @param capabilityName
	 * @param propertyValues
	 * @param validateCapabilityInstExistence
	 * @return
	 */
	public TitanOperationStatus createCapabilityInstanceOfCapabilityWithPropertyValuesForResourceInstance(
			TitanVertex resourceInstanceVertex, String resourceInstanceId, String capabilityId, String capabilityName,
			List<ComponentInstanceProperty> propertyValues, boolean validateCapabilityInstExistence);

	/**
	 * validate capability instance uniqueness
	 * 
	 * @param resourceInstanceId
	 * @param capabilityId
	 * @return
	 */
	public Either<Boolean, TitanOperationStatus> validateCapabilityInstExistence(String resourceInstanceId,
			String capabilityId);

	/**
	 * delete capability instance from resource instance
	 * 
	 * @param resourceInstanceId
	 * @param capabilityInstanceId
	 * @return
	 */
	public Either<CapabilityInstData, TitanOperationStatus> deleteCapabilityInstanceFromResourceInstance(
			String resourceInstanceId, String capabilityInstanceId);

	/**
	 * get all capability instances for resource instance returns all Capability
	 * Instances related to Resource Instance as List<CapabilityInstData> or
	 * TitanOperationStatus if error occurs or if Resource Instance have no any
	 * related Capability Instance
	 * 
	 * @param resourceInstanceId
	 * @return Either<List<CapabilityInstData>, TitanOperationStatus>
	 */
	public Either<List<ImmutablePair<CapabilityInstData, GraphEdge>>, TitanOperationStatus> getAllCapabilityInstancesOfResourceInstance(
			String resourceInstanceId);

	/**
	 * get capability instance of capability for resource instance
	 * 
	 * @param resourceInstanceId
	 * @param capabilityId
	 * @return
	 */
	public Either<CapabilityInstData, TitanOperationStatus> getCapabilityInstanceOfCapabilityOfResourceInstance(
			String resourceInstanceId, String capabilityId);

	/**
	 * update capability property values
	 * 
	 * @param resourceInstanceId
	 * @param capabilityInstanceId
	 * @param propertyValues
	 * @param capabilityId
	 * @return
	 */
	public Either<List<PropertyValueData>, TitanOperationStatus> updateCapabilityPropertyValues(
			String resourceInstanceId, String capabilityId, List<ComponentInstanceProperty> propertyValues);

	/**
	 * clone and associate capability instance with property values
	 * 
	 * @param createdComponentInstance
	 * @param capability
	 * @param capabilityInstPair
	 * @return
	 */
	public Either<ImmutablePair<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> cloneAssociateCapabilityInstanceWithPropertyValues(
			ComponentInstanceData createdComponentInstance, CapabilityDefinition capability,
			ImmutablePair<CapabilityInstData, GraphEdge> capabilityInstPair);

	Either<Boolean, TitanOperationStatus> validateCapabilityInstExistence(TitanVertex instanceVertex,
			String resourceInstanceId, String capabilityId);
}
