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

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.Template;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.EntityConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.ConfigConstants;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslation;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.NameExtractor;
import org.openecomp.sdc.translator.services.heattotosca.helper.FunctionTranslationHelper;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FunctionTranslationGetAttrImpl implements FunctionTranslation {


  @Override
  public Object translateFunction(ServiceTemplate serviceTemplate,
                                  String resourceId, String propertyName, String functionKey,
                                  Object functionValue, String heatFileName,
                                  HeatOrchestrationTemplate heatOrchestrationTemplate,
                                  Template toscaTemplate, TranslationContext context) {
    Object returnValue = new HashMap<>();
    List<Object> attributeFunctionExpression =
        translateGetAttributeFunctionExpression(serviceTemplate, resourceId, functionValue,
            propertyName, heatFileName, heatOrchestrationTemplate, (NodeTemplate) toscaTemplate,
            context);
    if (FunctionTranslationHelper.isResourceSupported(attributeFunctionExpression.get(0).toString())
        && FunctionTranslationHelper.isAttributeSupported(attributeFunctionExpression.get(0)
        .toString())) {
      ((Map) returnValue)
          .put(ToscaFunctions.GET_ATTRIBUTE.getDisplayName(), attributeFunctionExpression);
    } else {
      returnValue = attributeFunctionExpression;
    }

    return returnValue;
  }

  private static List<Object> translateGetAttributeFunctionExpression(
      ServiceTemplate serviceTemplate,
      String resourceId,
      Object functionValue,
      String propertyName,
      String heatFileName,
      HeatOrchestrationTemplate heatOrchestrationTemplate,
      NodeTemplate nodeTemplate,
      TranslationContext context) {

    List<Object> attributeParamList = (List) functionValue;
    List<Object> toscaAttributeParamList = new ArrayList<>();

    Optional<String> targetResourceTranslatedId = Optional.empty();
    String targetResourceId = null;
    if( attributeParamList.get(0) instanceof String) {
      targetResourceId = (String) attributeParamList.get(0);
      targetResourceTranslatedId =
          handleResourceName(targetResourceId, heatFileName, heatOrchestrationTemplate,
              context);
    }
    if (!targetResourceTranslatedId.isPresent()) {
      //unsupported resource
      toscaAttributeParamList
          .add(
              FunctionTranslationHelper.getUnsupportedResourcePrefix() + attributeParamList.get(0));
      return toscaAttributeParamList;
    } else {
      toscaAttributeParamList.add(targetResourceTranslatedId.get());
    }

    Optional<List<Object>> toscaAttList =
        handleAttributeName(attributeParamList, heatOrchestrationTemplate, propertyName,
            heatFileName, serviceTemplate,
            context);
    if (!toscaAttList.isPresent()) {
      //Unsupported attribute
      toscaAttributeParamList.clear();
      toscaAttributeParamList
          .add(FunctionTranslationHelper.getUnsupportedAttributePrefix() + attributeParamList.get(0)
              + "." + attributeParamList.get(1));
      return toscaAttributeParamList;
    } else {
      toscaAttributeParamList.addAll(toscaAttList.get());

      handleGetAttrConsolidationData(serviceTemplate, resourceId, propertyName, heatFileName,
          heatOrchestrationTemplate, context, targetResourceId,
          targetResourceTranslatedId,
          toscaAttList.get());
    }

    Optional<List<Object>> toscaIndexOrKey = handleAttributeIndexOrKey(serviceTemplate,
        resourceId, propertyName, HeatToToscaUtil
            .getResourceType((String) attributeParamList.get(0), heatOrchestrationTemplate,
                heatFileName), attributeParamList, context, heatFileName,
        heatOrchestrationTemplate);
    toscaIndexOrKey.ifPresent(toscaAttributeParamList::addAll);

    return toscaAttributeParamList;
  }

  private static void handleGetAttrConsolidationData(
      ServiceTemplate serviceTemplate,
      String resourceId, String propertyName,
      String heatFileName,
      HeatOrchestrationTemplate heatOrchestrationTemplate,
      TranslationContext context,
      String targetResourceId,
      Optional<String> targetResourceTranslatedId,
      List<Object> toscaAttList) {

    Optional<String> resourceTranslatedId = Optional.empty();
    if (resourceId != null) {
      resourceTranslatedId =
          handleResourceName(resourceId, heatFileName, heatOrchestrationTemplate,
              context);
      resourceTranslatedId
          .ifPresent(resourceTranslatedIdValue -> handleGetAttrOutConsolidationData(serviceTemplate,
              propertyName,
              heatOrchestrationTemplate, context, resourceId, targetResourceTranslatedId.get(),
              resourceTranslatedIdValue, toscaAttList, heatFileName));
    }

    if (targetResourceTranslatedId.isPresent()) {
      handleGetAttrInConsolidationData(serviceTemplate, resourceId, resourceTranslatedId,
          propertyName, heatOrchestrationTemplate, context, targetResourceId,
          targetResourceTranslatedId.get(), toscaAttList, heatFileName);
    }
  }

  private static void handleGetAttrOutConsolidationData(
      ServiceTemplate serviceTemplate,
      String propertyName,
      HeatOrchestrationTemplate heatOrchestrationTemplate,
      TranslationContext context,
      String resourceId,
      String targetTranslatedResourceId,
      String resourceTranslatedId,
      List<Object> toscaAttList,
      String heatFileName) {
    if (serviceTemplate != null) {
      Optional<EntityConsolidationData> entityConsolidationData =
          getEntityConsolidationData(serviceTemplate, heatOrchestrationTemplate, context,
              resourceId, resourceTranslatedId, heatFileName);
      if (entityConsolidationData.isPresent()) {
        String attName = (String) toscaAttList.get(0);
        handleNodeGetAttrOut(targetTranslatedResourceId, propertyName, heatOrchestrationTemplate,
            context, resourceId, entityConsolidationData.get(), attName);
      }
    }
  }

  private static void handleGetAttrInConsolidationData(
      ServiceTemplate serviceTemplate,
      String resourceId,
      Optional<String> resourceTranslatedId,
      String propertyName,
      HeatOrchestrationTemplate heatOrchestrationTemplate,
      TranslationContext context,
      String targetResourceId,
      String targetResourceTranslatedId,
      List<Object> toscaAttList,
      String  heatFileName) {

    if (serviceTemplate != null) {
      Optional<EntityConsolidationData> entityConsolidationData =
          getEntityConsolidationData(serviceTemplate, heatOrchestrationTemplate, context,
              targetResourceId, targetResourceTranslatedId, heatFileName);
      if (entityConsolidationData.isPresent()) {
        String attName = (String) toscaAttList.get(0);
        if (resourceTranslatedId.isPresent()) {
          handleNodeGetAttrIn(resourceTranslatedId.get(), propertyName, heatOrchestrationTemplate,
              context,
              resourceId, entityConsolidationData.get(), attName);
        } else {
          ConsolidationDataUtil
              .updateOutputGetAttributeInConsolidationData(entityConsolidationData.get(),
                  propertyName, attName);
        }
      }
    }
  }

  private static void handleNodeGetAttrOut(String nodeTemplateId, String propertyName,
                                           HeatOrchestrationTemplate heatOrchestrationTemplate,
                                           TranslationContext context, String resourceId,
                                           EntityConsolidationData entityConsolidationData,
                                           String attName) {
    Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
    boolean isNestedResource = HeatToToscaUtil.isNestedResource(resource);
    String toscaPropertyName = propertyName;
    if (!isNestedResource) {
      toscaPropertyName = HeatToToscaUtil.getToscaPropertyName(context, resource
          .getType(), propertyName);
    }
    ConsolidationDataUtil
        .updateNodeGetAttributeOut(entityConsolidationData,
            nodeTemplateId, toscaPropertyName, attName);
  }

  private static void handleNodeGetAttrIn(String nodeTemplateId, String propertyName,
                                          HeatOrchestrationTemplate heatOrchestrationTemplate,
                                          TranslationContext context, String resourceId,
                                          EntityConsolidationData entityConsolidationData,
                                          String attName) {
    Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
    boolean isNestedResource = HeatToToscaUtil.isNestedResource(resource);
    String toscaPropertyName = propertyName;
    if (!isNestedResource) {
      toscaPropertyName = HeatToToscaUtil.getToscaPropertyName(context, resource
          .getType(), propertyName);
    }
    ConsolidationDataUtil
        .updateNodeGetAttributeIn(entityConsolidationData,
            nodeTemplateId, toscaPropertyName, attName);
  }

  private static Optional<EntityConsolidationData> getEntityConsolidationData(
      ServiceTemplate serviceTemplate,
      HeatOrchestrationTemplate heatOrchestrationTemplate,
      TranslationContext context,
      String resourceId,
      String resourceTranslatedId,
      String heatFileName) {
    Resource resource = heatOrchestrationTemplate.getResources().get(resourceId);
    if (ConsolidationDataUtil.isComputeResource(heatOrchestrationTemplate, resourceId)) {
      String resourceType = heatOrchestrationTemplate.getResources().get(resourceId).getType();
      NameExtractor nodeTypeNameExtractor =
          context.getNameExtractorImpl(resourceType);
      String computeType =
          nodeTypeNameExtractor.extractNodeTypeName(
              resource, resourceId, context.getTranslatedIds().get(heatFileName).get(resourceId));

      return Optional.of(
          ConsolidationDataUtil.getComputeTemplateConsolidationData(context, serviceTemplate,
              computeType, resourceId));
    } else if (ConsolidationDataUtil.isPortResource(heatOrchestrationTemplate, resourceId)) {
      return Optional.of(ConsolidationDataUtil
          .getPortTemplateConsolidationData(context, serviceTemplate, resourceId));
    } else if (HeatToToscaUtil.isNestedResource(resource)) {
      return Optional.ofNullable(ConsolidationDataUtil
          .getNestedTemplateConsolidationData(context, serviceTemplate, heatFileName, resourceId));
    }
    return Optional.empty();
  }

  private static Optional<List<Object>> handleAttributeIndexOrKey(
      ServiceTemplate serviceTemplate,
      String resourceId, String propertyName,
      String resourceType,
      List<Object> attributeParamList,
      TranslationContext context,
      String heatFileName,
      HeatOrchestrationTemplate heatOrchestrationTemplate) {

    List<Object> attributeIndexOrKey = new ArrayList<>();
    if (attributeParamList.size() < 3) {
      return Optional.empty();
    }

    Object attributeName = attributeParamList.get(1);
    for (int i = 2; i < attributeParamList.size(); i++) {

      if (isInteger(attributeParamList.get(i))) {
        attributeIndexOrKey.add(attributeParamList.get(i));
      } else if (attributeParamList.get(i) instanceof Map) {
        attributeIndexOrKey.add(getToscaAttributeValue(serviceTemplate, resourceId,
            propertyName, attributeParamList.get(i), resourceType, heatFileName,
            heatOrchestrationTemplate, null, context));

      } else {
        Object toscaAttributeName = resourceType == null ? null : context
            .getElementMapping(resourceType, Constants.ATTR,
                getAttributeFullPath(attributeParamList, i));
        if (toscaAttributeName == null) {
          toscaAttributeName = attributeParamList.get(i);
        }
        attributeIndexOrKey.add(toscaAttributeName);
      }
    }

    return Optional.of(attributeIndexOrKey);
  }

  private static String getAttributeFullPath(List<Object> attributeParamList, int attributeIndex) {
    if (attributeParamList.size() < 3) {
      return null;
    }
    StringBuilder attributeFullPath = new StringBuilder();
    attributeFullPath.append(attributeParamList.get(1));
    for (int j = 2; j <= attributeIndex; j++) {
      if (isInteger(attributeParamList.get(j))) {
        continue;
      }
      attributeFullPath.append(ConfigConstants.TRANS_MAPPING_DELIMITER_CHAR);
      attributeFullPath.append(attributeParamList.get(j));
    }
    return attributeFullPath.toString();
  }

  private static boolean isInteger(Object inputNumber) {
    if (inputNumber == null) {
      return false;
    }

    /*try {
      Integer.parseInt(String.valueOf(inputNumber));
      return true;
    } catch (NumberFormatException exception) {
      return false;
    }*/
    if(StringUtils.isNumeric(String.valueOf(inputNumber))){
      return true;
    } else {
      return false;
    }
  }

  private static Optional<String> handleResourceName(String resourceId, String heatFileName,
                                                     HeatOrchestrationTemplate
                                                         heatOrchestrationTemplate,
                                                     TranslationContext context) {
    return ResourceTranslationBase
        .getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, resourceId, context);
  }

  private static Optional<List<Object>> handleAttributeName(List<Object> attributeParamList,
                                                            HeatOrchestrationTemplate
                                                                heatOrchestrationTemplate,
                                                            String propertyName,
                                                            String heatFileName,
                                                            ServiceTemplate serviceTemplate,
                                                            TranslationContext context) {
    String resourceId = (String) attributeParamList.get(0);
    Resource resource =
        HeatToToscaUtil.getResource(heatOrchestrationTemplate, resourceId, heatFileName);

    if (attributeParamList.size() == 1) {
      return getResourceTranslatedAttributesList(resource, context);
    }

    if(!(attributeParamList.get(1) instanceof String)){
      //todo - once dynamic attr name will be supported the commented line will be support it in
      // the first translation phase.
//      Object toscaAttributeValue = getToscaAttributeValue(serviceTemplate, resourceId, propertyName,
//          attributeParamList.get(1), resource
//              .getType(), heatFileName, heatOrchestrationTemplate, null, context);
//      List<Object> dynamicAttrValue = new ArrayList<>();
//      dynamicAttrValue.add(toscaAttributeValue);
//      return Optional.of(dynamicAttrValue);
      return Optional.empty();
    }

    if (HeatToToscaUtil.isNestedResource(resource)) {
      return getNestedResourceTranslatedAttribute((String) attributeParamList.get(1));
    } else {
      return getResourceTranslatedAttribute(resource, (String) attributeParamList.get(1), context);
    }
  }

  private static Optional<List<Object>> getNestedResourceTranslatedAttribute(String attributeName) {
    List<Object> translatedAttributesList = new ArrayList<>();

    if (attributeName.startsWith(HeatConstants.GET_ATTR_FROM_RESOURCE_GROUP_PREFIX)) {
      String[] attributeSplit = attributeName.split("\\.");
      if (attributeSplit.length == 2) {
        translatedAttributesList.add(attributeSplit[1]);
      } else if (attributeSplit.length == 3) {
        translatedAttributesList.add(attributeSplit[2]);
        translatedAttributesList.add(Integer.valueOf(attributeSplit[1]));
      } else {
        return Optional.empty();
      }
    } else {
      translatedAttributesList.add(attributeName);
    }
    return Optional.of(translatedAttributesList);
  }

  private static Optional<List<Object>> getResourceTranslatedAttributesList(Resource resource,
                                                                            TranslationContext
                                                                                context) {
    List<Object> translatedAttributes = new ArrayList<>();
    if (HeatToToscaUtil.isNestedResource(resource)) {
      Optional<String> nestedFile = HeatToToscaUtil.getNestedFile(resource);
      if (!nestedFile.isPresent()) {
        return Optional.empty();
      }
      HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
          .yamlToObject(context.getFiles().getFileContent(nestedFile.get()),
              HeatOrchestrationTemplate.class);
      translatedAttributes.addAll(nestedHeatOrchestrationTemplate.getOutputs().keySet());
      return Optional.of(translatedAttributes);

    } else {
      Map<String, String> resourceMappingAttributes =
          context.getElementMapping(resource.getType(), Constants.ATTR);
      if (resourceMappingAttributes == null) {
        return Optional.empty();
      }
      Set<String> mappingAttributes = new HashSet<>();
      mappingAttributes
          .addAll(resourceMappingAttributes.values().stream().collect(Collectors.toList()));
      translatedAttributes.addAll(mappingAttributes);
      return Optional.of(translatedAttributes);
    }
  }

  private static Optional<List<Object>> getResourceTranslatedAttribute(Resource resource,
                                                                       String attributeName,
                                                                       TranslationContext context) {
    List<Object> translatedAttributesList = new ArrayList<>();
    String translatedAttribute =
        context.getElementMapping(resource.getType(), Constants.ATTR, attributeName);
    if (translatedAttribute != null) {
      translatedAttributesList.add(translatedAttribute);
      return Optional.of(translatedAttributesList);
    } else {   //unsupported attribute
      return Optional.empty();
    }
  }

  private static Object getToscaAttributeValue(ServiceTemplate serviceTemplate,
                                               String resourceId, String propertyName,
                                               Object attributeVal, String resourceType,
                                               String heatFileName,
                                               HeatOrchestrationTemplate heatOrchestrationTemplate,
                                               Template template, TranslationContext context) {
    if (attributeVal instanceof Map && !((Map) attributeVal).isEmpty()) {
      Map.Entry<String, Object> functionMapEntry =
          (Map.Entry<String, Object>) ((Map) attributeVal).entrySet().iterator().next();
      if (FunctionTranslationFactory.getInstance(functionMapEntry.getKey()).isPresent()) {
        return FunctionTranslationFactory.getInstance(functionMapEntry.getKey()).get()
            .translateFunction(serviceTemplate, resourceId, propertyName,
                functionMapEntry.getKey(), functionMapEntry
                    .getValue(), heatFileName, heatOrchestrationTemplate, template, context);
      }
      Map<String, Object> attrValueMap = new HashMap<>();
      for (Map.Entry<String, Object> entry : ((Map<String, Object>) attributeVal).entrySet()) {
        attrValueMap.put(entry.getKey(),
            getToscaAttributeValue(serviceTemplate, resourceId, propertyName, entry.getValue(),
                resourceType, heatFileName, heatOrchestrationTemplate, template, context));
      }
      return attrValueMap;
    } else if (attributeVal instanceof List && !((List) attributeVal).isEmpty()) {
      List propertyValueArray = new ArrayList<>();
      for (int i = 0; i < ((List) attributeVal).size(); i++) {
        propertyValueArray.add(
            getToscaAttributeValue(serviceTemplate, resourceId, propertyName,
                ((List) attributeVal).get(i), resourceType, heatFileName,
                heatOrchestrationTemplate, template, context));
      }
      return propertyValueArray;
    }
    return attributeVal;
  }


}
