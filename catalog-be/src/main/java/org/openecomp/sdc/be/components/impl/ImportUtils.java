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
package org.openecomp.sdc.be.components.impl;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openecomp.sdc.be.components.impl.ResourceImportManager.PROPERTY_NAME_PATTERN_IGNORE_LENGTH;
import static org.openecomp.sdc.be.datatypes.elements.Annotation.setAnnotationsName;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import fj.data.Either;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.heat.HeatParameterType;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintType;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

@Component
public final class ImportUtils {

    private static final CustomResolver customResolver = new CustomResolver();
    private static final Yaml strictYamlLoader = new YamlLoader().getStrictYamlLoader();
    private static final Logger log = Logger.getLogger(ImportUtils.class);
    private static ComponentsUtils componentsUtils;

    private ImportUtils() {
    }

    @Autowired
    public static void setComponentsUtils(ComponentsUtils cu) {
        componentsUtils = cu;
    }

    private static void buildMap(Map<String, Object> output, Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String, Object> result = new LinkedHashMap<>();
                buildMap(result, (Map) value);
                output.put(key, result);
            } else if (value instanceof Collection) {
                Map<String, Object> result = new LinkedHashMap<>();
                int i = 0;
                for (Object item : (Collection<Object>) value) {
                    buildMap(result, Collections.singletonMap("[" + (i++) + "]", item));
                }
                output.put(key, new ArrayList<>(result.values()));
            } else {
                output.put(key, value);
            }
        }
    }

    public static Map<String, Object> loadYamlAsStrictMap(String content) {
        Map<String, Object> result = new LinkedHashMap<>();
        Object map = strictYamlLoader.load(content);
        buildMap(result, (Map<String, Object>) map);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Either<List<HeatParameterDefinition>, ResultStatusEnum> getHeatParamsWithoutImplicitTypes(String heatDecodedPayload,
                                                                                                            String artifactType) {
        Map<String, Object> heatData = (Map<String, Object>) new Yaml(new Constructor(), new Representer(), new DumperOptions(), customResolver)
            .load(heatDecodedPayload);
        return getHeatParameters(heatData, artifactType);
    }

    @SuppressWarnings("unchecked")
    private static void handleElementNameNotFound(String elementName, Object elementValue, ToscaElementTypeEnum elementType,
                                                  List<Object> returnedList) {
        if (elementValue instanceof Map) {
            findToscaElements((Map<String, Object>) elementValue, elementName, elementType, returnedList);
        } else if (elementValue instanceof List) {
            findAllToscaElementsInList((List<Object>) elementValue, elementName, elementType, returnedList);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addFoundElementAccordingToItsType(String elementName, ToscaElementTypeEnum elementType, List<Object> returnedList,
                                                          Object elementValue) {
        if (elementValue instanceof Boolean) {
            if (elementType == ToscaElementTypeEnum.BOOLEAN || elementType == ToscaElementTypeEnum.ALL) {
                returnedList.add(elementValue);
            }
        } else if (elementValue instanceof String) {
            if (elementType == ToscaElementTypeEnum.STRING || elementType == ToscaElementTypeEnum.ALL) {
                returnedList.add(elementValue);
            }
        } else if (elementValue instanceof Map) {
            if (elementType == ToscaElementTypeEnum.MAP || elementType == ToscaElementTypeEnum.ALL) {
                returnedList.add(elementValue);
            }
            findToscaElements((Map<String, Object>) elementValue, elementName, elementType, returnedList);
        } else if (elementValue instanceof List) {
            if (elementType == ToscaElementTypeEnum.LIST || elementType == ToscaElementTypeEnum.ALL) {
                returnedList.add(elementValue);
            }
            findAllToscaElementsInList((List<Object>) elementValue, elementName, elementType, returnedList);
        }
        // For Integer, Double etc...
        else if (elementType == ToscaElementTypeEnum.ALL && elementValue != null) {
            returnedList.add(String.valueOf(elementValue));
        }
    }

    private static void findAllToscaElementsInList(List<Object> list, String elementName, ToscaElementTypeEnum elementType,
                                                   List<Object> returnedList) {
        list.forEach(elementValue -> handleElementNameNotFound(elementName, elementValue, elementType, returnedList));
    }

    public static Either<Object, ResultStatusEnum> findToscaElement(Map<String, Object> toscaJson, TypeUtils.ToscaTagNamesEnum elementName,
                                                                    ToscaElementTypeEnum elementType) {
        List<Object> foundElements = new ArrayList<>();
        findToscaElements(toscaJson, elementName.getElementName(), elementType, foundElements);
        if (!isEmpty(foundElements)) {
            return Either.left(foundElements.get(0));
        }
        return Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
    }

    /**
     * Recursively searches for all tosca elements with key equals to elementName and value equals to elementType. <br> Returns Either element
     * with:<br> List with all value if values found<br> Or ELEMENT_NOT_FOUND ActionStatus
     *
     * @param toscaJson
     * @return
     */
    public static Either<List<Object>, ResultStatusEnum> findToscaElements(Map<String, Object> toscaJson, String elementName,
                                                                           ToscaElementTypeEnum elementType, List<Object> returnedList) {
        Either<List<Object>, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
        String skipKey = null;
        if (toscaJson.containsKey(elementName)) {
            skipKey = handleFoundElement(toscaJson, elementName, elementType, returnedList);
        }
        Iterator<Entry<String, Object>> keyValItr = toscaJson.entrySet().iterator();
        while (keyValItr.hasNext()) {
            Entry<String, Object> keyValEntry = keyValItr.next();
            if (!String.valueOf(keyValEntry.getKey()).equals(skipKey)) {
                handleElementNameNotFound(elementName, keyValEntry.getValue(), elementType, returnedList);
            }
        }
        if (!isEmpty(returnedList)) {
            returnedElement = Either.left(returnedList);
        }
        return returnedElement;
    }

    private static String handleFoundElement(Map<String, Object> toscaJson, String elementName, ToscaElementTypeEnum elementType,
                                             List<Object> returnedList) {
        Object elementValue = toscaJson.get(elementName);
        addFoundElementAccordingToItsType(elementName, elementType, returnedList, elementValue);
        return elementName;
    }

    @SuppressWarnings("unchecked")
    public static <T> Either<List<T>, ResultStatusEnum> findFirstToscaListElement(Map<String, Object> toscaJson,
                                                                                  TypeUtils.ToscaTagNamesEnum toscaTagName) {
        Either<List<T>, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
        Either<Object, ResultStatusEnum> findFirstToscaElement = findToscaElement(toscaJson, toscaTagName, ToscaElementTypeEnum.LIST);
        if (findFirstToscaElement.isLeft()) {
            returnedElement = Either.left((List<T>) findFirstToscaElement.left().value());
        }
        return returnedElement;
    }

    @SuppressWarnings("unchecked")
    public static <T> Either<Map<String, T>, ResultStatusEnum> findFirstToscaMapElement(Map<String, Object> toscaJson,
                                                                                        TypeUtils.ToscaTagNamesEnum toscaTagName) {
        Either<Map<String, T>, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
        Either<Object, ResultStatusEnum> findFirstToscaElement = findToscaElement(toscaJson, toscaTagName, ToscaElementTypeEnum.MAP);
        if (findFirstToscaElement.isLeft()) {
            returnedElement = Either.left((Map<String, T>) findFirstToscaElement.left().value());
        }
        return returnedElement;
    }

    public static Either<String, ResultStatusEnum> findFirstToscaStringElement(Map<String, Object> toscaJson,
                                                                               TypeUtils.ToscaTagNamesEnum toscaTagName) {
        Either<String, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
        Either<Object, ResultStatusEnum> findFirstToscaElements = findToscaElement(toscaJson, toscaTagName, ToscaElementTypeEnum.STRING);
        if (findFirstToscaElements.isLeft()) {
            returnedElement = Either.left((String) findFirstToscaElements.left().value());
        }
        return returnedElement;
    }

    /**
     * searches for first Tosca in Json map (toscaJson) boolean element by name (toscaTagName) returns found element or ELEMENT_NOT_FOUND status
     *
     * @param toscaJson
     * @param toscaTagName
     * @return
     */
    private static Either<String, ResultStatusEnum> findFirstToscaBooleanElement(Map<String, Object> toscaJson,
                                                                                 TypeUtils.ToscaTagNamesEnum toscaTagName) {
        Either<String, ResultStatusEnum> returnedElement = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
        Either<Object, ResultStatusEnum> findFirstToscaElements = findToscaElement(toscaJson, toscaTagName, ToscaElementTypeEnum.BOOLEAN);
        if (findFirstToscaElements.isLeft()) {
            returnedElement = Either.left(String.valueOf(findFirstToscaElements.left().value()));
        }
        return returnedElement;
    }

    private static void setPropertyConstraints(Map<String, Object> propertyValue, PropertyDefinition property) {
        List<PropertyConstraint> constraints = getPropertyConstraints(propertyValue, property.getType());
        if (CollectionUtils.isNotEmpty(constraints)) {
            property.setConstraints(constraints);
        }
    }

    private static List<PropertyConstraint> getPropertyConstraints(final Map<String, Object> propertyValue, final String propertyType) {
        final List<Object> propertyFieldConstraints = findCurrentLevelConstraintsElement(propertyValue);
        if (CollectionUtils.isEmpty(propertyFieldConstraints)) {
            return Collections.emptyList();
        }
        final List<PropertyConstraint> constraintList = new ArrayList<>();
        final Type constraintType = new TypeToken<PropertyConstraint>() {
        }.getType();
        final Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintDeserialiser()).create();
        for (final Object constraintJson : propertyFieldConstraints) {
            final PropertyConstraint propertyConstraint = validateAndGetPropertyConstraint(propertyType, constraintType, gson, constraintJson);
            constraintList.add(propertyConstraint);
        }
        return constraintList;
    }

    private static List<Object> findCurrentLevelConstraintsElement(Map<String, Object> toscaJson) {
        List<Object> constraints = null;
        if (toscaJson.containsKey(TypeUtils.ToscaTagNamesEnum.CONSTRAINTS.getElementName())) {
            try {
                constraints = (List<Object>) toscaJson.get(TypeUtils.ToscaTagNamesEnum.CONSTRAINTS.getElementName());
            } catch (ClassCastException e) {
                throw new ByActionStatusComponentException(ActionStatus.INVALID_PROPERTY_CONSTRAINTS_FORMAT,
                    toscaJson.get(TypeUtils.ToscaTagNamesEnum.CONSTRAINTS.getElementName()).toString());
            }
        }
        return constraints;
    }

    private static PropertyConstraint validateAndGetPropertyConstraint(String propertyType, Type constraintType, Gson gson, Object constraintJson) {
        PropertyConstraint propertyConstraint;
        try {
            propertyConstraint = gson.fromJson(gson.toJson(constraintJson), constraintType);
        } catch (ClassCastException | JsonParseException e) {
            throw new ByActionStatusComponentException(ActionStatus.INVALID_PROPERTY_CONSTRAINTS_FORMAT, constraintJson.toString());
        }
        if (propertyConstraint instanceof ValidValuesConstraint) {
            try {
                ((ValidValuesConstraint) propertyConstraint).validateType(propertyType);
            } catch (ConstraintValueDoNotMatchPropertyTypeException e) {
                BeEcompErrorManager.getInstance()
                    .logInternalFlowError("GetInitializedPropertyConstraint", e.getMessage(), BeEcompErrorManager.ErrorSeverity.ERROR);
                throw new ByActionStatusComponentException(ActionStatus.INVALID_PROPERTY_CONSTRAINTS, ConstraintType.VALID_VALUES.name(),
                    ((ValidValuesConstraint) propertyConstraint).getValidValues().toString(), propertyType);
            }
        }
        return propertyConstraint;
    }

    static PropertyDefinition createModuleProperty(Map<String, Object> propertyValue) {
        PropertyDefinition propertyDef = new PropertyDefinition();
        setField(propertyValue, TypeUtils.ToscaTagNamesEnum.TYPE, propertyDef::setType);
        setFieldBoolean(propertyValue, ToscaTagNamesEnum.REQUIRED, req -> propertyDef.setRequired(Boolean.parseBoolean(req)));
        setField(propertyValue, TypeUtils.ToscaTagNamesEnum.DESCRIPTION, propertyDef::setDescription);
        setJsonStringField(propertyValue, TypeUtils.ToscaTagNamesEnum.DEFAULT_VALUE, propertyDef.getType(), propertyDef::setDefaultValue);
        setJsonStringField(propertyValue, TypeUtils.ToscaTagNamesEnum.VALUE, propertyDef.getType(), propertyDef::setValue);
        setFieldBoolean(propertyValue, TypeUtils.ToscaTagNamesEnum.IS_PASSWORD, pass -> propertyDef.setPassword(Boolean.parseBoolean(pass)));
        setField(propertyValue, TypeUtils.ToscaTagNamesEnum.STATUS, propertyDef::setStatus);
        setSchema(propertyValue, propertyDef);
        setPropertyConstraints(propertyValue, propertyDef);
        return propertyDef;
    }

    private static void setJsonStringField(Map<String, Object> propertyValue, ToscaTagNamesEnum elementName, String type, Consumer<String> setter) {
        Either<Object, ResultStatusEnum> eitherValue = findToscaElement(propertyValue, elementName, ToscaElementTypeEnum.ALL);
        if (eitherValue.isLeft()) {
            setter.accept(getPropertyJsonStringValue(eitherValue.left().value(), type));
        }
    }

    private static Annotation createModuleAnnotation(Map<String, Object> annotationMap, AnnotationTypeOperations annotationTypeOperations) {
        String parsedAnnotationType = findFirstToscaStringElement(annotationMap, TypeUtils.ToscaTagNamesEnum.TYPE).left().value();
        AnnotationTypeDefinition annotationTypeObject = annotationTypeOperations.getLatestType(parsedAnnotationType);
        if (annotationTypeObject != null) {
            Annotation annotation = new Annotation();
            setField(annotationMap, TypeUtils.ToscaTagNamesEnum.TYPE, annotation::setType);
            setField(annotationMap, TypeUtils.ToscaTagNamesEnum.DESCRIPTION, annotation::setDescription);
            Either<Map<String, PropertyDefinition>, ResultStatusEnum> properties = getProperties(annotationMap);
            modifyPropertiesKeysToProperForm(properties, annotation);
            return annotation;
        }
        return null;
    }

    private static Either<Boolean, ResponseFormat> modifyPropertiesKeysToProperForm(
        Either<Map<String, PropertyDefinition>, ResultStatusEnum> properties, Annotation annotation) {
        Either<Boolean, ResponseFormat> result = Either.left(true);
        if (properties.isLeft()) {
            List<PropertyDataDefinition> propertiesList = new ArrayList<>();
            Map<String, PropertyDefinition> value = properties.left().value();
            if (value != null) {
                for (Entry<String, PropertyDefinition> entry : value.entrySet()) {
                    String name = entry.getKey();
                    if (!PROPERTY_NAME_PATTERN_IGNORE_LENGTH.matcher(name).matches()) {
                        log.debug("The property with invalid name {} occurred upon import resource {}. ", name, annotation.getName());
                        result = Either.right(componentsUtils.getResponseFormat(
                            componentsUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_NAME, JsonPresentationFields.PROPERTY)));
                    }
                    PropertyDefinition propertyDefinition = entry.getValue();
                    propertyDefinition.setValue(propertyDefinition.getName());
                    propertyDefinition.setName(name);
                    propertiesList.add(propertyDefinition);
                }
            }
            annotation.setProperties(propertiesList);
        } else if (properties.right().value() != ResultStatusEnum.ELEMENT_NOT_FOUND) {
            result = Either.right(componentsUtils
                .getResponseFormat(componentsUtils.convertFromResultStatusEnum(properties.right().value(), JsonPresentationFields.PROPERTY)));
        }
        return result;
    }

    private static InputDefinition createModuleInput(final Map<String, Object> inputValue, final AnnotationTypeOperations annotationTypeOperations) {
        return parseAnnotationsAndAddItToInput(createModuleInput(inputValue), inputValue, annotationTypeOperations);
    }

    private static InputDefinition createModuleInput(final Map<String, Object> inputValue) {
        final InputDefinition inputDef = new InputDefinition();
        setField(inputValue, TypeUtils.ToscaTagNamesEnum.TYPE, inputDef::setType);
        setFieldBoolean(inputValue, ToscaTagNamesEnum.REQUIRED, req -> inputDef.setRequired(Boolean.parseBoolean(req)));
        setField(inputValue, TypeUtils.ToscaTagNamesEnum.DESCRIPTION, inputDef::setDescription);
        setJsonStringField(inputValue, TypeUtils.ToscaTagNamesEnum.DEFAULT_VALUE, inputDef.getType(), inputDef::setDefaultValue);
        setFieldBoolean(inputValue, TypeUtils.ToscaTagNamesEnum.IS_PASSWORD, pass -> inputDef.setPassword(Boolean.parseBoolean(pass)));
        setField(inputValue, TypeUtils.ToscaTagNamesEnum.STATUS, inputDef::setStatus);
        setField(inputValue, TypeUtils.ToscaTagNamesEnum.LABEL, inputDef::setLabel);
        setFieldBoolean(inputValue, TypeUtils.ToscaTagNamesEnum.HIDDEN, hidden -> inputDef.setHidden(Boolean.parseBoolean(hidden)));
        setFieldBoolean(inputValue, TypeUtils.ToscaTagNamesEnum.IMMUTABLE, immutable -> inputDef.setImmutable(Boolean.parseBoolean(immutable)));
        setSchema(inputValue, inputDef);
        setPropertyConstraints(inputValue, inputDef);
        return inputDef;
    }

    private static OutputDefinition createModuleOutput(final Map<String, Object> outputValue) {
        final OutputDefinition outputDef = new OutputDefinition();
        setField(outputValue, TypeUtils.ToscaTagNamesEnum.TYPE, outputDef::setType);
        setField(outputValue, TypeUtils.ToscaTagNamesEnum.DESCRIPTION, outputDef::setDescription);
        setJsonStringField(outputValue, TypeUtils.ToscaTagNamesEnum.VALUE, outputDef.getType(), outputDef::setValue);
        setField(outputValue, TypeUtils.ToscaTagNamesEnum.STATUS, outputDef::setStatus);
        return outputDef;
    }

    private static InputDefinition parseAnnotationsAndAddItToInput(InputDefinition inputDef, Map<String, Object> inputValue,
                                                                   AnnotationTypeOperations annotationTypeOperations) {
        Function<String, Annotation> elementGenByName = ImportUtils::createAnnotation;
        Function<Map<String, Object>, Annotation> func = annotation -> createModuleAnnotation(annotation, annotationTypeOperations);
        return getElements(inputValue, TypeUtils.ToscaTagNamesEnum.ANNOTATIONS, elementGenByName, func).left()
            .map(annotations -> modifyInputWithAnnotations(inputDef, annotations)).left().on(err -> {
                log.error("Parsing annotations or adding them to the PropertyDataDefinition object failed");
                return inputDef;
            });
    }

    private static InputDefinition modifyInputWithAnnotations(InputDefinition inputDef, Map<String, Annotation> annotationsMap) {
        setAnnotationsName(annotationsMap);
        inputDef.setAnnotationsToInput(annotationsMap.values());
        return inputDef;
    }

    static AttributeDefinition createModuleAttribute(Map<String, Object> attributeMap) {
        AttributeDefinition attributeDef = new AttributeDefinition();
        setField(attributeMap, TypeUtils.ToscaTagNamesEnum.TYPE, attributeDef::setType);
        setField(attributeMap, TypeUtils.ToscaTagNamesEnum.DESCRIPTION, attributeDef::setDescription);
        setField(attributeMap, TypeUtils.ToscaTagNamesEnum.STATUS, attributeDef::setStatus);
        setJsonStringField(attributeMap, TypeUtils.ToscaTagNamesEnum.DEFAULT_VALUE, attributeDef.getType(), attributeDef::set_default);
        setEntrySchema(attributeMap, attributeDef);
        return attributeDef;
    }

    private static void setSchema(final Map<String, Object> propertyValue, final PropertyDefinition propertyDefinition) {
        final Either<Object, ResultStatusEnum> schemaElementRes = findEntrySchemaElement(propertyValue);
        if (schemaElementRes.isLeft()) {
            propertyDefinition.setSchema(getSchema(schemaElementRes.left().value()));
        }
    }

    private static void setEntrySchema(final Map<String, Object> toscaJsonMap, final AttributeDefinition attributeDefinition) {
        final Either<Object, ResultStatusEnum> schemaElementRes = findEntrySchemaElement(toscaJsonMap);
        if (schemaElementRes.isLeft()) {
            attributeDefinition.setEntry_schema(createEntrySchema(schemaElementRes.left().value()));
        }
    }

    private static Either<Object, ResultStatusEnum> findEntrySchemaElement(final Map<String, Object> propertyValue) {
        return findToscaElement(propertyValue, TypeUtils.ToscaTagNamesEnum.ENTRY_SCHEMA, ToscaElementTypeEnum.ALL);
    }

    private static SchemaDefinition getSchema(Object propertyFieldEntryScheme) {
        SchemaDefinition schema = new SchemaDefinition();
        if (propertyFieldEntryScheme instanceof String) {
            String schemaType = (String) propertyFieldEntryScheme;
            PropertyDefinition schemeProperty = new PropertyDefinition();
            schemeProperty.setType(schemaType);
            schema.setProperty(schemeProperty);
        } else if (propertyFieldEntryScheme instanceof Map) {
            PropertyDefinition schemeProperty = createModuleProperty((Map<String, Object>) propertyFieldEntryScheme);
            schema.setProperty(schemeProperty);
        }
        return schema;
    }

    private static EntrySchema createEntrySchema(final Object toscaEntrySchemaObj) {
        final EntrySchema entrySchema = new EntrySchema();
        if (toscaEntrySchemaObj instanceof String) {
            entrySchema.setType((String) toscaEntrySchemaObj);
        } else if (toscaEntrySchemaObj instanceof Map) {
            final PropertyDefinition schemeProperty = createModuleProperty((Map<String, Object>) toscaEntrySchemaObj);
            entrySchema.setType(schemeProperty.getType());
            entrySchema.setDescription(schemeProperty.getDescription());
        }
        return entrySchema;
    }

    private static void setField(Map<String, Object> toscaJson, TypeUtils.ToscaTagNamesEnum tagName, Consumer<String> setter) {
        Either<String, ResultStatusEnum> fieldStringValue = findFirstToscaStringElement(toscaJson, tagName);
        if (fieldStringValue.isLeft()) {
            setter.accept(fieldStringValue.left().value());
        }
    }

    private static void setFieldBoolean(Map<String, Object> toscaJson, TypeUtils.ToscaTagNamesEnum tagName, Consumer<String> setter) {
        Either<String, ResultStatusEnum> fieldStringValue = findFirstToscaBooleanElement(toscaJson, tagName);
        if (fieldStringValue.isLeft()) {
            setter.accept(fieldStringValue.left().value());
        }
    }

    public static Either<Map<String, PropertyDefinition>, ResultStatusEnum> getProperties(Map<String, Object> toscaJson) {
        Function<String, PropertyDefinition> elementGenByName = ImportUtils::createProperties;
        Function<Map<String, Object>, PropertyDefinition> func = ImportUtils::createModuleProperty;
        return getElements(toscaJson, TypeUtils.ToscaTagNamesEnum.PROPERTIES, elementGenByName, func);
    }

    public static Either<Map<String, AttributeDefinition>, ResultStatusEnum> getAttributes(final Map<String, Object> toscaJson) {
        final Function<String, AttributeDefinition> elementGenByName = ImportUtils::createAttribute;
        final Function<Map<String, Object>, AttributeDefinition> func = ImportUtils::createModuleAttribute;
        return getElements(toscaJson, ToscaTagNamesEnum.ATTRIBUTES, elementGenByName, func);
    }

    public static Either<Map<String, InputDefinition>, ResultStatusEnum> getInputs(Map<String, Object> toscaJson,
                                                                                   AnnotationTypeOperations annotationTypeOperations) {
        Function<String, InputDefinition> elementGenByName = ImportUtils::createInputs;
        Function<Map<String, Object>, InputDefinition> func = object -> createModuleInput(object, annotationTypeOperations);
        return getElements(toscaJson, TypeUtils.ToscaTagNamesEnum.INPUTS, elementGenByName, func);
    }

    public static Either<Map<String, OutputDefinition>, ResultStatusEnum> getOutputs(Map<String, Object> toscaJson) {
        final Function<String, OutputDefinition> elementGenByName = ImportUtils::createOutputs;
        final Function<Map<String, Object>, OutputDefinition> func = object -> createModuleOutput(object);
        return getElements(toscaJson, TypeUtils.ToscaTagNamesEnum.OUTPUTS, elementGenByName, func);
    }

    public static Either<Map<String, InputDefinition>, ResultStatusEnum> getInputs(final Map<String, Object> toscaJson) {
        return getElements(toscaJson, TypeUtils.ToscaTagNamesEnum.INPUTS, ImportUtils::createInputs, ImportUtils::createModuleInput);
    }

    private static <T> Either<Map<String, T>, ResultStatusEnum> getElements(Map<String, Object> toscaJson, TypeUtils.ToscaTagNamesEnum elementTagName,
                                                                            Function<String, T> elementGenByName,
                                                                            Function<Map<String, Object>, T> func) {
        Either<Map<String, T>, ResultStatusEnum> eitherResult = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
        Either<Map<String, Object>, ResultStatusEnum> toscaAttributes = findFirstToscaMapElement(toscaJson, elementTagName);
        if (toscaAttributes.isLeft()) {
            Map<String, Object> jsonAttributes = toscaAttributes.left().value();
            Map<String, T> moduleAttributes = new HashMap<>();
            Iterator<Entry<String, Object>> propertiesNameValue = jsonAttributes.entrySet().iterator();
            while (propertiesNameValue.hasNext()) {
                Entry<String, Object> attributeNameValue = propertiesNameValue.next();
                if (attributeNameValue.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked") T attribute = func.apply((Map<String, Object>) attributeNameValue.getValue());
                    if (attribute != null) {
                        moduleAttributes.put(String.valueOf(attributeNameValue.getKey()), attribute);
                    }
                } else {
                    T element = elementGenByName.apply(String.valueOf(attributeNameValue.getValue()));
                    moduleAttributes.put(String.valueOf(attributeNameValue.getKey()), element);
                }
            }
            if (moduleAttributes.size() > 0) {
                eitherResult = Either.left(moduleAttributes);
            }
        }
        return eitherResult;
    }

    private static AttributeDefinition createAttribute(String name) {
        AttributeDefinition attribute = new AttributeDefinition();
        attribute.setName(name);
        return attribute;
    }

    private static PropertyDefinition createProperties(String name) {
        PropertyDefinition property = new PropertyDefinition();
        property.setDefaultValue(name);
        property.setName(name);
        return property;
    }

    private static InputDefinition createInputs(String name) {
        InputDefinition input = new InputDefinition();
        input.setName(name);
        return input;
    }

    private static OutputDefinition createOutputs(final String name) {
        final OutputDefinition output = new OutputDefinition();
        output.setName(name);
        return output;
    }

    private static Annotation createAnnotation(String name) {
        Annotation annotation = new Annotation();
        annotation.setName(name);
        return annotation;
    }

    static Either<List<HeatParameterDefinition>, ResultStatusEnum> getHeatParameters(Map<String, Object> heatData, String artifactType) {
        Either<List<HeatParameterDefinition>, ResultStatusEnum> eitherResult = Either.right(ResultStatusEnum.ELEMENT_NOT_FOUND);
        Either<Map<String, Object>, ResultStatusEnum> toscaProperties = findFirstToscaMapElement(heatData, TypeUtils.ToscaTagNamesEnum.PARAMETERS);
        if (toscaProperties.isLeft()) {
            Map<String, Object> jsonProperties = toscaProperties.left().value();
            List<HeatParameterDefinition> moduleProperties = new ArrayList<>();
            Iterator<Entry<String, Object>> propertiesNameValue = jsonProperties.entrySet().iterator();
            while (propertiesNameValue.hasNext()) {
                Entry<String, Object> propertyNameValue = propertiesNameValue.next();
                if (propertyNameValue.getValue() instanceof Map || propertyNameValue.getValue() instanceof List) {
                    if (!artifactType.equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
                        @SuppressWarnings("unchecked") Either<HeatParameterDefinition, ResultStatusEnum> propertyStatus = createModuleHeatParameter(
                            (Map<String, Object>) propertyNameValue.getValue());
                        if (propertyStatus.isRight()) {
                            return Either.right(propertyStatus.right().value());
                        }
                        HeatParameterDefinition property = propertyStatus.left().value();
                        property.setName(String.valueOf(propertyNameValue.getKey()));
                        moduleProperties.add(property);
                    } else {
                        addHeatParamDefinition(moduleProperties, propertyNameValue, true);
                    }
                } else {
                    addHeatParamDefinition(moduleProperties, propertyNameValue, false);
                }
            }
            if (!isEmpty(moduleProperties)) {
                eitherResult = Either.left(moduleProperties);
            }
        }
        return eitherResult;
    }

    private static void addHeatParamDefinition(List<HeatParameterDefinition> moduleProperties, Entry<String, Object> propertyNameValue,
                                               boolean isJson) {
        HeatParameterDefinition property = new HeatParameterDefinition();
        Object value = propertyNameValue.getValue();
        if (value != null) {
            property.setDefaultValue(isJson ? new Gson().toJson(value) : StringEscapeUtils.escapeJava(String.valueOf(value)));
        }
        property.setName(String.valueOf(propertyNameValue.getKey()));
        moduleProperties.add(property);
    }

    private static Either<HeatParameterDefinition, ResultStatusEnum> createModuleHeatParameter(Map<String, Object> propertyValue) {
        HeatParameterDefinition propertyDef = new HeatParameterDefinition();
        String type;
        Either<String, ResultStatusEnum> propertyFieldType = findFirstToscaStringElement(propertyValue, TypeUtils.ToscaTagNamesEnum.TYPE);
        if (propertyFieldType.isLeft()) {
            type = propertyFieldType.left().value();
            propertyDef.setType(type);
        } else {
            return Either.right(ResultStatusEnum.INVALID_PROPERTY_TYPE);
        }
        Either<String, ResultStatusEnum> propertyFieldDescription = findFirstToscaStringElement(propertyValue,
            TypeUtils.ToscaTagNamesEnum.DESCRIPTION);
        if (propertyFieldDescription.isLeft()) {
            propertyDef.setDescription(propertyFieldDescription.left().value());
        }
        Either<Object, ResultStatusEnum> propertyFieldDefaultVal = findToscaElement(propertyValue, TypeUtils.ToscaTagNamesEnum.DEFAULT_VALUE,
            ToscaElementTypeEnum.ALL);
        if (propertyFieldDefaultVal.isLeft()) {
            if (propertyFieldDefaultVal.left().value() == null) {
                return Either.right(ResultStatusEnum.INVALID_PROPERTY_VALUE);
            }
            Object value = propertyFieldDefaultVal.left().value();
            String defaultValue =
                type.equals(HeatParameterType.JSON.getType()) ? new Gson().toJson(value) : StringEscapeUtils.escapeJava(String.valueOf(value));
            propertyDef.setDefaultValue(defaultValue);
            propertyDef.setCurrentValue(defaultValue);
        }
        return Either.left(propertyDef);
    }

    public static boolean containsGetInput(Object propValue) {
        String value = getPropertyJsonStringValue(propValue, ToscaPropertyType.MAP.getType());
        return value != null && value.contains(TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
    }

    public static String getPropertyJsonStringValue(Object value, String type) {
        if (type == null) {
            return null;
        }
        ToscaPropertyType validType = ToscaPropertyType.isValidType(type);
        if (validType == null
            || validType == ToscaPropertyType.JSON
            || validType == ToscaPropertyType.MAP
            || validType == ToscaPropertyType.LIST
            || value instanceof Map) {
            return (new Gson()).toJson(value);
        }
        return value.toString();
    }

    /**
     * removes from Json map (toscaJson) first element found by name (elementName) note that this method could update the received argument toscaJson
     *
     * @param toscaJson
     * @param elementName
     */
    public static void removeElementFromJsonMap(Map<String, Object> toscaJson, String elementName) {
        for (Entry<String, Object> entry : toscaJson.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals(elementName)) {
                toscaJson.remove(elementName);
                return;
            } else if (value instanceof Map) {
                removeElementFromJsonMap((Map<String, Object>) value, elementName);
            }
        }
    }

    public enum ResultStatusEnum {
        ELEMENT_NOT_FOUND, GENERAL_ERROR, OK, INVALID_PROPERTY_DEFAULT_VALUE, INVALID_PROPERTY_TYPE, INVALID_PROPERTY_VALUE, MISSING_ENTRY_SCHEMA_TYPE, INVALID_PROPERTY_NAME, INVALID_ATTRIBUTE_NAME
    }

    public enum ToscaElementTypeEnum {
        BOOLEAN, STRING, MAP, LIST, ALL
    }

    private static class CustomResolver extends Resolver {

        @Override
        protected void addImplicitResolvers() {
            // avoid implicit resolvers for strings that can be interpreted as boolean values
            addImplicitResolver(Tag.STR, EMPTY, "");
            addImplicitResolver(Tag.STR, NULL, null);
            addImplicitResolver(Tag.NULL, NULL, "~nN\0");
            addImplicitResolver(Tag.NULL, EMPTY, null);
            addImplicitResolver(Tag.INT, INT, "-+0123456789");
            addImplicitResolver(Tag.FLOAT, FLOAT, "-+0123456789.");
            addImplicitResolver(Tag.YAML, YAML, "!&*");
        }
    }

    private static class YamlLoader extends YamlProcessor {

        public Yaml getStrictYamlLoader() {
            return createYaml();
        }
    }

    public static class Constants {

        public static final String FIRST_NON_CERTIFIED_VERSION = "0.1";
        public static final String VENDOR_NAME = "ONAP (Tosca)";
        public static final String VENDOR_RELEASE = "1.0.0.wd03";
        public static final LifecycleStateEnum NORMATIVE_TYPE_LIFE_CYCLE = LifecycleStateEnum.CERTIFIED;
        public static final LifecycleStateEnum NORMATIVE_TYPE_LIFE_CYCLE_NOT_CERTIFIED_CHECKOUT = LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT;
        public static final boolean NORMATIVE_TYPE_HIGHEST_VERSION = true;
        public static final String ABSTRACT_CATEGORY_NAME = "Generic";
        public static final String ABSTRACT_SUBCATEGORY = "Abstract";
        public static final String DEFAULT_ICON = "defaulticon";
        public static final String INNER_VFC_DESCRIPTION = "Not reusable inner VFC";
        public static final String USER_DEFINED_RESOURCE_NAMESPACE_PREFIX = "org.openecomp.resource.";
        public static final String UI_JSON_PAYLOAD_NAME = "payloadName";
        public static final String CVFC_DESCRIPTION = "Complex node type that is used as nested type in VF";
        public static final String ESCAPED_DOUBLE_QUOTE = "\"";
        public static final String QUOTE = "'";
        public static final String VF_DESCRIPTION = "Nested VF in service";

        private Constants() {
        }
    }
}
