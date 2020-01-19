/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
