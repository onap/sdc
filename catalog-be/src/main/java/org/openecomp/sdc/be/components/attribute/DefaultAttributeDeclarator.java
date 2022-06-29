/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.components.attribute;

import static org.openecomp.sdc.common.api.Constants.GET_ATTRIBUTE;

import com.google.gson.Gson;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetOutputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertiesOwner;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceAttribOutput;
import org.openecomp.sdc.be.model.IComponentInstanceConnectedElement;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.yaml.snakeyaml.Yaml;

public abstract class DefaultAttributeDeclarator<PROPERTYOWNER extends PropertiesOwner, ATTRIBUTETYPE extends AttributeDataDefinition> implements
    AttributeDeclarator {

    private static final Logger log = Logger.getLogger(DefaultAttributeDeclarator.class);
    private static final String UNDERSCORE = "_";
    private final Gson gson = new Gson();

    protected DefaultAttributeDeclarator() {
    }

    @Override
    public Either<List<OutputDefinition>, StorageOperationStatus> declareAttributesAsOutputs(final Component component,
                                                                                             final String propertiesOwnerId,
                                                                                             final List<ComponentInstanceAttribOutput> attribsToDeclare) {
        log.debug("#declarePropertiesAsInputs - declaring properties as inputs for component {} from properties owner {}", component.getUniqueId(),
            propertiesOwnerId);
        return resolvePropertiesOwner(component, propertiesOwnerId)
            .map(propertyOwner -> declareAttributesAsOutputs(component, propertyOwner, attribsToDeclare))
            .orElse(Either.right(onPropertiesOwnerNotFound(component.getUniqueId(), propertiesOwnerId)));
    }

    protected abstract ATTRIBUTETYPE createDeclaredAttribute(final AttributeDataDefinition attributeDataDefinition);

    protected abstract Either<?, StorageOperationStatus> updateAttributesValues(final Component component, final String propertiesOwnerId,
                                                                                final List<ATTRIBUTETYPE> attributetypeList);

    protected abstract Optional<PROPERTYOWNER> resolvePropertiesOwner(final Component component, final String propertiesOwnerId);

    private StorageOperationStatus onPropertiesOwnerNotFound(final String componentId, final String propertiesOwnerId) {
        log.debug("#declarePropertiesAsInputs - properties owner {} was not found on component {}", propertiesOwnerId, componentId);
        return StorageOperationStatus.NOT_FOUND;
    }

    private Either<List<OutputDefinition>, StorageOperationStatus> declareAttributesAsOutputs(final Component component,
                                                                                              final PROPERTYOWNER propertiesOwner,
                                                                                              final List<ComponentInstanceAttribOutput> attributesToDeclare) {
        final AttributesDeclarationData attributesDeclarationData = createOutputsAndOverrideAttributesValues(component, propertiesOwner,
            attributesToDeclare);
        return updateAttributesValues(component, propertiesOwner.getUniqueId(), attributesDeclarationData.getAttributesToUpdate()).left()
            .map(updatePropsRes -> attributesDeclarationData.getOutputsToCreate());
    }

    private AttributesDeclarationData createOutputsAndOverrideAttributesValues(final Component component, final PROPERTYOWNER propertiesOwner,
                                                                               final List<ComponentInstanceAttribOutput> attributesToDeclare) {
        final List<ATTRIBUTETYPE> declaredAttributes = new ArrayList<>();
        final List<OutputDefinition> createdInputs = attributesToDeclare.stream()
            .map(attributeOutput -> declareAttributeOutput(component, propertiesOwner, declaredAttributes, attributeOutput))
            .collect(Collectors.toList());
        return new AttributesDeclarationData(createdInputs, declaredAttributes);
    }

    private OutputDefinition declareAttributeOutput(final Component component, final PROPERTYOWNER propertiesOwner,
                                                    final List<ATTRIBUTETYPE> declaredAttributes, final ComponentInstanceAttribOutput attribOutput) {
        final AttributeDataDefinition attribute = resolveAttribute(declaredAttributes, attribOutput);
        final OutputDefinition outputDefinition = createOutput(component, propertiesOwner, attribOutput, attribute);
        final ATTRIBUTETYPE declaredAttribute = createDeclaredAttribute(attribute);
        if (!declaredAttributes.contains(declaredAttribute)) {
            declaredAttributes.add(declaredAttribute);
        }
        return outputDefinition;
    }

    private OutputDefinition createOutput(final Component component, final PROPERTYOWNER propertiesOwner,
                                          final ComponentInstanceAttribOutput attribOutput, final AttributeDataDefinition attributeDataDefinition) {
        String generatedInputPrefix = propertiesOwner.getNormalizedName();
        if (propertiesOwner.getUniqueId().equals(attribOutput.getParentUniqueId())) {
            //Creating input from property create on self using add property..Do not add the prefix
            generatedInputPrefix = null;
        }
        final String generatedOutputName = generateOutputName(generatedInputPrefix, attribOutput);
        log.debug("createInput: propOwner.uniqueId={}, attribOutput.parentUniqueId={}", propertiesOwner.getUniqueId(),
            attribOutput.getParentUniqueId());
        return createOutputFromAttribute(component.getUniqueId(), propertiesOwner, generatedOutputName, attribOutput, attributeDataDefinition);
    }

    private String generateOutputName(final String outputName, final ComponentInstanceAttribOutput attribOutput) {
        final String declaredInputName;
        final String[] parsedPropNames = attribOutput.getParsedAttribNames();
        if (parsedPropNames != null) {
            declaredInputName = handleInputName(outputName, parsedPropNames);
        } else {
            final String[] propName = {attribOutput.getName()};
            declaredInputName = handleInputName(outputName, propName);
        }
        return declaredInputName;
    }

    private String handleInputName(final String outputName, final String[] parsedPropNames) {
        final StringBuilder prefix = new StringBuilder();
        int startingIndex;
        if (Objects.isNull(outputName)) {
            prefix.append(parsedPropNames[0]);
            startingIndex = 1;
        } else {
            prefix.append(outputName);
            startingIndex = 0;
        }
        while (startingIndex < parsedPropNames.length) {
            prefix.append(UNDERSCORE);
            prefix.append(parsedPropNames[startingIndex]);
            startingIndex++;
        }
        return prefix.toString();
    }

    private AttributeDataDefinition resolveAttribute(final List<ATTRIBUTETYPE> attributesToCreate, final ComponentInstanceAttribOutput attribOutput) {
        final Optional<ATTRIBUTETYPE> resolvedAttribute = attributesToCreate.stream().filter(p -> p.getName().equals(attribOutput.getName()))
            .findFirst();
        return resolvedAttribute.isPresent() ? resolvedAttribute.get() : attribOutput;
    }

    OutputDefinition createOutputFromAttribute(final String componentId, final PROPERTYOWNER propertiesOwner, final String outputName,
                                               final ComponentInstanceAttribOutput attributeOutput, final AttributeDataDefinition attribute) {
        final String attributesName = attributeOutput.getAttributesName();
        final AttributeDefinition selectedAttrib = attributeOutput.getOutput();
        final String[] parsedAttribNames = attributeOutput.getParsedAttribNames();
        OutputDefinition outputDefinition;
        boolean complexProperty = false;
        if (attributesName != null && !attributesName.isEmpty() && selectedAttrib != null) {
            complexProperty = true;
            outputDefinition = new OutputDefinition(selectedAttrib);
            outputDefinition.setDefaultValue(selectedAttrib.getValue());
        } else {
            outputDefinition = new OutputDefinition(attribute);
            outputDefinition.setDefaultValue(attribute.getValue());
        }
        outputDefinition.setName(outputName);
        outputDefinition.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(componentId, outputDefinition.getName()));
        outputDefinition.setOutputPath(attributesName);
        outputDefinition.setInstanceUniqueId(propertiesOwner.getUniqueId());
        outputDefinition.setAttributeId(attributeOutput.getUniqueId());
        outputDefinition.setAttribute((attributeOutput));
        if (attribute instanceof IComponentInstanceConnectedElement) {
            ((IComponentInstanceConnectedElement) attribute).setComponentInstanceId(propertiesOwner.getUniqueId());
            ((IComponentInstanceConnectedElement) attribute).setComponentInstanceName(propertiesOwner.getName());
        }
        changeOutputValueToGetAttributeValue(outputName, parsedAttribNames, outputDefinition, attribute, complexProperty);
        return outputDefinition;
    }

    private void changeOutputValueToGetAttributeValue(final String outputName, final String[] parsedPropNames, final OutputDefinition output,
                                                      final AttributeDataDefinition attributeDataDefinition, final boolean complexProperty) {
        JSONObject jsonObject = new JSONObject();
        final String value = attributeDataDefinition.getValue();
        if (StringUtils.isEmpty(value)) {
            if (complexProperty) {
                jsonObject = createJSONValueForProperty(parsedPropNames.length - 1, parsedPropNames, jsonObject, outputName);
                attributeDataDefinition.setValue(jsonObject.toJSONString());
            } else {
                jsonObject
                    .put(GET_ATTRIBUTE, Arrays.asList(output.getAttribute().getComponentInstanceName(), attributeDataDefinition.getName()));
                output.setValue(jsonObject.toJSONString());
            }
        } else {
            final Object objValue = new Yaml().load(value);
            if (objValue instanceof Map || objValue instanceof List) {
                if (!complexProperty) {
                    jsonObject.put(GET_ATTRIBUTE,
                        Arrays.asList(output.getAttribute().getComponentInstanceName(), attributeDataDefinition.getName()));
                    output.setValue(jsonObject.toJSONString());
                } else {
                    final Map<String, Object> mappedToscaTemplate = (Map<String, Object>) objValue;
                    createOutputValue(mappedToscaTemplate, 1, parsedPropNames, outputName);
                    output.setValue(gson.toJson(mappedToscaTemplate));
                }
            } else {
                jsonObject
                    .put(GET_ATTRIBUTE, Arrays.asList(output.getAttribute().getComponentInstanceName(), attributeDataDefinition.getName()));
                output.setValue(jsonObject.toJSONString());
            }
        }
        if (CollectionUtils.isEmpty(attributeDataDefinition.getGetOutputValues())) {
            attributeDataDefinition.setGetOutputValues(new ArrayList<>());
        }
        final List<GetOutputValueDataDefinition> getOutputValues = attributeDataDefinition.getGetOutputValues();
        final GetOutputValueDataDefinition getOutputValueDataDefinition = new GetOutputValueDataDefinition();
        getOutputValueDataDefinition.setOutputId(output.getUniqueId());
        getOutputValueDataDefinition.setOutputName(output.getName());
        getOutputValues.add(getOutputValueDataDefinition);
    }

    private JSONObject createJSONValueForProperty(int i, final String[] parsedPropNames, final JSONObject ooj, final String outputName) {
        while (i >= 1) {
            if (i == parsedPropNames.length - 1) {
                final JSONObject jobProp = new JSONObject();
                jobProp.put(GET_ATTRIBUTE, outputName);
                ooj.put(parsedPropNames[i], jobProp);
                i--;
                return createJSONValueForProperty(i, parsedPropNames, ooj, outputName);
            } else {
                final JSONObject res = new JSONObject();
                res.put(parsedPropNames[i], ooj);
                i--;
                return createJSONValueForProperty(i, parsedPropNames, res, outputName);
            }
        }
        return ooj;
    }

    private Map<String, Object> createOutputValue(final Map<String, Object> lhm1, int index, final String[] outputNames, final String outputName) {
        while (index < outputNames.length) {
            if (lhm1.containsKey(outputNames[index])) {
                final Object value = lhm1.get(outputNames[index]);
                if (value instanceof Map) {
                    if (index == outputNames.length - 1) {
                        return (Map<String, Object>) ((Map) value).put(GET_ATTRIBUTE, outputName);
                    } else {
                        return createOutputValue((Map) value, ++index, outputNames, outputName);
                    }
                } else {
                    final Map<String, Object> jobProp = new HashMap<>();
                    if (index == outputNames.length - 1) {
                        jobProp.put(GET_ATTRIBUTE, outputName);
                        lhm1.put(outputNames[index], jobProp);
                        return lhm1;
                    } else {
                        lhm1.put(outputNames[index], jobProp);
                        return createOutputValue(jobProp, ++index, outputNames, outputName);
                    }
                }
            } else {
                final Map<String, Object> jobProp = new HashMap<>();
                lhm1.put(outputNames[index], jobProp);
                if (index == outputNames.length - 1) {
                    jobProp.put(GET_ATTRIBUTE, outputName);
                    return jobProp;
                } else {
                    return createOutputValue(jobProp, ++index, outputNames, outputName);
                }
            }
        }
        return lhm1;
    }

    private void resetOutputName(final Map<String, Object> lhm1, final String outputName) {
        for (final Map.Entry<String, Object> entry : lhm1.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof String && ((String) value).equalsIgnoreCase(outputName) && GET_ATTRIBUTE.equals(key)) {
                lhm1.remove(key);
            } else if (value instanceof Map) {
                final Map<String, Object> subMap = (Map<String, Object>) value;
                resetOutputName(subMap, outputName);
            } else if (value instanceof List && ((List) value).contains(outputName) && GET_ATTRIBUTE.equals(key)) {
                lhm1.remove(key);
            }
        }
    }

    /*        Mutates the object
     *        Tail recurse -> traverse the tosca elements and remove nested empty map properties
     *        this only handles nested maps, other objects are left untouched (even a Set containing a map) since behaviour is unexpected
     *
     *        @param  toscaElement - expected map of tosca values
     *        @return mutated @param toscaElement , where empty maps are deleted , return null for empty map.
     **/
    private Object cleanEmptyNestedValuesInMap(final Object toscaElement, short loopProtectionLevel) {
        if (loopProtectionLevel <= 0 || !(toscaElement instanceof Map)) {
            return toscaElement;
        }
        Map<Object, Object> toscaMap = (Map<Object, Object>) toscaElement;
        if (MapUtils.isNotEmpty(toscaMap)) {
            Object ret;
            final Set<Object> keysToRemove = new HashSet<>();                                                                 // use different set to avoid ConcurrentModificationException
            for (final Object key : toscaMap.keySet()) {
                final Object value = toscaMap.get(key);
                ret = cleanEmptyNestedValuesInMap(value, --loopProtectionLevel);
                if (ret == null) {
                    keysToRemove.add(key);
                }
            }
            final Set<Object> keySet = toscaMap.keySet();
            if (CollectionUtils.isNotEmpty(keySet)) {
                keySet.removeAll(keysToRemove);
            }
            if (isEmptyNestedMap(toscaElement)) {
                return null;
            }
        } else {
            return null;
        }
        return toscaElement;
    }

    /**
     * @param element
     * @return true if map nested maps are all empty, ignores other collection objects
     */
    private boolean isEmptyNestedMap(final Object element) {
        boolean isEmpty = true;
        if (element != null) {
            if (element instanceof Map) {
                if (MapUtils.isNotEmpty((Map) element)) {
                    for (final Object key : ((Map) (element)).keySet()) {
                        Object value = ((Map) (element)).get(key);
                        isEmpty &= isEmptyNestedMap(value);
                    }
                }
            } else {
                isEmpty = false;
            }
        }
        return isEmpty;
    }

    private class AttributesDeclarationData {

        private final List<OutputDefinition> outputsToCreate;
        private final List<ATTRIBUTETYPE> attributesToUpdate;

        AttributesDeclarationData(final List<OutputDefinition> outputsToCreate, final List<ATTRIBUTETYPE> attributesToUpdate) {
            this.outputsToCreate = outputsToCreate;
            this.attributesToUpdate = attributesToUpdate;
        }

        List<OutputDefinition> getOutputsToCreate() {
            return outputsToCreate;
        }

        List<ATTRIBUTETYPE> getAttributesToUpdate() {
            return attributesToUpdate;
        }
    }
}
