/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.dao.janusgraph;

import static java.util.Collections.emptyMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.janusgraph.core.attribute.Contain.NOT_IN;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.janusgraph.graphdb.query.JanusGraphPredicate;

public class JanusGraphUtils {

    private JanusGraphUtils() {
    }

    public static <T> Map<String, Entry<JanusGraphPredicate, Object>> buildNotInPredicate(String propKey, Collection<T> notInCollection) {
        if (isEmpty(notInCollection)) {
            return emptyMap();
        }
        Map<String, Entry<JanusGraphPredicate, Object>> predicateCriteria = new HashMap<>();
        predicateCriteria.put(propKey, new HashMap.SimpleEntry<>(NOT_IN, notInCollection));
        return predicateCriteria;
    }
}
