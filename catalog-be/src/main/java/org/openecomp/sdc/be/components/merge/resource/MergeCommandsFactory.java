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

import fj.data.Either;
import java.util.List;
import org.openecomp.sdc.be.components.merge.ComponentsGlobalMergeCommand;
import org.openecomp.sdc.be.components.merge.ComponentsMergeCommand;
import org.openecomp.sdc.be.components.merge.TopologyComparator;
import org.openecomp.sdc.be.components.merge.VspComponentsMergeCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class MergeCommandsFactory {

    private final List<ComponentsGlobalMergeCommand> globalMergeCommands;
    private final List<VspComponentsMergeCommand> mergeCommands;
    private final TopologyComparator topologyComparator;

    public MergeCommandsFactory(List<ComponentsGlobalMergeCommand> globalMergeCommands, List<VspComponentsMergeCommand> mergeCommands,
                                TopologyComparator topologyComparator) {
        this.globalMergeCommands = globalMergeCommands;
        this.mergeCommands = mergeCommands;
        this.topologyComparator = topologyComparator;
    }

    public Either<List<? extends ComponentsMergeCommand>, ActionStatus> getMergeCommands(Resource prevResource, Resource currResource) {
        return topologyComparator.isTopologyChanged(prevResource, currResource).left()
            .map(topologyChanged -> topologyChanged ? globalMergeCommands : mergeCommands);
    }
}
