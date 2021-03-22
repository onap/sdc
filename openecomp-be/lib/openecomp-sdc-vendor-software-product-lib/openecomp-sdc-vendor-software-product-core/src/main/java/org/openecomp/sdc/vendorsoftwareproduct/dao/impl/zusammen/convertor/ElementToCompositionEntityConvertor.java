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
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;

public class ElementToCompositionEntityConvertor extends ElementConvertor<CompositionEntity> {

    @Override
    public CompositionEntity convert(Element element) {
        CompositionEntity compositionEntity = null;
        switch (getElementType(element)) {
            case ComponentQuestionnaire:
                compositionEntity = new ComponentEntity();
                break;
            case VSPQuestionnaire:
                compositionEntity = new VspQuestionnaireEntity();
                break;
            case ImageQuestionnaire:
                compositionEntity = new ImageEntity();
                break;
            case ComputeQuestionnaire:
                compositionEntity = new ComponentEntity();
                break;
            case NicQuestionnaire:
                compositionEntity = new NicEntity();
        }
        if (compositionEntity != null) {
            compositionEntity.setId(element.getElementId().getValue());
            compositionEntity.setQuestionnaireData(element.getData() == null ? null : new String(FileUtils.toByteArray(element.getData())));
            return compositionEntity;
        } else {
            return null;
        }
    }
}
