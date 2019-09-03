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
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.NAME;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.QUERY;
import static org.openecomp.core.converter.pnfd.model.PnfTransformationToken.TRANSFORMATION_FOR;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.pnfd.model.TransformationBlock;

/**
 * Handles YAML from/to {@link Transformation} conversions
 */
public class TransformationYamlParser {

    private TransformationYamlParser() {

    }

    /**
     * Parses the given YAML object to a {@link Transformation} instance.
     * @param transformationYaml      the YAML object representing a transformation
     * @return
     *  A new instance of {@link Transformation}.
     */
    public static Transformation parse(final Map<String, Object> transformationYaml) {
        final Transformation transformation = new Transformation();
        transformation.setName((String) transformationYaml.get(NAME.getName()));
        transformation.setDescription((String) transformationYaml.get(DESCRIPTION.getName()));

        final String block = (String) transformationYaml.get(TRANSFORMATION_FOR.getName());
        transformation.setBlock(TransformationBlock.parse(block).orElse(null));

        transformation.setConversionQuery(
            ConversionQueryYamlParser.parse(transformationYaml.get(QUERY.getName()))
        );
        transformation.setConversionDefinitionList(parseConversions(transformationYaml));

        return transformation;
    }

    private static List<ConversionDefinition> parseConversions(final Map<String, Object> conversionYaml) {
        final List<Object> conversionList = (List<Object>) conversionYaml.get(CONVERSIONS.getName());

        if (CollectionUtils.isEmpty(conversionList)) {
            return Collections.emptyList();
        }

        return conversionList.stream()
            .map(conversion -> ConversionDefinitionYamlParser.parse((Map<String, Object>) conversion))
            .collect(Collectors.toList());
    }

}
