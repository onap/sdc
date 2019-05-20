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

import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.resources.data.AdditionalInfoParameterData;

import java.util.List;

public interface IAdditionalInformationOperation {

    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> addAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String key, String value);

    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> updateAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String origKey, String key, String value);

    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> deleteAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String key);

    public Either<AdditionalInfoParameterData, JanusGraphOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String resourceUniqueId);

    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition parameters);

    public JanusGraphOperationStatus findResourceAllAdditionalInformationRecursively(String uniqueId, List<AdditionalInformationDefinition> properties);

    public JanusGraphOperationStatus findServiceAllAdditionalInformationRecursively(String uniqueId, List<AdditionalInformationDefinition> properties);

    public Either<AdditionalInformationDefinition, StorageOperationStatus> createAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String key, String value, boolean inTransaction);

    public Either<AdditionalInformationDefinition, StorageOperationStatus> updateAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, String key, String value, boolean inTransaction);

    public Either<AdditionalInformationDefinition, StorageOperationStatus> deleteAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, boolean inTransaction);

    public Either<Integer, StorageOperationStatus> getNumberOfAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean inTransaction);

    public Either<Integer, JanusGraphOperationStatus> getNumberOfParameters(NodeTypeEnum nodeType, String resourceId);

    public Either<AdditionalInfoParameterInfo, JanusGraphOperationStatus> getAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id);

    public Either<AdditionalInfoParameterInfo, StorageOperationStatus> getAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, boolean inTransaction);

    public Either<AdditionalInformationDefinition, JanusGraphOperationStatus> getAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean ignoreVerification);

    public Either<AdditionalInformationDefinition, StorageOperationStatus> getAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean ignoreVerification, boolean inTransaction);

    public Either<AdditionalInformationDefinition, StorageOperationStatus> deleteAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean inTransaction);

    public Either<JanusGraphVertex, JanusGraphOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, JanusGraphVertex matadatVertex);

    public JanusGraphOperationStatus addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition parameters, JanusGraphVertex metadataVertex);

}
