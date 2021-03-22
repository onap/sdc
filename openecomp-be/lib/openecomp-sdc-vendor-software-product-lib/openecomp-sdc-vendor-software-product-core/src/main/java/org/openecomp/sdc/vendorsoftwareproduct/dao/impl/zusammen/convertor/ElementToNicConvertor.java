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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.types.ElementPropertyName;

public class ElementToNicConvertor extends ElementConvertor<NicEntity> {

    @Override
    public NicEntity convert(Element element) {
        NicEntity nicEntity = new NicEntity();
        nicEntity.setId(element.getElementId().getValue());
        nicEntity.setCompositionData(new String(FileUtils.toByteArray(element.getData())));
        mapInfoToNicEntity(nicEntity, element.getInfo());
        return nicEntity;
    }

    @Override
    public NicEntity convert(ElementInfo elementInfo) {
        NicEntity nicEntity = new NicEntity();
        nicEntity.setId(elementInfo.getId().getValue());
        mapInfoToNicEntity(nicEntity, elementInfo.getInfo());
        return nicEntity;
    }

    public void mapInfoToNicEntity(NicEntity nicEntity, Info info) {
        nicEntity.setCompositionData(info.getProperty(ElementPropertyName.compositionData.name()));
    }
}
