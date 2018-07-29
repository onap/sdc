/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.openecomp.sdc.translator.services.heattotosca.ConsolidationEntityType;

import java.util.Optional;

public class ConsolidationData {

    private final ComputeConsolidationData computeConsolidationData;
    private final ComputeConsolidationDataHandler computeConsolidationDataHandler;

    private final PortConsolidationData portConsolidationData;
    private final PortConsolidationDataHandler portConsolidationDataHandler;
    private final SubInterfaceConsolidationDataHandler subInterfaceConsolidationDataHandler;

    private NestedConsolidationData nestedConsolidationData;
    private final NestedConsolidationDataHandler nestedConsolidationDataHandler;

    public ConsolidationData() {

        computeConsolidationData = new ComputeConsolidationData();
        computeConsolidationDataHandler = new ComputeConsolidationDataHandler(computeConsolidationData);

        portConsolidationData = new PortConsolidationData();
        portConsolidationDataHandler = new PortConsolidationDataHandler(portConsolidationData);
        subInterfaceConsolidationDataHandler = new SubInterfaceConsolidationDataHandler(portConsolidationData);

        nestedConsolidationData = new NestedConsolidationData();
        nestedConsolidationDataHandler = new NestedConsolidationDataHandler(nestedConsolidationData);

    }

    /**
     * Get Consolidation data handler by entity type.
     *
     * @return If there is no consolidation handler for a type, return an empty {@link Optional}.
     */
    public Optional<ConsolidationDataHandler> getConsolidationDataHandler(ConsolidationEntityType type) {

        switch (type) {
            case COMPUTE:
                return Optional.of(getComputeConsolidationDataHandler());
            case PORT:
                return Optional.of(getPortConsolidationDataHandler());
            case SUB_INTERFACE:
                return Optional.of(getSubInterfaceConsolidationDataHandler());
            case NESTED:
            case VFC_NESTED:
                return Optional.of(getNestedConsolidationDataHandler());
            default:
                return Optional.empty();
        }
    }

    public ComputeConsolidationDataHandler getComputeConsolidationDataHandler() {
        return computeConsolidationDataHandler;
    }

    public PortConsolidationDataHandler getPortConsolidationDataHandler() {
        return portConsolidationDataHandler;
    }

    public NestedConsolidationDataHandler getNestedConsolidationDataHandler() {
        return nestedConsolidationDataHandler;
    }

    public SubInterfaceConsolidationDataHandler getSubInterfaceConsolidationDataHandler() {
        return subInterfaceConsolidationDataHandler;
    }

    public ComputeConsolidationData getComputeConsolidationData() {
        return computeConsolidationData;
    }

    public PortConsolidationData getPortConsolidationData() {
        return portConsolidationData;
    }

    public NestedConsolidationData getNestedConsolidationData() {
        return nestedConsolidationData;
    }

    public void setNestedConsolidationData(NestedConsolidationData nestedConsolidationData) {
        this.nestedConsolidationData = nestedConsolidationData;
    }
}
