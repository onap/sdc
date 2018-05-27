package org.openecomp.sdc.be.dao.titan;

import static com.thinkaurelius.titan.core.attribute.Contain.NOT_IN;
import static java.util.Collections.emptyMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.thinkaurelius.titan.graphdb.query.TitanPredicate;

public class TitanUtils {

    private TitanUtils() {
    }

    public static <T> Map<String, Entry<TitanPredicate, Object>> buildNotInPredicate(String propKey, Collection<T> notInCollection) {
        if (isEmpty(notInCollection)) {
            return emptyMap();
        }
        Map<String, Entry<TitanPredicate,  Object>> predicateCriteria = new HashMap<>();
        predicateCriteria.put(propKey, new HashMap.SimpleEntry<>(NOT_IN, notInCollection));
        return predicateCriteria;
    }
}
