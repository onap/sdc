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

package org.openecomp.core.converter.impl.pnfd.strategy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openecomp.core.converter.pnfd.strategy.AbstractPnfdConversionStrategy;
import org.openecomp.core.converter.pnfd.model.ConversionStrategyType;
import org.openecomp.core.converter.pnfd.model.PnfTransformationToken;

public class ReplaceInListConversionStrategy extends AbstractPnfdConversionStrategy<List<Object>, List<Object>> {

    private final Map<Object, Object> replaceInListConversionStrategyMap;

    public ReplaceInListConversionStrategy(final List<Map<String, Object>> descriptorList) {
        super(ConversionStrategyType.REPLACE_IN_LIST, new HashMap<>());
        replaceInListConversionStrategyMap = new LinkedHashMap<>();
        descriptorList
            .forEach(stringObjectMap -> replaceInListConversionStrategyMap.put(stringObjectMap.get(
                PnfTransformationToken.FROM.getName())
                , stringObjectMap.get(PnfTransformationToken.TO.getName()))
            );
    }

    @Override
    public Optional<List<Object>> convert(final List<Object> originalValue) {
        if (originalValue == null || originalValue.isEmpty()) {
            return Optional.empty();
        }

        final List<Object> convertedList = originalValue.stream()
            .map(replaceInListConversionStrategyMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return convertedList.isEmpty() ? Optional.empty() : Optional.of(convertedList);
    }
}
