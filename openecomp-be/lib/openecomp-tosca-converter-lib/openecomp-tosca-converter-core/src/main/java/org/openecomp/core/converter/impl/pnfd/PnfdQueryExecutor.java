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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.MapUtils;
import org.openecomp.core.converter.pnfd.exception.QueryOperationNotSupportedException;
import org.openecomp.core.converter.pnfd.model.ConversionQuery;

/**
 * Runs YAML queries
 */
public class PnfdQueryExecutor {

    private PnfdQueryExecutor() {

    }

    /**
     * Finds if a YAML object contains the provided YAML query.
     * @param conversionQuery   The query
     * @param yamlObject        The YAML object to be searched
     * @return
     * {@code true} if the YAML query structure was found in the YAML object, {@code false} otherwise.
     */
    public static boolean find(final ConversionQuery conversionQuery, final Object yamlObject) {
        return find(conversionQuery.getQuery(), yamlObject);
    }

    /**
     * Recursive structure combined with {@link #findMap(Map, Map)} to find if a YAML object contains the provided YAML query.
     * Compares the objects if it's a scalar value, otherwise go further in the YAML hierarchical structure
     * calling the {@link #findMap(Map, Map)}.
     * @param query        The current query
     * @param yamlObject   The current YAML object to be searched
     * @return
     * {@code true} if the YAML query structure was found in the YAML object, {@code false} otherwise.
     */
    private static boolean find(final Object query, final Object yamlObject) {
        if (query == null) {
            return true;
        }

        checkSupportedQueryType(query);

        if (query instanceof String) {
            return query.equals(yamlObject);
        }
        if (query instanceof Map) {
            return findMap((Map) query, (Map) yamlObject);
        }
        return false;
    }

    /**
     * Recursive structure combined with {@link #find(Object, Object)} to find if a YAML object contains the provided YAML query.     *
     * @param query        The query current object
     * @param yamlObject   The YAML object to be searched
     * @return
     * {@code true} if the YAML query structure was found in the YAML object, {@code false} otherwise.
     */
    private static boolean findMap(final Map query, final Map yamlObject) {
        if (MapUtils.isEmpty(query) || MapUtils.isEmpty(yamlObject)) {
            return false;
        }

        if (!yamlObject.keySet().containsAll(query.keySet())) {
            return false;
        }

        return query.entrySet().parallelStream().allMatch(queryEntryObj -> {
            final Entry queryEntry = (Entry) queryEntryObj;
            return find(queryEntry.getValue(), yamlObject.get(queryEntry.getKey()));
        });
    }

    /**
     * Checks the supported types for a query.
     * @param query    the query to check
     */
    private static void checkSupportedQueryType(final Object query) {
        if (query instanceof String || query instanceof Map) {
            return;
        }
        if (query instanceof List || query instanceof Set) {
            throw new QueryOperationNotSupportedException("Yaml list query is not supported yet");
        }
        throw new QueryOperationNotSupportedException(
            String.format("Yaml query operation for '%s' is not supported yet", query.getClass())
        );
    }

}
