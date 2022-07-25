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

import fj.data.Either;
import java.util.List;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.resources.data.ArtifactData;

public interface IGroupOperation {

    Either<List<GraphRelation>, StorageOperationStatus> dissociateAllGroupsFromArtifactOnGraph(String componentId, NodeTypeEnum componentTypeEnum,
                                                                                               String artifactId);

    StorageOperationStatus dissociateAndAssociateGroupsFromArtifact(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId,
                                                                    ArtifactData newArtifact, boolean inTransaction);

    boolean isGroupExist(String groupName, boolean inTransaction);

    /**
     * Validates and updates the given property value based on the property type
     *
     * @param groupOwner the container component that owns the group instance that has the property
     * @param property   the group instance property to validate
     * @return the status of the operation
     */
    StorageOperationStatus validateAndUpdatePropertyValue(Component groupOwner, GroupProperty property);
}
