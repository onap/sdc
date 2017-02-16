/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.Map;
import java.util.Optional;

public class ResourceTranslationCinderVolumeImpl extends ResourceTranslationBase {

  @Override
  public void translate(TranslateTo translateTo) {
    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType(ToscaNodeType.CINDER_VOLUME.getDisplayName());

    nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getResource().getProperties(),
            nodeTemplate.getProperties(), translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            nodeTemplate, translateTo.getContext()));
    handleSizeProperty(nodeTemplate.getProperties());
    Object readOnly = nodeTemplate.getProperties().get("read_only");
    if (readOnly != null && !(readOnly instanceof Map)) {
      nodeTemplate.getProperties().put("read_only", HeatBoolean.eval(readOnly));
    }
    Optional<String> resourceTranslatedId = getResourceTranslatedId(translateTo.getHeatFileName(),
        translateTo.getHeatOrchestrationTemplate(), translateTo.getResourceId(),
        translateTo.getContext());
    if (resourceTranslatedId.isPresent()) {
      DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), resourceTranslatedId.get(),
          nodeTemplate);
    }
  }

  private void handleSizeProperty(Map<String, Object> nodeTemplateProperties) {
    Object size = nodeTemplateProperties.get("size");
    if (size == null) {
      return;
    }

    if (size instanceof Map) {
      Map<String, Object> propMap = (Map) size;
      if (propMap.entrySet().iterator().hasNext()) {
        Map.Entry entry = propMap.entrySet().iterator().next();
        String val = "(" + entry.getKey() + " : " + entry.getValue() + ") * 1024";
        nodeTemplateProperties.put("size", val);
        return;
      }
    } else {
      nodeTemplateProperties.put("size", size + "*1024");
    }
  }
}
