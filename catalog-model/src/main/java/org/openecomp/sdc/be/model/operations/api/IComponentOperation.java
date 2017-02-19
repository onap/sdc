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

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;

import fj.data.Either;

public interface IComponentOperation {
	public <T extends Component> Either<T, StorageOperationStatus> getComponent(String id, Class<T> clazz);

	public Either<List<ArtifactDefinition>, StorageOperationStatus> getComponentArtifactsForDelete(String parentId,
			NodeTypeEnum parentType, boolean inTransacton);

	public <T> Either<T, StorageOperationStatus> getLightComponent(String id, boolean inTransaction);

	public <T> Either<T, StorageOperationStatus> getComponent(String id, boolean inTransaction);

	public <T> Either<List<T>, StorageOperationStatus> getFilteredComponents(Map<FilterKeyEnum, String> filters,
			boolean inTranscation);

	public <T extends GraphNode> Either<T, StorageOperationStatus> getComponentByLabelAndId(String uniqueId,
			NodeTypeEnum nodeType, Class<T> clazz);

}
