package org.openecomp.sdc.be.model;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.jsontitan.datamodel.NodeType;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.List;

public interface DerivedNodeTypeResolver {

    Either<List<GraphVertex>, TitanOperationStatus> findDerivedResources(String parentResource);

}
