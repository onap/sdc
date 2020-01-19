/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.merge.resource;

import org.openecomp.sdc.be.components.merge.ComponentsMergeCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourceDataMergeBusinessLogic implements MergeResourceBusinessLogic {

    private static final Logger log = Logger.getLogger(ResourceDataMergeBusinessLogic.class);
    public static final int FIRST_COMMAND = 0;
    public static final int PENULTIMATE_COMMAND = Integer.MAX_VALUE - 1;
    public static final int LAST_COMMAND = Integer.MAX_VALUE;
    public static final int ANY_ORDER_COMMAND = 1;

    private MergeCommandsFactory mergeCommandsFactory;

    public ResourceDataMergeBusinessLogic(MergeCommandsFactory mergeCommandsFactory) {
        this.mergeCommandsFactory = mergeCommandsFactory;
    }

    @Override
    public ActionStatus mergeResourceEntities(Resource oldResource, Resource newResource) {
        if (oldResource == null) {
            return ActionStatus.OK;
        }
        return mergeCommandsFactory.getMergeCommands(oldResource, newResource)
                .either(mergeCommands -> executeMergeCommands(oldResource, newResource, mergeCommands),
                        err -> err);
    }

    private ActionStatus executeMergeCommands(Resource oldResource, Resource newResource, List<? extends ComponentsMergeCommand> componentMergingCommands) {
        for (ComponentsMergeCommand componentMergeCommand : componentMergingCommands) {
            ActionStatus mergeStatus = componentMergeCommand.mergeComponents(oldResource, newResource);
            if (mergeStatus != ActionStatus.OK) {
                log.debug("failed on merge command {} of resource {} status is {}", componentMergeCommand.description(), newResource.getUniqueId(), mergeStatus);
                return mergeStatus;
            }
        }
        return ActionStatus.OK;
    }

}
