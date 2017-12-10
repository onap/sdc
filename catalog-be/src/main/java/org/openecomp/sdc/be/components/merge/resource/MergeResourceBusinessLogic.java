package org.openecomp.sdc.be.components.merge.resource;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;

public interface MergeResourceBusinessLogic {

    ActionStatus mergeResourceEntities(Resource oldResource, Resource newResource);

}
