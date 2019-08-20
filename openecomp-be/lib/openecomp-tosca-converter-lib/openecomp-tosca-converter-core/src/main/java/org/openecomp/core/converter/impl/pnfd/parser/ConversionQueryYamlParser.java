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

import org.openecomp.core.converter.pnfd.model.ConversionQuery;

/**
 * Handles YAML from/to {@link ConversionQuery} conversions
 */
public class ConversionQueryYamlParser {

    private ConversionQueryYamlParser() {

    }

    /**
     * Parses the given a YAML object to a {@link ConversionQuery} instance.
     * @param conversionYaml    the YAML object representing a conversion query
     * @return
     *  A new instance of {@link ConversionQuery}.
     */
    public static ConversionQuery parse(final Object conversionYaml) {
        return new ConversionQuery(conversionYaml);
    }
}
