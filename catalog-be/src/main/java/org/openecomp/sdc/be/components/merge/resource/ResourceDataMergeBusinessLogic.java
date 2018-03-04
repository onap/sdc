package org.openecomp.sdc.be.components.merge.resource;

import org.openecomp.sdc.be.components.merge.instance.ComponentsMergeCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourceDataMergeBusinessLogic implements MergeResourceBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDataMergeBusinessLogic.class);

    private List<ComponentsMergeCommand> componentMergingCommands;

    public ResourceDataMergeBusinessLogic(List<ComponentsMergeCommand> componentMergingCommands) {
        this.componentMergingCommands = componentMergingCommands;
    }

    @Override
    public ActionStatus mergeResourceEntities(Resource oldResource, Resource newResource) {
        if (oldResource == null) {
            return ActionStatus.OK;
        }
        for (ComponentsMergeCommand componentMergeCommand : componentMergingCommands) {
            ActionStatus mergeStatus = componentMergeCommand.mergeComponents(oldResource, newResource);
            if (mergeStatus != ActionStatus.OK) {
                LOGGER.error("failed on merge command {} of resource {} status is {}", componentMergeCommand.description(), newResource.getUniqueId(), mergeStatus);
                return mergeStatus;
            }
        }
        return ActionStatus.OK;
    }

}
