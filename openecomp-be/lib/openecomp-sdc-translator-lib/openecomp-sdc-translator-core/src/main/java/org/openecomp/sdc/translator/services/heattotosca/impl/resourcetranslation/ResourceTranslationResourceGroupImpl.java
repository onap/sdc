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

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.InvalidPropertyValueErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ResourceTranslationResourceGroupImpl extends ResourceTranslationBase {

  @Override
  protected void translate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    final String heatFileName = translateTo.getHeatFileName();
    Object resourceDef =
        translateTo.getResource().getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
    Resource nestedResource = new Resource();
    Object typeDefinition = ((Map) resourceDef).get("type");
    if (!(typeDefinition instanceof String)) {
      logger.warn("Resource '" + translateTo.getResourceId() + "' of type'"
          + HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource()
          + "' with resourceDef which is not pointing to nested heat file is not supported and "
          + "will be ignored in the translation ");
      return;
    }
    String type = (String) typeDefinition;
    if (!HeatToToscaUtil.isYmlFileType(type)) {
      logger.warn("Resource '" + translateTo.getResourceId() + "' of type'"
          + HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource()
          + "' with resourceDef which is not pointing to nested heat file is not supported and "
          + "will be ignored in the translation ");

      mdcDataDebugMessage.debugExitMessage(null, null);
      return;
    }

    nestedResource.setType(type);
    nestedResource.setProperties((Map<String, Object>) ((Map) resourceDef).get("properties"));
    nestedResource.setMetadata(((Map) resourceDef).get("metadata"));

    Optional<String> substitutionNodeTemplateId =
        ResourceTranslationFactory.getInstance(nestedResource)
            .translateResource(heatFileName, translateTo.getServiceTemplate(),
                translateTo.getHeatOrchestrationTemplate(), nestedResource,
                translateTo.getResourceId(), translateTo.getContext());
    if (substitutionNodeTemplateId.isPresent()) {
      NodeTemplate substitutionNodeTemplate =
          DataModelUtil.getNodeTemplate(translateTo.getServiceTemplate(), substitutionNodeTemplateId.get());
      if(!Objects.isNull(substitutionNodeTemplate)) {
        Map serviceTemplateFilter = (Map<String, Object>) substitutionNodeTemplate.getProperties()
            .get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);

        populateServiceTemplateFilterProperties(translateTo, substitutionNodeTemplate,
            serviceTemplateFilter);
        handlingIndexVar(translateTo, substitutionNodeTemplate);
        DataModelUtil
            .addNodeTemplate(translateTo.getServiceTemplate(), substitutionNodeTemplateId.get(),
                substitutionNodeTemplate);
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handlingIndexVar(TranslateTo translateTo, NodeTemplate substitutionNodeTemplate) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String indexVarValue = getIndexVarValue(translateTo);
    replacePropertiesIndexVarValue(indexVarValue, substitutionNodeTemplate.getProperties());

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private Map<String, List> getNewIndexVarValue() {
    final Map<String, List> newIndexVarValue = new HashMap<>();
    List indexVarValList = new ArrayList<>();
    indexVarValList.add(ToscaConstants.MODELABLE_ENTITY_NAME_SELF);
    indexVarValList.add(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
    indexVarValList.add(ToscaConstants.INDEX_VALUE_PROPERTY_NAME);
    newIndexVarValue.put(ToscaFunctions.GET_PROPERTY.getDisplayName(), indexVarValList);
    return newIndexVarValue;
  }

  private void replacePropertiesIndexVarValue(String indexVarValue,
                                              Map<String, Object> properties) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (properties == null || properties.isEmpty()) {
      return;
    }

    for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
      Object propertyValue = propertyEntry.getValue();
      Object newPropertyValue = getUpdatedPropertyValueWithIndex(indexVarValue, propertyValue);
      if (newPropertyValue != null) {
        properties.put(propertyEntry.getKey(), newPropertyValue);
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private Object getUpdatedPropertyValueWithIndex(String indexVarValue, Object propertyValue) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (propertyValue != null && propertyValue instanceof String) {
      if (propertyValue.equals(indexVarValue)) {
        return getNewIndexVarValue();
      }
      if (((String) propertyValue).contains(indexVarValue)) {
        Map<String, List<Object>> concatMap = new HashMap<>();
        List<Object> concatList = new ArrayList<>();
        String value = (String) propertyValue;

        while (value.contains(indexVarValue)) {
          if (value.indexOf(indexVarValue) == 0) {
            concatList.add(getNewIndexVarValue());
            value = value.substring(indexVarValue.length());
          } else {
            int end = value.indexOf(indexVarValue);
            concatList.add(value.substring(0, end));
            value = value.substring(end);
          }
        }
        if (!value.isEmpty()) {
          concatList.add(value);
        }

        concatMap.put(ToscaFunctions.CONCAT.getDisplayName(), concatList);
        return concatMap;
      }

      mdcDataDebugMessage.debugExitMessage(null, null);
      return propertyValue; //no update is needed
    } else if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      replacePropertiesIndexVarValue(indexVarValue, (Map<String, Object>) propertyValue);
      return propertyValue;
    } else if (propertyValue instanceof List && !((List) propertyValue).isEmpty()) {
      List newPropertyValueList = new ArrayList<>();
      for (Object entry : ((List) propertyValue)) {
        newPropertyValueList.add(getUpdatedPropertyValueWithIndex(indexVarValue, entry));
      }

      mdcDataDebugMessage.debugExitMessage(null, null);
      return newPropertyValueList;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return propertyValue;
  }

  private String getIndexVarValue(TranslateTo translateTo) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    Object indexVar =
        translateTo.getResource().getProperties().get(HeatConstants.INDEX_PROPERTY_NAME);
    if (indexVar == null) {
      return HeatConstants.RESOURCE_GROUP_INDEX_VAR_DEFAULT_VALUE;
    }

    if (indexVar instanceof String) {

      mdcDataDebugMessage.debugExitMessage(null, null);
      return (String) indexVar;
    } else {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_RESOURCE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_PROPERTY);
      throw new CoreException(
          new InvalidPropertyValueErrorBuilder("index_var", indexVar.toString(), "String").build());
    }
  }

  private void populateServiceTemplateFilterProperties(TranslateTo translateTo,
                                                       NodeTemplate substitutionNodeTemplate,
                                                       Map serviceTemplateFilter) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    boolean mandatory = false;
    Object countValue = TranslatorHeatToToscaPropertyConverter
        .getToscaPropertyValue(translateTo.getServiceTemplate(),translateTo.getResourceId(),
            ToscaConstants.COUNT_PROPERTY_NAME, translateTo.getResource().getProperties().get
                (ToscaConstants.COUNT_PROPERTY_NAME), null,
            translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(),
            substitutionNodeTemplate, translateTo.getContext());

    if (countValue != null) {
      serviceTemplateFilter.put(ToscaConstants.COUNT_PROPERTY_NAME, countValue);
    } else {
      serviceTemplateFilter.put(ToscaConstants.COUNT_PROPERTY_NAME, 1);
    }
    if (countValue instanceof Integer && (Integer) countValue > 0) {
      mandatory = true;
    }
    if (countValue == null) {
      mandatory = true;
    }
    serviceTemplateFilter.put("mandatory", mandatory);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }
}
