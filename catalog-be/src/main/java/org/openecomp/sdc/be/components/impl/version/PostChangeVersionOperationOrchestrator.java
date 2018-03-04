package org.openecomp.sdc.be.components.impl.version;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

@org.springframework.stereotype.Component
public class PostChangeVersionOperationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PostChangeVersionOperationOrchestrator.class);
    private final List<PostChangeVersionOperation> postChangeVersionOperations;

    public PostChangeVersionOperationOrchestrator(List<PostChangeVersionOperation> postChangeVersionOperations) {
        this.postChangeVersionOperations = postChangeVersionOperations;
    }

    public ActionStatus doPostChangeVersionOperations(Component container, ComponentInstance prevVersion, ComponentInstance newVersion) {
        log.debug("#doPostChangeVersionOperations - starting post change version operations for component {}. from instance {} to instance {}", container.getUniqueId(), prevVersion.getUniqueId(), newVersion.getUniqueId());
        ActionStatus postOperationsResult = ActionStatus.OK;
        Iterator<PostChangeVersionOperation> postChangeVersionIter = postChangeVersionOperations.iterator();
        while(postChangeVersionIter.hasNext() && postOperationsResult == ActionStatus.OK) {
            postOperationsResult = postChangeVersionIter.next().onChangeVersion(container, prevVersion, newVersion);
        }
        return postOperationsResult;
    }

}
