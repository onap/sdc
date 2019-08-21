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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openecomp.core.converter.pnfd.model.ConversionStrategyType;
import org.openecomp.core.converter.pnfd.model.PnfTransformationToken;
import org.openecomp.core.converter.impl.pnfd.strategy.CopyConversionStrategy;
import org.openecomp.core.converter.pnfd.strategy.PnfdConversionStrategy;
import org.openecomp.core.converter.impl.pnfd.strategy.ReplaceConversionStrategy;
import org.openecomp.core.converter.impl.pnfd.strategy.ReplaceInListConversionStrategy;


/**
 * Handles YAML from/to {@link PnfdConversionStrategy} conversions.
 */
public class PnfdConversionStrategyYamlParser {

    private PnfdConversionStrategyYamlParser() {

    }

    /**
     * Parses the given YAML object to a {@link PnfdConversionStrategy} instance.
     * @param strategyYaml      the YAML object representing a conversion strategy
     * @return
     *  A new instance of {@link PnfdConversionStrategy}.
     */
    public static Optional<PnfdConversionStrategy> parse(final Map<String, Object> strategyYaml) {
        final Optional<ConversionStrategyType> optionalStrategy = ConversionStrategyType.parse(
            (String) strategyYaml.get(PnfTransformationToken.STRATEGY.getName())
        );

        if (!optionalStrategy.isPresent()) {
            return Optional.empty();
        }

        final ConversionStrategyType strategyType = optionalStrategy.get();
        if (strategyType == ConversionStrategyType.COPY) {
            return Optional.of(new CopyConversionStrategy());
        }
        if (strategyType == ConversionStrategyType.REPLACE) {
            final Object from = strategyYaml.get(PnfTransformationToken.FROM.getName());
            final Object to = strategyYaml.get(PnfTransformationToken.TO.getName());
            return Optional.of(new ReplaceConversionStrategy(from, to));
        }
        if (strategyType == ConversionStrategyType.REPLACE_IN_LIST) {
            return Optional.of(new ReplaceInListConversionStrategy(
                (List<Map<String, Object>>) strategyYaml.get(PnfTransformationToken.LIST.getName()))
            );
        }
        return Optional.empty();
    }

}
