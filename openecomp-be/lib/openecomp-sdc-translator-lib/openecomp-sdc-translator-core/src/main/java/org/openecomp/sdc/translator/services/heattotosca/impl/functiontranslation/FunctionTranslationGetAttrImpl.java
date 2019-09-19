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

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import static org.openecomp.sdc.translator.services.heattotosca.ConfigConstants.TRANS_MAPPING_DELIMITER_CHAR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslation;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;

public class FunctionTranslationGetAttrImpl implements FunctionTranslation {

    private static List<Object> translateGetAttributeFunctionExpression(FunctionTranslator functionTranslator) {

        List<Object> attributeParamList = (List) functionTranslator.getFunctionValue();
        List<Object> toscaAttributeParamList = new ArrayList<>();

        Optional<String> targetResourceTranslatedId = Optional.empty();
        String targetResourceId = null;
        if (attributeParamList.get(0) instanceof String) {
            targetResourceId = (String) attributeParamList.get(0);
            targetResourceTranslatedId = handleResourceName(targetResourceId, functionTranslator.getHeatFileName(),
                    functionTranslator.getHeatOrchestrationTemplate(), functionTranslator.getContext());
        }
        if (!targetResourceTranslatedId.isPresent()) {
            //unsupported resource
            toscaAttributeParamList.add(functionTranslator.getUnsupportedResourcePrefix() + attributeParamList.get(0));
            return toscaAttributeParamList;
        }
        toscaAttributeParamList.add(targetResourceTranslatedId.get());
        Optional<List<Object>> toscaAttList = handleAttributeName(attributeParamList, functionTranslator);
        if (!toscaAttList.isPresent()) {
            //Unsupported attribute
            toscaAttributeParamList.clear();
            toscaAttributeParamList.add(functionTranslator.getUnsupportedAttributePrefix()
                    + attributeParamList.get(0) + "." + attributeParamList.get(1));
            return toscaAttributeParamList;
        }
        toscaAttributeParamList.addAll(toscaAttList.get());
        handleGetAttrConsolidationData(functionTranslator, targetResourceId, targetResourceTranslatedId.get(),
                toscaAttList.get());

        String resourceType = HeatToToscaUtil.getResourceType((String) attributeParamList.get(0), functionTranslator
                .getHeatOrchestrationTemplate(), functionTranslator.getHeatFileName());
        Optional<List<Object>> toscaIndexOrKey = handleAttributeIndexOrKey(functionTranslator, resourceType,
                attributeParamList);
        toscaIndexOrKey.ifPresent(toscaAttributeParamList::addAll);
        return toscaAttributeParamList;
    }

    private static void handleGetAttrConsolidationData(FunctionTranslator functionTranslator,
                                                       String targetResourceId,
                                                       String targetResourceTranslatedId,
                                                       List<Object> toscaAttList) {
        Optional<String> resourceTranslatedId;
        String resourceId = functionTranslator.getResourceId();
        String resourceTranslatedIdValue = null;
        if (resourceId != null) {
            resourceTranslatedId = handleResourceName(resourceId, functionTranslator.getHeatFileName(),
                    functionTranslator.getHeatOrchestrationTemplate(), functionTranslator.getContext());
            if (resourceTranslatedId.isPresent()) {
                resourceTranslatedIdValue = resourceTranslatedId.get();
                handleGetAttrOutConsolidationData(functionTranslator, targetResourceTranslatedId,
                        resourceTranslatedIdValue, toscaAttList);
            }
        }
        handleGetAttrInConsolidationData(functionTranslator, resourceTranslatedIdValue,
                targetResourceId, targetResourceTranslatedId, toscaAttList);
    }

    private static void handleGetAttrOutConsolidationData(FunctionTranslator functionTranslator,
                                                          String targetTranslatedResourceId,
                                                          String resourceTranslatedId,
                                                          List<Object> toscaAttList) {
        if (functionTranslator.getServiceTemplate() == null) {
            return;
        }

        String attName = (String) toscaAttList.get(0);
        ConsolidationDataUtil.updateNodeGetAttributeOut(functionTranslator, targetTranslatedResourceId,
                resourceTranslatedId, attName);

    }

    private static void handleGetAttrInConsolidationData(FunctionTranslator functionTranslator,
                                                         String resourceTranslatedId,
                                                         String targetResourceId,
                                                         String targetResourceTranslatedId,
                                                         List<Object> toscaAttList) {
        if (functionTranslator.getServiceTemplate() == null) {
            return;
        }
        String attName = (String) toscaAttList.get(0);
        if (Objects.nonNull(resourceTranslatedId)) {
            ConsolidationDataUtil.updateNodeGetAttributeIn(functionTranslator, resourceTranslatedId,
                    targetResourceId, targetResourceTranslatedId, attName);
        } else {
            ConsolidationDataUtil.updateOutputParamGetAttrIn(functionTranslator, targetResourceId,
                    targetResourceTranslatedId, functionTranslator.getPropertyName(), attName);
        }
    }

    private static Optional<List<Object>> handleAttributeIndexOrKey(FunctionTranslator functionTranslator,
                                                                    String resourceType,
                                                                    List<Object> attributeParamList) {

        List<Object> attributeIndexOrKey = new ArrayList<>();
        if (attributeParamList.size() < 3) {
            return Optional.empty();
        }

        for (int i = 2; i < attributeParamList.size(); i++) {
            if (isInteger(attributeParamList.get(i))) {
                attributeIndexOrKey.add(attributeParamList.get(i));
            } else if (attributeParamList.get(i) instanceof Map) {
                attributeIndexOrKey.add(getToscaAttributeValue(functionTranslator, attributeParamList.get(i)));
            } else {
                Object toscaAttributeName = resourceType == null ? null : functionTranslator.getContext()
                        .getElementMapping(resourceType, Constants.ATTR, getAttributeFullPath(attributeParamList, i));
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
            attributeFullPath.append(TRANS_MAPPING_DELIMITER_CHAR);
            attributeFullPath.append(attributeParamList.get(j));
        }
        return attributeFullPath.toString();
    }

    private static boolean isInteger(Object inputNumber) {
        if (inputNumber == null) {
            return false;
        }
        return StringUtils.isNumeric(String.valueOf(inputNumber));
    }

    private static Optional<String> handleResourceName(String resourceId, String heatFileName,
                                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                       TranslationContext context) {
        return ResourceTranslationBase
                .getResourceTranslatedId(heatFileName, heatOrchestrationTemplate, resourceId, context);
    }

    private static Optional<List<Object>> handleAttributeName(List<Object> attributeParamList,
                                                              FunctionTranslator functionTranslator) {
        String resourceId = (String) attributeParamList.get(0);
        Resource resource = HeatToToscaUtil.getResource(functionTranslator.getHeatOrchestrationTemplate(),
                resourceId, functionTranslator.getHeatFileName());
        if (attributeParamList.size() == 1) {
            return getResourceTranslatedAttributesList(resource, functionTranslator.getContext());
        }
        if (!(attributeParamList.get(1) instanceof String)) {
            return Optional.empty();
        }
        if (HeatToToscaUtil.isNestedResource(resource)) {
            return getNestedResourceTranslatedAttribute((String) attributeParamList.get(1));
        } else {
            return getResourceTranslatedAttribute(resource, (String) attributeParamList.get(1), functionTranslator
                    .getContext());
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
                                                                              TranslationContext context) {
        List<Object> translatedAttributes = new ArrayList<>();
        if (HeatToToscaUtil.isNestedResource(resource)) {
            Optional<String> nestedFile = HeatToToscaUtil.getNestedFile(resource);
            if (!nestedFile.isPresent()) {
                return Optional.empty();
            }
            HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
                    .yamlToObject(context.getFiles().getFileContentAsStream(nestedFile.get()), HeatOrchestrationTemplate.class);
            translatedAttributes.addAll(nestedHeatOrchestrationTemplate.getOutputs().keySet());
            return Optional.of(translatedAttributes);

        } else {
            Map<String, String> resourceMappingAttributes =
                    context.getElementMapping(resource.getType(), Constants.ATTR);
            if (resourceMappingAttributes == null) {
                return Optional.empty();
            }
            Set<String> mappingAttributes = new HashSet<>(new ArrayList<>(resourceMappingAttributes.values()));
            translatedAttributes.addAll(mappingAttributes);
            return Optional.of(translatedAttributes);
        }
    }

    private static Optional<List<Object>> getResourceTranslatedAttribute(Resource resource,
                                                                         String attributeName,
                                                                         TranslationContext context) {
        List<Object> translatedAttributesList = new ArrayList<>();
        String translatedAttribute = context.getElementMapping(resource.getType(), Constants.ATTR, attributeName);
        if (translatedAttribute != null) {
            translatedAttributesList.add(translatedAttribute);
            return Optional.of(translatedAttributesList);
        } else {   //unsupported attribute
            return Optional.empty();
        }
    }

    private static Object getToscaAttributeValue(FunctionTranslator functionTranslator,
                                                 Object attributeVal) {
        if (attributeVal instanceof Map && !((Map) attributeVal).isEmpty()) {
            Map.Entry<String, Object> functionMapEntry =
                    (Map.Entry<String, Object>) ((Map) attributeVal).entrySet().iterator().next();
            Optional<FunctionTranslation> functionTranslationInstance =
                    FunctionTranslationFactory.getInstance(functionMapEntry.getKey());
            if (functionTranslationInstance.isPresent()) {
                functionTranslator.setFunctionValue(functionMapEntry.getValue());
                return functionTranslationInstance.get().translateFunction(functionTranslator);
            }
            Map<String, Object> attrValueMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) attributeVal).entrySet()) {
                attrValueMap.put(entry.getKey(), getToscaAttributeValue(functionTranslator, entry.getValue()));
            }
            return attrValueMap;
        } else if (attributeVal instanceof List && !((List) attributeVal).isEmpty()) {
            List<Object> propertyValueArray = new ArrayList<>();
            for (int i = 0; i < ((List) attributeVal).size(); i++) {
                propertyValueArray.add(getToscaAttributeValue(functionTranslator, ((List) attributeVal).get(i)));
            }
            return propertyValueArray;
        }
        return attributeVal;
    }

    @Override
    public Object translateFunction(FunctionTranslator functionTranslator) {
        Object returnValue;
        List<Object> attributeFunctionExpression = translateGetAttributeFunctionExpression(functionTranslator);
        if (functionTranslator.isResourceSupported(attributeFunctionExpression.get(0).toString())
                && functionTranslator.isAttributeSupported(attributeFunctionExpression.get(0).toString())) {
            Map<String, Object> getAttrValue = new HashMap<>();
            getAttrValue.put(ToscaFunctions.GET_ATTRIBUTE.getDisplayName(), attributeFunctionExpression);
            returnValue = getAttrValue;
        } else {
            returnValue = attributeFunctionExpression;
        }
        return returnValue;
    }
}
