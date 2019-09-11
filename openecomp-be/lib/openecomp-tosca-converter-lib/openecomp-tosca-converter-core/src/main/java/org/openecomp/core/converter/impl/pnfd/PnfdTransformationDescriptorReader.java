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

package org.openecomp.core.converter.impl.pnfd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.converter.impl.pnfd.parser.TransformationYamlParser;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.pnfd.model.TransformationDescription;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * Reads the PNF Transformation Description
 */
public class PnfdTransformationDescriptorReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PnfdTransformationDescriptorReader.class);

    /**
     * Parse the transformation description to {@link TransformationDescription} class.
     * @return
     *  The {@link TransformationDescription} instance.
     */
    public TransformationDescription parse(final InputStream transformationDescriptionInputStream) {
        final List<Object> transformationList = readDescriptionYaml(transformationDescriptionInputStream);
        final Set<Transformation> transformationSet = parseTransformationList(transformationList);

        return new TransformationDescription(transformationSet);
    }

    /**
     * Reads the description file that has the required YAML format.
     * @return
     *  The yaml parsed to Object
     */
    private List<Object> readDescriptionYaml(final InputStream transformationDescriptionPath) {
        try (final InputStream fileInputStream = transformationDescriptionPath) {
            return YamlUtil.yamlToList(fileInputStream).orElse(Collections.emptyList());
        } catch (final FileNotFoundException e) {
            LOGGER.error("Could not find the resource on path.", e);
        } catch (final IOException e) {
            LOGGER.error("Could not load resource.", e);
        }
        return Collections.emptyList();
    }

    /**
     * Parse the transformation list represented in a YAML object to {@link Transformation}.
     * @param transformationYamlList    the YAML object read from the transformation description file
     * @return
     *  The set of transformations represented as {@link Transformation} class
     */
    private Set<Transformation> parseTransformationList(final List<Object> transformationYamlList) {
        if (CollectionUtils.isEmpty(transformationYamlList)) {
            return Collections.emptySet();
        }

        return transformationYamlList.stream()
            .map(conversionMap -> TransformationYamlParser.parse((Map<String, Object>) conversionMap).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

}
