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

package org.openecomp.core.converter.pnfd.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * Represents a transformation from the PNFD transformation descriptor.
 */
@Getter
@Setter
public class Transformation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transformation.class);

    private String name;
    private String description;
    private TransformationBlock block;
    private Set<TransformationProperty> propertySet;
    private ConversionQuery conversionQuery;
    private List<ConversionDefinition> conversionDefinitionList;

    /**
     * Checks if the instance contains all necessary information to be used.
     *
     * @return {code true} if the instance is valid, {code false} otherwise
     */
    public boolean isValid() {
        if (block == TransformationBlock.GET_INPUT_FUNCTION) {
            return !StringUtils.isEmpty(name) && !CollectionUtils.isEmpty(conversionDefinitionList);
        }
        return block != null && conversionQuery != null && !CollectionUtils.isEmpty(conversionDefinitionList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Transformation that = (Transformation) o;
        //if there is no query, compares by block and name.
        if (conversionQuery == null && that.conversionQuery == null) {
            return block == that.block &&
                Objects.equals(name, that.name);
        }
        //transformations with the same block and query will override themselves.
        return block == that.block &&
            Objects.equals(conversionQuery, that.conversionQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, conversionQuery);
    }

    public <T> Optional<T> getPropertyValue(final TransformationPropertyType type, Class<T> clazz) {
        if (CollectionUtils.isEmpty(propertySet)) {
            return Optional.empty();
        }

        final Optional<TransformationProperty> transformationProperty = propertySet.stream()
            .filter(transformationProperty1 -> transformationProperty1.getType() == type)
            .findFirst();
        if (transformationProperty.isPresent()) {
            try {
                T value = clazz.cast(transformationProperty.get().getValue());
                return Optional.of(value);
            } catch (final ClassCastException e) {
                LOGGER.warn("Could not get property value.", e);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
