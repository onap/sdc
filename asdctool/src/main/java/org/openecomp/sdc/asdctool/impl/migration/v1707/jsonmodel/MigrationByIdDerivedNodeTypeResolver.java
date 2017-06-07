package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

public class MigrationByIdDerivedNodeTypeResolver implements DerivedNodeTypeResolver {

    @Resource(name = "titan-dao")
    private TitanDao titanDao;

    @Override
    public Either<List<GraphVertex>, TitanOperationStatus> findDerivedResources(String parentResource) {
        return titanDao.getVertexById(parentResource, JsonParseFlagEnum.ParseMetadata).left().map(Collections::singletonList);
    }
}
