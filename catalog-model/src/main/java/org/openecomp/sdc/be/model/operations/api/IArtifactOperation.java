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

import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public interface IArtifactOperation {

	public Either<ArtifactDefinition, StorageOperationStatus> addArifactToComponent(ArtifactDefinition artifactInfo, String id, NodeTypeEnum type, boolean failIfExist, boolean inTransaction);

	public Either<ArtifactDefinition, StorageOperationStatus> updateArifactOnResource(ArtifactDefinition artifactInfo, String id, String artifactId, NodeTypeEnum type, boolean inTransaction);

	public Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromResource(String id, String artifactId, NodeTypeEnum resource, boolean deleteMandatoryArtifact, boolean inTransaction);

	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType, boolean inTransaction);

	public StorageOperationStatus addArifactToComponent(ArtifactDefinition artifactInfo, String parentId, NodeTypeEnum type, boolean failIfExist, TitanVertex parentVertex);

}
