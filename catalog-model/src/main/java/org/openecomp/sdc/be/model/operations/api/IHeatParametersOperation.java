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

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.resources.data.HeatParameterValueData;

import fj.data.Either;

public interface IHeatParametersOperation {

	public StorageOperationStatus addPropertiesToGraph(List<HeatParameterDefinition> properties, String resourceId, NodeTypeEnum nodeType);

	public StorageOperationStatus getHeatParametersOfNode(NodeTypeEnum nodeType, String uniqueId, List<HeatParameterDefinition> properties);

	public Either<List<HeatParameterDefinition>, StorageOperationStatus> deleteAllHeatParametersAssociatedToNode(NodeTypeEnum nodeType, String uniqueId);

	public StorageOperationStatus deleteAllHeatValuesAssociatedToNode(NodeTypeEnum parentNodeType, String parentUniqueId);

	public StorageOperationStatus validateAndUpdateProperty(HeatParameterDefinition heatParam);

	public Either<HeatParameterValueData, StorageOperationStatus> updateHeatParameterValue(HeatParameterDefinition heatParam, String artifactId, String resourceInstanceId, String artifactLabel);

	public StorageOperationStatus updateHeatParameters(List<HeatParameterDefinition> properties);
}
