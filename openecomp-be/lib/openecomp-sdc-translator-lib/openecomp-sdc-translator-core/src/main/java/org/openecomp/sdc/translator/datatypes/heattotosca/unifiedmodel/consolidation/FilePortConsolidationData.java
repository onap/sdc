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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatResourceUtil;

public class FilePortConsolidationData {

    //key - port node template id
    private final Map<String, PortTemplateConsolidationData> portTemplateConsolidationData;

    public FilePortConsolidationData() {
        this.portTemplateConsolidationData = new HashMap<>();
    }

    public Set<String> getAllPortNodeTemplateIds() {
        return portTemplateConsolidationData.keySet();
    }

    public Collection<PortTemplateConsolidationData> getAllPortConsolidationData() {
        return portTemplateConsolidationData.values();
    }

    public PortTemplateConsolidationData getPortTemplateConsolidationData(String portNodeTemplateId) {
        return portTemplateConsolidationData.get(portNodeTemplateId);
    }

    public void setPortTemplateConsolidationData(String portNodeTemplateId,
                                               PortTemplateConsolidationData
                                                   portTemplateConsolidationData) {
        this.portTemplateConsolidationData.put(portNodeTemplateId, portTemplateConsolidationData);
    }

    /**
    * If entity doesn't exist yet - create subInterface template consolidation data entity
    * base on given parameters.
    *
    * @return subInterface template consolidation data entity
    */
    SubInterfaceTemplateConsolidationData addSubInterfaceTemplateConsolidationData(
            Resource resource, String subInterfaceNodeTemplateId, String parentPortNodeTemplateId) {
        PortTemplateConsolidationData consolidationData =
                addPortTemplateConsolidationData(parentPortNodeTemplateId);
        return consolidationData.addSubInterfaceTemplateConsolidationData(resource,
                             subInterfaceNodeTemplateId, parentPortNodeTemplateId);
    }

    /**
    * If entity doesn't exist yet - create subInterface template consolidation data entity
    * base on given parameters.
    *
    * @return subInterface template consolidation data entity
    */
    SubInterfaceTemplateConsolidationData addSubInterfaceTemplateConsolidationData(
            Resource resource, String subInterfaceNodeTemplateId, String parentPortNodeTemplateId,
            String parentPortResourceId, String parentPortResourceType) {

        PortTemplateConsolidationData consolidationData =
                addPortTemplateConsolidationData(parentPortNodeTemplateId, parentPortResourceId,
                parentPortResourceType);

        return consolidationData.addSubInterfaceTemplateConsolidationData(resource,
                subInterfaceNodeTemplateId, parentPortNodeTemplateId);
    }

    /**
    * If entity doesn't exist yet - create port template consolidation data and
    * update it's network role according to given resource parameters.
    *
    * @return port template consolidation data entity by given keys
    */
    PortTemplateConsolidationData addPortTemplateConsolidationData(
            String portNodeTemplateId, String portResourceId, String portResourceType) {
        PortTemplateConsolidationData consolidationData = getPortTemplateConsolidationData(portNodeTemplateId);
        if (consolidationData == null) {
            consolidationData = createPortTemplateConsolidationData(portNodeTemplateId,
              portResourceId, portResourceType);
            setPortTemplateConsolidationData(portNodeTemplateId, consolidationData);
        }
        return consolidationData;
    }

    /**
     * If entity doesn't exist yet - create port template consolidation data and.
     *
     * @return port template consolidation data entity by given keys
     */
    private PortTemplateConsolidationData addPortTemplateConsolidationData(String portNodeTemplateId) {
        PortTemplateConsolidationData consolidationData = getPortTemplateConsolidationData(portNodeTemplateId);
        if (consolidationData == null) {
            consolidationData = new PortTemplateConsolidationData();
            consolidationData.setNodeTemplateId(portNodeTemplateId);
            setPortTemplateConsolidationData(portNodeTemplateId, consolidationData);
        }
        return consolidationData;
    }
    
    private PortTemplateConsolidationData createPortTemplateConsolidationData(String portNodeTemplateId,
            String portResourceId, String portResourceType) {
        PortTemplateConsolidationData consolidationData = new PortTemplateConsolidationData();
        consolidationData.setNodeTemplateId(portNodeTemplateId);
        Optional<String> portNetworkRole = HeatResourceUtil.evaluateNetworkRoleFromResourceId(portResourceId,
                portResourceType);
        portNetworkRole.ifPresent(consolidationData::setNetworkRole);
        return consolidationData;
    }
}
