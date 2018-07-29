package org.openecomp.sdc.be.components.merge;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;

public interface ComponentsMergeCommand {

    /**
     * encapsulates the logic of merging component inner entities from the previous component into the currently updated component
     * @param prevComponent the old component, whose entities need to be merged
     * @param currentComponent the new component, whose entities need to be merged
     * @return the status of the merge process
     */
    ActionStatus mergeComponents(Component prevComponent, Component currentComponent);

    /**
     *
     * @return short description of the command for logging purposes
     */
    String description();

}
