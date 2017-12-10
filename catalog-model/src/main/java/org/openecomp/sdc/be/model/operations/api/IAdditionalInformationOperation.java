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

import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.resources.data.AdditionalInfoParameterData;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public interface IAdditionalInformationOperation {

	public Either<AdditionalInformationDefinition, TitanOperationStatus> addAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String key, String value);

	public Either<AdditionalInformationDefinition, TitanOperationStatus> updateAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String origKey, String key, String value);

	public Either<AdditionalInformationDefinition, TitanOperationStatus> deleteAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String key);

	public Either<AdditionalInfoParameterData, TitanOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String resourceUniqueId);

	public Either<AdditionalInformationDefinition, TitanOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition parameters);

	public TitanOperationStatus findResourceAllAdditionalInformationRecursively(String uniqueId, List<AdditionalInformationDefinition> properties);

	public TitanOperationStatus findServiceAllAdditionalInformationRecursively(String uniqueId, List<AdditionalInformationDefinition> properties);

	public Either<AdditionalInformationDefinition, StorageOperationStatus> createAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String key, String value, boolean inTransaction);

	public Either<AdditionalInformationDefinition, StorageOperationStatus> updateAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, String key, String value, boolean inTransaction);

	public Either<AdditionalInformationDefinition, StorageOperationStatus> deleteAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, boolean inTransaction);

	public Either<Integer, StorageOperationStatus> getNumberOfAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean inTransaction);

	public Either<Integer, TitanOperationStatus> getNumberOfParameters(NodeTypeEnum nodeType, String resourceId);

	public Either<AdditionalInfoParameterInfo, TitanOperationStatus> getAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id);

	public Either<AdditionalInfoParameterInfo, StorageOperationStatus> getAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, boolean inTransaction);

	public Either<AdditionalInformationDefinition, TitanOperationStatus> getAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean ignoreVerification);

	public Either<AdditionalInformationDefinition, StorageOperationStatus> getAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean ignoreVerification, boolean inTransaction);

	public Either<AdditionalInformationDefinition, StorageOperationStatus> deleteAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean inTransaction);

	public Either<TitanVertex, TitanOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, TitanVertex matadatVertex);

	public TitanOperationStatus addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition parameters, TitanVertex metadataVertex);

}
