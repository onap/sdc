/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.core.converter.impl.pnfd.parser;

import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.CONVERSIONS;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.DESCRIPTION;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.FROM;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.NAME;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.PROPERTIES;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.QUERY;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.TO;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.TRANSFORMATION_FOR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.core.converter.impl.pnfd.strategy.ReplaceConversionStrategy;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;
import org.openecomp.core.converter.pnfd.model.ConversionQuery;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.pnfd.model.TransformationBlock;
import org.openecomp.core.converter.pnfd.model.TransformationProperty;
import org.openecomp.core.converter.pnfd.model.TransformationPropertyType;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * Handles YAML from/to {@link Transformation} conversions
 */
public class TransformationYamlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationYamlParser.class);

    private TransformationYamlParser() {

    }

    /**
     * Parses the given YAML object to a {@link Transformation} instance.
     * @param transformationYaml      the YAML object representing a transformation
     * @return
     *  A new instance of {@link Transformation}.
     */
    public static Optional<Transformation> parse(final Map<String, Object> transformationYaml) {
        final Transformation transformation = new Transformation();
        final Optional<String> name = parseStringAttribute(NAME.getName(), transformationYaml);
        if (!name.isPresent()) {
            LOGGER.warn("Invalid '{}' value in transformation '{}'", NAME.getName(), transformationYaml.toString());
        }
        transformation.setName(name.orElse(null));
        transformation.setDescription(parseStringAttribute(DESCRIPTION.getName(), transformationYaml).orElse(null));
        transformation.setPropertySet(readProperties(transformationYaml));

        final String block = parseStringAttribute(TRANSFORMATION_FOR.getName(), transformationYaml).orElse(null);
        final Optional<TransformationBlock> transformationBlockOptional = TransformationBlock.parse(block);
        if (transformationBlockOptional.isPresent()) {
            final TransformationBlock transformationBlock = transformationBlockOptional.get();
            transformation.setBlock(transformationBlock);
            parseTransformationBlock(transformationBlock, transformation, transformationYaml);
        } else {
            LOGGER.warn("Invalid '{}' value in transformation '{}'", TRANSFORMATION_FOR.getName(),
                transformationYaml.toString());
        }

        if (transformation.isValid()) {
            return Optional.of(transformation);
        }

        return Optional.empty();
    }

    private static Set<TransformationProperty> readProperties(final Map<String, Object> transformationYaml) {
        final Map<String, Object> propertyMap = (Map<String, Object>) transformationYaml.get(PROPERTIES.getName());
        if (MapUtils.isEmpty(propertyMap)) {
            return Collections.emptySet();
        }

        final Set<TransformationProperty> propertySet = new HashSet<>();

        propertyMap.forEach((key, value) -> {
            final TransformationPropertyType transformationPropertyType = TransformationPropertyType.parse(key)
                .orElse(null);

            if(transformationPropertyType != null) {
                if (value instanceof String) {
                    propertySet.add(new TransformationProperty<>(transformationPropertyType, (String) value));
                } else if (value instanceof Boolean) {
                    propertySet.add(new TransformationProperty<>(transformationPropertyType, (Boolean) value));
                } else if (value instanceof Integer) {
                    propertySet.add(new TransformationProperty<>(transformationPropertyType, (Integer) value));
                } else {
                    propertySet.add(new TransformationProperty<>(transformationPropertyType, value));
                }
            }
        });

        return propertySet;
    }

    private static void parseTransformationBlock(final TransformationBlock transformationBlock,
                                                 final Transformation transformationReference,
                                                 final Map<String, Object> transformationYaml) {
        if (transformationBlock == TransformationBlock.CUSTOM_NODE_TYPE) {
            parseCustomNodeTypeBlock(transformationReference, transformationYaml);
            return;
        }

        ConversionQueryYamlParser.parse(transformationYaml.get(QUERY.getName()))
            .ifPresent(transformationReference::setConversionQuery);

        transformationReference.setConversionDefinitionList(parseConversions(transformationYaml));
    }

    private static void parseCustomNodeTypeBlock(final Transformation transformationReference,
                                                 final Map<String, Object> transformationYaml) {
        final Object fromAttribute = transformationYaml.get(FROM.getName());
        if (!(fromAttribute instanceof String)) {
            return;
        }
        final String from = parseStringAttribute(FROM.getName(), transformationYaml).orElse(null);

        final Object toAttribute = transformationYaml.get(TO.getName());
        if (!(toAttribute instanceof String)) {
            return;
        }
        final String to = parseStringAttribute(TO.getName(), transformationYaml).orElse(null);

        final HashMap<String, String> transformationQuery = new HashMap<>();
        transformationQuery.put(ToscaTagNamesEnum.DERIVED_FROM.getElementName(), from);
        transformationReference.setConversionQuery(new ConversionQuery(transformationQuery));

        final List<ConversionDefinition> conversionDefinitionList = new ArrayList<>();
        final HashMap<String, String> conversionDefinitionQuery = new HashMap<>();
        conversionDefinitionQuery.put(ToscaTagNamesEnum.TYPE.getElementName(), null);
        ConversionDefinition conversionDefinition = new ConversionDefinition(new ConversionQuery(conversionDefinitionQuery)
            , ToscaTagNamesEnum.TYPE.getElementName(), new ReplaceConversionStrategy(from, to));
        conversionDefinitionList.add(conversionDefinition);
        transformationReference.setConversionDefinitionList(conversionDefinitionList);
    }

    private static List<ConversionDefinition> parseConversions(final Map<String, Object> conversionYaml) {
        final List<Object> conversionList = (List<Object>) conversionYaml.get(CONVERSIONS.getName());

        if (CollectionUtils.isEmpty(conversionList)) {
            return Collections.emptyList();
        }

        return conversionList.stream()
            .map(conversion -> ConversionDefinitionYamlParser.parse((Map<String, Object>) conversion).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static Optional<String> parseStringAttribute(final String attribute, final Map<String, Object> transformationYaml) {
        try {
            return Optional.of((String) transformationYaml.get(attribute));
        } catch (final Exception ignore) {
            LOGGER.warn("Could not parse the String '{}' in transformation '{}'", attribute, transformationYaml.toString());
            return Optional.empty();
        }
    }

}
