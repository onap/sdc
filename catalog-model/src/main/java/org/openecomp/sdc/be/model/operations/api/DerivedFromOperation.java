/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import fj.data.Either;
import java.util.function.Function;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public interface DerivedFromOperation {

    /**
     * @param parentUniqueId      the unique id of the object which is the parent of the derived from object
     * @param derivedFromUniqueId the unique id of the derived from object
     * @param nodeType            the type of the derived from and its parent objects
     * @return the status of the operation
     */
    Either<GraphRelation, StorageOperationStatus> addDerivedFromRelation(String parentUniqueId, String derivedFromUniqueId, NodeTypeEnum nodeType);

    /**
     * @param uniqueId the id of the entity of which to fetch its derived from object
     * @param nodeType the type of the derived from object
     * @param clazz    the class which represent the derived from object
     * @return the derived from object or error status of operation failed
     */
    <T extends GraphNode> Either<T, StorageOperationStatus> getDerivedFromChild(String uniqueId, NodeTypeEnum nodeType, Class<T> clazz);

    /**
     * @param uniqueId            the id of the entity of which to remove its derived from object
     * @param derivedFromUniqueId the unique id of the derived from object
     * @param nodeType            the type of the derived from and its parent objects
     * @return the status of the remove operation. if no derived from relation exists the operation is successful.
     */
    StorageOperationStatus removeDerivedFromRelation(String uniqueId, String derivedFromUniqueId, NodeTypeEnum nodeType);

    /**
     * Checks whether childCandidateType is derived from parentCandidateType
     */
    public <T extends GraphNode> Either<Boolean, StorageOperationStatus> isTypeDerivedFrom(String childCandidateType, String parentCandidateType,
                                                                                           String currentChildType, NodeTypeEnum capabilitytype,
                                                                                           Class<T> clazz, Function<T, String> typeProvider);

    /**
     * Checks whether replacement of oldTypeParent hold in DERIVED FROM with newTypeParent is legal
     */
    public <T extends GraphNode> StorageOperationStatus isUpdateParentAllowed(String oldTypeParent, String newTypeParent, String childType,
                                                                              NodeTypeEnum capabilitytype, Class<T> clazz,
                                                                              Function<T, String> typeProvider);
}
