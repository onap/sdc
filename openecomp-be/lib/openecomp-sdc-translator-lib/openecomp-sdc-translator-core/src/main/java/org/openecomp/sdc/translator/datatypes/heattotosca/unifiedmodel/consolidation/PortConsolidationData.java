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

import org.openecomp.sdc.heat.datatypes.model.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PortConsolidationData {

    //Key - Service template file name
    private final Map<String, FilePortConsolidationData> filePortConsolidationData;

    public PortConsolidationData() {
        this.filePortConsolidationData = new HashMap<>();
    }

    public Set<String> getAllServiceTemplateFileNames() {
        return filePortConsolidationData.keySet();
    }

    public FilePortConsolidationData getFilePortConsolidationData(String serviceTemplateFileName) {
        return filePortConsolidationData.get(serviceTemplateFileName);
    }

    public void setFilePortConsolidationData(String serviceTemplateFileName, FilePortConsolidationData
            filePortConsolidationData) {
        this.filePortConsolidationData.put(serviceTemplateFileName, filePortConsolidationData);
    }

    /**
    * Create port template consolidation data base on given parameters - if it doesn't exist yet.
    *
    * @return port template consolidation data
    */
    PortTemplateConsolidationData addPortTemplateConsolidationData(String serviceTemplateFileName,
            String portNodeTemplateId, String portResourceId, String portResourceType) {
        FilePortConsolidationData consolidationData = addFilePortConsolidationData(serviceTemplateFileName);
        return consolidationData
                   .addPortTemplateConsolidationData(portNodeTemplateId, portResourceId, portResourceType);
    }

    /**
    * Create subInterface template consolidation data base on given parameters - if it doesn't exist yet.
    *
    * @return port template consolidation data by given keys
    */
    SubInterfaceTemplateConsolidationData addSubInterfaceTemplateConsolidationData(
            String serviceTemplateFileName, Resource resource, String subInterfaceNodeTemplateId,
            String parentPortNodeTemplateId) {
        FilePortConsolidationData consolidationData = addFilePortConsolidationData(serviceTemplateFileName);
        return consolidationData.addSubInterfaceTemplateConsolidationData(
            resource, subInterfaceNodeTemplateId, parentPortNodeTemplateId);
    }

    /**
     * Create subInterface template consolidation data base on given parameters - if it doesn't exist yet.
     *
     * @return port template consolidation data by given keys
     */
    SubInterfaceTemplateConsolidationData addSubInterfaceTemplateConsolidationData(
                String serviceTemplateFileName, Resource resource,
                String subInterfaceNodeTemplateId, String parentPortNodeTemplateId,
                String parentPortResourceId, String parentPortResourceType) {
        FilePortConsolidationData consolidationData = addFilePortConsolidationData(serviceTemplateFileName);
        return consolidationData.addSubInterfaceTemplateConsolidationData(
                resource, subInterfaceNodeTemplateId, parentPortNodeTemplateId,
                parentPortResourceId, parentPortResourceType);
    }

    private FilePortConsolidationData addFilePortConsolidationData(String serviceTemplateFileName) {
        FilePortConsolidationData consolidationData = getFilePortConsolidationData(serviceTemplateFileName);
        if (consolidationData == null) {
            consolidationData = new FilePortConsolidationData();
            setFilePortConsolidationData(serviceTemplateFileName, consolidationData);
        }
        return consolidationData;
    }
}
