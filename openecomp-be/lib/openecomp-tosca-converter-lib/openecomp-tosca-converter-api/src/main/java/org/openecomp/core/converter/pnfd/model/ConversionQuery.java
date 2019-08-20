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

import java.util.Map;
import java.util.Objects;

/**
 * Stores a YAML query. The query is a YAML Object that should have the same structure or part of the original YAML that
 * is desired to find in a TOSCA Yaml block.
 */
public class ConversionQuery {

    private final Object query;

    public ConversionQuery(final Object query) {
        this.query = query;
    }

    public Object getQuery() {
        return query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConversionQuery that = (ConversionQuery) o;
        return Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query);
    }

    /**
     * Checks if the query is a valid attribute query.
     *
     * @return {@code true} when its valid, {@code false} otherwise.
     */
    public boolean isValidAttributeQuery() {
        //only a map structure is supported
        return query instanceof Map;
    }
}
