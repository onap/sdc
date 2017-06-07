package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("derived-resource-resolver")
public class ByToscaNameDerivedNodeTypeResolver implements DerivedNodeTypeResolver {

    @Autowired
    private TitanDao titanDao;

    @Override
    public Either<List<GraphVertex>, TitanOperationStatus> findDerivedResources(String parentResource) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new HashMap<GraphPropertyEnum, Object>();
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());

        propertiesToMatch.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, parentResource);
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        return titanDao.getByCriteria(VertexTypeEnum.NODE_TYPE, propertiesToMatch, JsonParseFlagEnum.NoParse);
    }
}
