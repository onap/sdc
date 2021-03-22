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
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.types.ElementPropertyName;

public class ElementToImageConvertor extends ElementConvertor<ImageEntity> {

    @Override
    public ImageEntity convert(Element element) {
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(element.getElementId().getValue());
        imageEntity.setCompositionData(new String(FileUtils.toByteArray(element.getData())));
        mapInfoToImageEntity(imageEntity, element.getInfo());
        return imageEntity;
    }

    @Override
    public ImageEntity convert(ElementInfo elementInfo) {
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(elementInfo.getId().getValue());
        mapInfoToImageEntity(imageEntity, elementInfo.getInfo());
        return imageEntity;
    }

    public void mapInfoToImageEntity(ImageEntity imageEntity, Info info) {
        imageEntity.setCompositionData(info.getProperty(ElementPropertyName.compositionData.name()));
    }
}
