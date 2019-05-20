package org.openecomp.sdc.be.dao.janusgraph;

import org.janusgraph.graphdb.query.JanusGraphPredicate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.janusgraph.core.attribute.Contain.NOT_IN;
import static java.util.Collections.emptyMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public class JanusGraphUtils {

    private JanusGraphUtils() {
    }

    public static <T> Map<String, Entry<JanusGraphPredicate, Object>> buildNotInPredicate(String propKey, Collection<T> notInCollection) {
        if (isEmpty(notInCollection)) {
            return emptyMap();
        }
        Map<String, Entry<JanusGraphPredicate,  Object>> predicateCriteria = new HashMap<>();
        predicateCriteria.put(propKey, new HashMap.SimpleEntry<>(NOT_IN, notInCollection));
        return predicateCriteria;
    }
}
