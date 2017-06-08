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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.Map;


public class ResourceTranslationCinderVolumeImpl extends ResourceTranslationBase {

  @Override
  public void translate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType(ToscaNodeType.CINDER_VOLUME);
    nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),translateTo.
            getResourceId(),translateTo.getResource().getProperties(),
            nodeTemplate.getProperties(), translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            nodeTemplate, translateTo.getContext()));
    handleSizeProperty(nodeTemplate.getProperties());
    String toscaReadOnlyPropName =
        HeatToToscaUtil.getToscaPropertyName(translateTo, HeatConstants.READ_ONLY_PROPERTY_NAME);
    Object readOnlyPropVal = nodeTemplate.getProperties().get(toscaReadOnlyPropName);
    if (readOnlyPropVal != null && !(readOnlyPropVal instanceof Map)) {
      nodeTemplate.getProperties().put(toscaReadOnlyPropName, HeatBoolean.eval(readOnlyPropVal));
    }
    DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
        nodeTemplate);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }


  private void handleSizeProperty(Map<String, Object> nodeTemplateProperties) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Object size = nodeTemplateProperties.get("size");
    if (size == null) {
      return;
    }

    if (size instanceof Map) {
      Map<String, Object> propMap = (Map) size;
      for (Map.Entry entry : propMap.entrySet()) {
        String val = "(" + entry.getKey() + " : " + entry.getValue() + ") * 1024";
        nodeTemplateProperties.put("size", val);

        mdcDataDebugMessage.debugExitMessage(null, null);
        return;
      }
    } else {
      nodeTemplateProperties.put("size", size + "*1024");
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }
}
