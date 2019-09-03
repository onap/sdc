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

import java.util.Map;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;
import org.openecomp.core.converter.pnfd.model.ConversionQuery;
import org.openecomp.core.converter.pnfd.model.PnfTransformationToken;
import org.openecomp.core.converter.pnfd.strategy.PnfdConversionStrategy;

/**
 * Handles YAML from/to {@link ConversionDefinition} conversions
 */
public class ConversionDefinitionYamlParser {

    private ConversionDefinitionYamlParser() {

    }

    /**
     * Parses the given a YAML object to a {@link ConversionDefinition} instance.
     * @param conversionYaml    the YAML object representing a conversion definition
     * @return
     *  A new instance of {@link ConversionDefinition}.
     */
    public static ConversionDefinition parse(final Map<String, Object> conversionYaml) {
        final ConversionQuery conversionQuery = ConversionQueryYamlParser
            .parse(conversionYaml.get(PnfTransformationToken.QUERY.getName()));
        final String toName = (String) conversionYaml.get(PnfTransformationToken.TO_NAME.getName());
        final PnfdConversionStrategy toValue = PnfdConversionStrategyYamlParser
            .parse((Map<String, Object>) conversionYaml.get(PnfTransformationToken.TO_VALUE.getName()))
            .orElse(null);
        final String toGetInput = (String) conversionYaml.get(PnfTransformationToken.TO_GET_INPUT.getName());

        return new ConversionDefinition(conversionQuery, toName, toValue, toGetInput);
    }

}
