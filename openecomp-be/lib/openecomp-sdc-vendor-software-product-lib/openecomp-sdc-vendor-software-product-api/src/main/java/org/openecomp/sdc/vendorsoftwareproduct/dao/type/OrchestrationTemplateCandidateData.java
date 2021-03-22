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
package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;

@Getter
@Setter
@NoArgsConstructor
public class OrchestrationTemplateCandidateData {

    private ByteBuffer contentData;
    private String filesDataStructure;
    private String fileSuffix;
    private String fileName;
    private String validationData;
    private ByteBuffer originalFileContentData;
    private String originalFileName;
    private String originalFileSuffix;

    public OrchestrationTemplateCandidateData(final ByteBuffer contentData, final String dataStructureJson, final String fileSuffix,
                                              final String fileName, final String originalFileName, final String originalFileSuffix,
                                              final ByteBuffer originalFileContentData) {
        this.contentData = contentData;
        this.filesDataStructure = dataStructureJson;
        this.fileSuffix = fileSuffix;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.originalFileSuffix = originalFileSuffix;
        this.originalFileContentData = originalFileContentData;
    }

    public ValidationStructureList getValidationDataStructure() {
        return validationData == null ? null : JsonUtil.json2Object(validationData, ValidationStructureList.class);
    }
}
