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

import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToProcessConvertor.ARTIFACT_NAME;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import java.nio.ByteBuffer;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;

public class ElementToComponentMonitoringUploadConvertor extends ElementConvertor<ComponentMonitoringUploadEntity> {

    @Override
    public ComponentMonitoringUploadEntity convert(Element element) {
        ComponentMonitoringUploadEntity mibEntity = new ComponentMonitoringUploadEntity();
        mibEntity.setId(element.getElementId().getValue());
        mibEntity.setArtifact(ByteBuffer.wrap(FileUtils.toByteArray(element.getData())));
        mapInfoToComponentMonitoringUploadEntity(mibEntity, element.getInfo());
        return mibEntity;
    }

    @Override
    public ComponentMonitoringUploadEntity convert(ElementInfo elementInfo) {
        ComponentMonitoringUploadEntity mibEntity = new ComponentMonitoringUploadEntity();
        mibEntity.setId(elementInfo.getId().getValue());
        mapInfoToComponentMonitoringUploadEntity(mibEntity, elementInfo.getInfo());
        return mibEntity;
    }

    public void mapInfoToComponentMonitoringUploadEntity(ComponentMonitoringUploadEntity mibEntity, Info info) {
        mibEntity.setArtifactName((String) info.getProperties().get(ARTIFACT_NAME));
        mibEntity.setType(MonitoringUploadType.valueOf(info.getName()));
    }
}
