package org.openecomp.sdc.be.components.merge.resource;

import fj.data.Either;
import org.openecomp.sdc.be.components.merge.ComponentsGlobalMergeCommand;
import org.openecomp.sdc.be.components.merge.ComponentsMergeCommand;
import org.openecomp.sdc.be.components.merge.TopologyComparator;
import org.openecomp.sdc.be.components.merge.VspComponentsMergeCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MergeCommandsFactory {

    private final List<ComponentsGlobalMergeCommand> globalMergeCommands;
    private final List<VspComponentsMergeCommand> mergeCommands;
    private final TopologyComparator topologyComparator;

    public MergeCommandsFactory(List<ComponentsGlobalMergeCommand> globalMergeCommands, List<VspComponentsMergeCommand> mergeCommands, TopologyComparator topologyComparator) {
        this.globalMergeCommands = globalMergeCommands;
        this.mergeCommands = mergeCommands;
        this.topologyComparator = topologyComparator;
    }

    public Either<List<? extends ComponentsMergeCommand>, ActionStatus> getMergeCommands(Resource prevResource, Resource currResource) {
        return topologyComparator.isTopologyChanged(prevResource, currResource)
                .left()
                .map(topologyChanged -> topologyChanged ? globalMergeCommands : mergeCommands);

    }
}
