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

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a PNFD transformation block property.
 *
 * @param <T> the type of the property value
 */
@Getter
@AllArgsConstructor
public class TransformationProperty<T> {

    private TransformationPropertyType type;
    private T value;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TransformationProperty<?> property = (TransformationProperty<?>) o;
        return Objects.equals(type, property.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
