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

public class NestedConsolidationData {

    //Key - Service template file name
    private final Map<String, FileNestedConsolidationData> fileNestedConsolidationData;

    public NestedConsolidationData() {
        this.fileNestedConsolidationData = new HashMap<>();
    }

    public Set<String> getAllServiceTemplateFileNames() {
        return fileNestedConsolidationData.keySet();
    }

    public FileNestedConsolidationData getFileNestedConsolidationData(String serviceTemplateFileName) {
        return fileNestedConsolidationData.get(serviceTemplateFileName);
    }

    public void setFileNestedConsolidationData(String serviceTemplateFileName,
            FileNestedConsolidationData fileNestedConsolidationData) {
        this.fileNestedConsolidationData.put(serviceTemplateFileName, fileNestedConsolidationData);
    }

    /**
    * Create nested template consolidation data base on given key - if it doesn't exist yet.
    *
    * @return nested template consolidation data by given keys
    */
    NestedTemplateConsolidationData addNestedTemplateConsolidationData(
            String serviceTemplateFileName, String nestedNodeTemplateId) {

        FileNestedConsolidationData consolidationData = addFileNestedConsolidationData(serviceTemplateFileName);
        return consolidationData.addNestedTemplateConsolidationData(nestedNodeTemplateId);
    }

    private FileNestedConsolidationData addFileNestedConsolidationData(String serviceTemplateFileName) {
        FileNestedConsolidationData consolidationData = getFileNestedConsolidationData(serviceTemplateFileName);
        if (consolidationData == null) {
            consolidationData = new FileNestedConsolidationData();
            setFileNestedConsolidationData(serviceTemplateFileName, consolidationData);
        }
        return consolidationData;
    }

    public boolean isNestedConsolidationDataExist(String nestedServiceTemplateName) {
        return Objects.nonNull(getFileNestedConsolidationData(nestedServiceTemplateName));
    }
}
