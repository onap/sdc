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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ComputeConsolidationData {

    //Key - Service template file name
    private final Map<String, FileComputeConsolidationData> fileComputeConsolidationData;

    public ComputeConsolidationData() {
        fileComputeConsolidationData = new HashMap<>();
    }

    public Set<String> getAllServiceTemplateFileNames() {
        return fileComputeConsolidationData.keySet();
    }

    public FileComputeConsolidationData getFileComputeConsolidationData(String serviceTemplateFileName) {
        return fileComputeConsolidationData.get(serviceTemplateFileName);
    }

    public void setFileComputeConsolidationData(String serviceTemplateFileName,
              FileComputeConsolidationData fileComputeConsolidationData) {
        this.fileComputeConsolidationData.put(serviceTemplateFileName, fileComputeConsolidationData);
    }

    /**
     * add compute template consolidation data entity if it doesn't exist yet
     * base on given parameters.
     *
     * @return compute template consolidation data entity by given keys
    */
    ComputeTemplateConsolidationData addComputeTemplateConsolidationData(
            String serviceTemplateFileName, String computeNodeType, String computeNodeTemplateId) {
        FileComputeConsolidationData consolidationData = addFileComputeConsolidationData(serviceTemplateFileName);
        return consolidationData.addComputeTemplateConsolidationData(computeNodeType, computeNodeTemplateId);
    }

    private FileComputeConsolidationData addFileComputeConsolidationData(String serviceTemplateFileName) {
        FileComputeConsolidationData consolidationData = getFileComputeConsolidationData(serviceTemplateFileName);
        if (consolidationData == null) {
            consolidationData = new FileComputeConsolidationData();
            setFileComputeConsolidationData(serviceTemplateFileName, consolidationData);
        }
        return consolidationData;
    }

    /**
     * Is number of compute types legal.
     *
     * @param serviceTemplateName the service template name
     * @return the boolean
     */
    public boolean isNumberOfComputeTypesLegal(String serviceTemplateName) {
        FileComputeConsolidationData fileComputeConsolidationData =
                getFileComputeConsolidationData(serviceTemplateName);
        return Objects.nonNull(fileComputeConsolidationData)
                       && fileComputeConsolidationData.isNumberOfComputeTypesLegal();
    }
}
