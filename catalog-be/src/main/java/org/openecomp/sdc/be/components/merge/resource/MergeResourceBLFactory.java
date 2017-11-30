package org.openecomp.sdc.be.components.merge.resource;


import fj.data.Either;
import org.openecomp.sdc.be.components.merge.GlobalTypesMergeBusinessLogic;
import org.openecomp.sdc.be.components.merge.TopologyComparator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class MergeResourceBLFactory {

    @javax.annotation.Resource
    private ResourceDataMergeBusinessLogic resourceDataMergeBusinessLogic;

    @javax.annotation.Resource
    private GlobalTypesMergeBusinessLogic globalTypesMergeBusinessLogic;

    @javax.annotation.Resource
    private TopologyComparator topologyComparator;

    public Either<MergeResourceBusinessLogic, ActionStatus> getInstance(Resource oldResource, Resource newResource) {
        Either<Boolean, ActionStatus> isTopologyChangeEither = topologyComparator.isTopologyChanged(oldResource, newResource);
        return isTopologyChangeEither.bimap(this::getInstance, actionStatus -> actionStatus);
    }

    private MergeResourceBusinessLogic getInstance(boolean topologyChanged) {
        return topologyChanged ? globalTypesMergeBusinessLogic : resourceDataMergeBusinessLogic;
    }

}
