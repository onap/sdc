package org.openecomp.sdc.be.model.operations.api;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import fj.data.Either;

public interface DerivedFromOperation {

    /**
     *
     * @param parentUniqueId the unique id of the object which is the parent of the derived from object
     * @param derivedFromUniqueId the unique id of the derived from object
     * @param nodeType the type of the derived from and its parent objects
     * @return the status of the operation
     */
    Either<GraphRelation, StorageOperationStatus> addDerivedFromRelation(String parentUniqueId, String derivedFromUniqueId, NodeTypeEnum nodeType);

    /**
     *
     * @param uniqueId the id of the entity of which to fetch its derived from object
     * @param nodeType the type of the derived from object
     * @param clazz the class which represent the derived from object
     * @return the derived from object or error status of operation failed
     */
    <T extends GraphNode> Either<T, StorageOperationStatus> getDerivedFromChild(String uniqueId, NodeTypeEnum nodeType, Class<T> clazz);

    /**
     *
     * @param uniqueId the id of the entity of which to remove its derived from object
     * @param derivedFromUniqueId the unique id of the derived from object
     * @param nodeType the type of the derived from and its parent objects
     * @return the status of the remove operation. if no derived from relation exists the operation is successful.
     */
    StorageOperationStatus removeDerivedFromRelation(String uniqueId, String derivedFromUniqueId, NodeTypeEnum nodeType);
}
