package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;

public interface OnDeleteEntityOperation {

    /**
     * side effect operation to be executed when a given entity is deleted
     * @param container the container which holds the entity to be deleted
     * @param deletedEntityId the id of the entity that was deleted
     * @return the status of the on delete operation
     */
    ActionStatus onDelete(Component container, String deletedEntityId);

}
