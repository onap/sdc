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
package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import static org.openecomp.sdc.heat.services.HeatConstants.RESOURCE_DEF_TYPE_PROPERTY_NAME;
import static org.openecomp.sdc.heat.services.HeatConstants.RESOURCE_GROUP_INDEX_VAR_DEFAULT_VALUE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.InvalidPropertyValueErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

public class ResourceTranslationResourceGroupImpl extends ResourceTranslationBase {

    private static final String NESTED_RESOURCE_METADATA = "metadata";

    @Override
    protected void translate(TranslateTo translateTo) {
        final String heatFileName = translateTo.getHeatFileName();
        Object resourceDef = translateTo.getResource().getProperties().get(HeatConstants.RESOURCE_DEF_PROPERTY_NAME);
        Resource nestedResource = new Resource();
        Object typeDefinition = ((Map) resourceDef).get(RESOURCE_DEF_TYPE_PROPERTY_NAME);
        if (!(typeDefinition instanceof String)) {
            logger.warn("Resource '{}' of type '{}' with resourceDef which is not pointing to nested heat file is not"
                    + " supported and will be ignored in the translation ", translateTo.getResourceId(),
                HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource());
            return;
        }
        String type = (String) typeDefinition;
        if (!HeatToToscaUtil.isYmlFileType(type)) {
            logger.warn("Resource '{}' of type '{}' with resourceDef which is not pointing to nested heat file is not"
                    + " supported and will be ignored in the translation ", translateTo.getResourceId(),
                HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource());
            return;
        }
        nestedResource.setType(type);
        nestedResource.setProperties((Map<String, Object>) ((Map) resourceDef).get(HeatConstants.RESOURCE_DEF_PROPERTIES));
        nestedResource.setMetadata(((Map) resourceDef).get(NESTED_RESOURCE_METADATA));
        Optional<String> substitutionNodeTemplateId = ResourceTranslationFactory.getInstance(nestedResource)
            .translateResource(heatFileName, translateTo.getServiceTemplate(), translateTo.getHeatOrchestrationTemplate(), nestedResource,
                translateTo.getResourceId(), translateTo.getContext());
        substitutionNodeTemplateId.ifPresent(nodeTemplateId -> addSubstitutionNodeTemplate(translateTo, nodeTemplateId));
    }

    private void addSubstitutionNodeTemplate(TranslateTo translateTo, String substitutionNodeTemplateId) {
        NodeTemplate substitutionNodeTemplate = DataModelUtil.getNodeTemplate(translateTo.getServiceTemplate(), substitutionNodeTemplateId);
        if (Objects.isNull(substitutionNodeTemplate)) {
            return;
        }
        Map<String, Object> serviceTemplateFilter = (Map<String, Object>) substitutionNodeTemplate.getProperties()
            .get(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
        populateServiceTemplateFilterProperties(translateTo, substitutionNodeTemplate, serviceTemplateFilter);
        handlingIndexVar(translateTo, substitutionNodeTemplate);
        DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), substitutionNodeTemplateId, substitutionNodeTemplate);
    }

    private void handlingIndexVar(TranslateTo translateTo, NodeTemplate substitutionNodeTemplate) {
        List<String> indexVarProperties = new ArrayList<>();
        String indexVarValue = getIndexVarValue(translateTo);
        replacePropertiesIndexVarValue(indexVarValue, substitutionNodeTemplate.getProperties(), indexVarProperties, translateTo);
        //Add index var properties to context for unified model later
        translateTo.getContext()
            .addIndexVarProperties(ToscaUtil.getServiceTemplateFileName(translateTo.getServiceTemplate()), translateTo.getTranslatedId(),
                indexVarProperties);
    }

    private Map<String, List<String>> getNewIndexVarValue() {
        final Map<String, List<String>> newIndexVarValue = new HashMap<>();
        List<String> indexVarValList = new ArrayList<>();
        indexVarValList.add(ToscaConstants.MODELABLE_ENTITY_NAME_SELF);
        indexVarValList.add(ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
        indexVarValList.add(ToscaConstants.INDEX_VALUE_PROPERTY_NAME);
        newIndexVarValue.put(ToscaFunctions.GET_PROPERTY.getFunctionName(), indexVarValList);
        return newIndexVarValue;
    }

    private void replacePropertiesIndexVarValue(String indexVarValue, Map<String, Object> properties, List<String> indexVarProperties,
                                                TranslateTo translateTo) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
            Object propertyValue = propertyEntry.getValue();
            if (propertyValue != null && propertyValue.equals(RESOURCE_GROUP_INDEX_VAR_DEFAULT_VALUE)) {
                indexVarProperties.add(propertyEntry.getKey());
            }
            Object newPropertyValue = getUpdatedPropertyValueWithIndex(indexVarValue, propertyValue, indexVarProperties, translateTo);
            if (newPropertyValue != null) {
                properties.put(propertyEntry.getKey(), newPropertyValue);
            }
        }
    }

    private Object getUpdatedPropertyValueWithIndex(String indexVarValue, Object propertyValue, List<String> indexVarProperties,
                                                    TranslateTo translateTo) {
        if (propertyValue instanceof String) {
            return handleStringPropertyValueWithIndex(indexVarValue, propertyValue);
        } else if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
            return handleMapPropertyValueWithIndex(indexVarValue, propertyValue, indexVarProperties, translateTo);
        } else if (propertyValue instanceof List && !((List) propertyValue).isEmpty()) {
            return handleListPropertyValueWithIndex(indexVarValue, (List) propertyValue, indexVarProperties, translateTo);
        }
        return propertyValue;
    }

    private Object handleListPropertyValueWithIndex(String indexVarValue, List propertyValue, List<String> indexVarProperties,
                                                    TranslateTo translateTo) {
        List<Object> newPropertyValueList = new ArrayList<>();
        for (Object entry : propertyValue) {
            newPropertyValueList.add(getUpdatedPropertyValueWithIndex(indexVarValue, entry, indexVarProperties, translateTo));
        }
        return newPropertyValueList;
    }

    private Object handleMapPropertyValueWithIndex(String indexVarValue, Object propertyValue, List<String> indexVarProperties,
                                                   TranslateTo translateTo) {
        replacePropertiesIndexVarValue(indexVarValue, (Map<String, Object>) propertyValue, indexVarProperties, translateTo);
        return propertyValue;
    }

    private Object handleStringPropertyValueWithIndex(String indexVarValue, Object propertyValue) {
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
            concatMap.put(ToscaFunctions.CONCAT.getFunctionName(), concatList);
            return concatMap;
        }
        return propertyValue; //no update is needed
    }

    private String getIndexVarValue(TranslateTo translateTo) {
        Object indexVar = translateTo.getResource().getProperties().get(HeatConstants.INDEX_PROPERTY_NAME);
        if (indexVar == null) {
            return HeatConstants.RESOURCE_GROUP_INDEX_VAR_DEFAULT_VALUE;
        }
        if (indexVar instanceof String) {
            return (String) indexVar;
        }
        throw new CoreException(new InvalidPropertyValueErrorBuilder(HeatConstants.INDEX_PROPERTY_NAME, indexVar.toString(), "String").build());
    }

    private void populateServiceTemplateFilterProperties(TranslateTo translateTo, NodeTemplate substitutionNodeTemplate,
                                                         Map<String, Object> serviceTemplateFilter) {
        boolean mandatory = false;
        Object countValue = TranslatorHeatToToscaPropertyConverter
            .getToscaPropertyValue(translateTo.getServiceTemplate(), translateTo.getResourceId(), ToscaConstants.COUNT_PROPERTY_NAME,
                translateTo.getResource().getProperties().get(ToscaConstants.COUNT_PROPERTY_NAME), null, translateTo.getHeatFileName(),
                translateTo.getHeatOrchestrationTemplate(), substitutionNodeTemplate, translateTo.getContext());
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
        serviceTemplateFilter.put(ToscaConstants.MANDATORY_PROPERTY_NAME, mandatory);
    }
}
