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
package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import java.nio.ByteBuffer;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;

public class ElementToProcessConvertor extends ElementConvertor<ProcessEntity> {

    public static final String NAME = "name";
    public static final String ARTIFACT_NAME = "artifactName";
    public static final String DESCRIPTION = "description";
    public static final String PROCESS_TYPE = "processType";

    @Override
    public ProcessEntity convert(Element element) {
        if (element == null) {
            return null;
        }
        ProcessEntity processEntity = new ProcessEntity();
        processEntity.setId(element.getElementId().getValue());
        processEntity.setArtifact(ByteBuffer.wrap(FileUtils.toByteArray(element.getData())));
        mapInfoToProcessEntity(processEntity, element.getInfo());
        return processEntity;
    }

    @Override
    public ProcessEntity convert(ElementInfo elementInfo) {
        if (elementInfo == null) {
            return null;
        }
        ProcessEntity processEntity = new ProcessEntity();
        processEntity.setId(elementInfo.getId().getValue());
        mapInfoToProcessEntity(processEntity, elementInfo.getInfo());
        return processEntity;
    }

    public void mapInfoToProcessEntity(ProcessEntity processEntity, Info info) {
        processEntity.setName(info.getProperty(NAME));
        processEntity.setArtifactName(info.getProperty(ARTIFACT_NAME));
        processEntity.setDescription(info.getProperty(DESCRIPTION));
        processEntity.setType(info.getProperty(PROCESS_TYPE) != null ? ProcessType.valueOf(info.getProperty(PROCESS_TYPE)) : null);
    }
}
