package org.openecomp.sdc.be.components.impl.version;

import org.openecomp.sdc.be.dao.api.ActionStatus;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.common.log.wrappers.Logger;


import java.util.Iterator;
import java.util.List;

import java.util.function.Function;




@org.springframework.stereotype.Component
public class VesionUpdateHandler {
    
    private static final Logger log = Logger.getLogger(VesionUpdateHandler.class);

    private final List<OnChangeVersionCommand> onChangeVersionOperations;
    
    public VesionUpdateHandler( List<OnChangeVersionCommand> onChangeVersionOperations) {
 
        this.onChangeVersionOperations = onChangeVersionOperations;
    }
    
    
    public ActionStatus doPostChangeVersionCommand(Component container) {
        log.debug("#doPostChangeVersionOperations - starting post change version operations for component {}. from instance {} to instance {}", container.getUniqueId());
        Function<OnChangeVersionCommand, ActionStatus> vesionChangeTaskRunner = operation -> operation.onChangeVersion(container);
        return doOnChangeVesionOperations(vesionChangeTaskRunner);
    }
    
    private ActionStatus doOnChangeVesionOperations(Function<OnChangeVersionCommand, ActionStatus> vesionChangeTaskRunner) {
        ActionStatus onVesionChangeResult = ActionStatus.OK;
        Iterator<OnChangeVersionCommand> onChangeVesionIter = onChangeVersionOperations.iterator();
        while(onChangeVesionIter.hasNext() && onVesionChangeResult == ActionStatus.OK) {
            onVesionChangeResult = vesionChangeTaskRunner.apply(onChangeVesionIter.next());
        }
        return onVesionChangeResult;
    }
    
    
  
    

}
