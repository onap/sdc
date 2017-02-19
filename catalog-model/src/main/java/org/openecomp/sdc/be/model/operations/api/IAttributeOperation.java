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

import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.resources.data.AttributeData;
import org.openecomp.sdc.be.resources.data.AttributeValueData;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public interface IAttributeOperation {
	Either<AttributeData, StorageOperationStatus> deleteAttribute(String attributeId);

	TitanOperationStatus addAttributesToGraph(TitanVertex metadataVertex, Map<String, AttributeDefinition> attributes, String resourceId, Map<String, DataTypeDefinition> dataTypes);

	Either<List<ComponentInstanceAttribute>, TitanOperationStatus> getAllAttributesOfResourceInstance(ComponentInstance compInstance);

	TitanOperationStatus findAllResourceAttributesRecursively(String resourceId, List<AttributeDefinition> attributes);

	Either<Map<String, AttributeDefinition>, StorageOperationStatus> deleteAllAttributeAssociatedToNode(NodeTypeEnum nodeType, String uniqueId);

	TitanOperationStatus findNodeNonInheretedAttribues(String uniqueId, NodeTypeEnum nodeType, List<AttributeDefinition> attributes);

	Either<AttributeData, StorageOperationStatus> addAttribute(AttributeDefinition attributeDefinition, String resourceId);

	Either<AttributeData, TitanOperationStatus> addAttributeToGraph(AttributeDefinition attribute, String resourceId, Map<String, DataTypeDefinition> dataTypes);

	AttributeDefinition convertAttributeDataToAttributeDefinition(AttributeData attributeData, String attributeName, String resourceId);

	Either<AttributeData, StorageOperationStatus> updateAttribute(String attributeId, AttributeDefinition newAttDef, Map<String, DataTypeDefinition> dataTypes);

	/**
	 * Builds ComponentInstanceAttribute from AttributeValueData
	 * 
	 * @param attributeValueData
	 * @param resourceInstanceAttribute
	 * @return
	 */
	ComponentInstanceAttribute buildResourceInstanceAttribute(AttributeValueData attributeValueData, ComponentInstanceAttribute resourceInstanceAttribute);

	TitanOperationStatus addAttributeToGraphByVertex(TitanVertex metadataVertex, AttributeDefinition attribute, String resourceId, Map<String, DataTypeDefinition> dataTypes);

}
