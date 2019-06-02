/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.dao.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> List<T> safeGetList(List<T> listToCheck) {
        return org.apache.commons.collections.CollectionUtils.isNotEmpty(listToCheck) ? listToCheck : Collections.emptyList();
    }

    /**
     * Add the content of the 'source' Set to the 'target' set and return the
     * union set.
     * <p>
     * If 'source' is null then a new set is created and returned. If 'target'
     * is null then no content is added to the 'source' Set or newly created
     * set.
     *
     * @param source The Set to merge in the target Set.
     * @param target The Set in which the source set will be merged (through
     *               addAll).
     * @return The target Set with addition of source Set elements, or a new Set
     * (including content of source set) if target was null.
     */
    public static <T> Set<T> merge(Set<T> source, Set<T> target) {
        Set<T> merged = new HashSet<>();
        if (target != null) {
            merged.addAll(target);
        }
        if (source != null) {
            merged.addAll(source);
        }
        return merged.isEmpty() ? null : merged;
    }

    /**
     * <p>
     * Add the content of the 'source' Map to the 'target' set and return the
     * union Map.
     * </p>
     * <p>
     * If 'source' is null then a new Map is created and returned. If 'target'
     * is null then no content is added to the 'source' Map or newly created
     * Map.
     * </p>
     *
     * @param source   The Map to merge in the target Map.
     * @param target   The Map in which the source Map will be merged (through
     *                 addAll).
     * @param override If an key from the source map already exists in the target
     *                 map, should it override (true) or not (false) the value.
     * @return The target Map with addition of source Map elements, or a new Map
     * (including content of source set) if target was null.
     */
    public static <T, V> Map<T, V> merge(Map<T, ? extends V> source, Map<T, V> target, boolean override) {
        if (target == null) {
            target = new HashMap();
        }

        if (source != null) {
            for (Entry<T, ? extends V> entry : source.entrySet()) {
                if (override || !target.containsKey(entry.getKey())) {
                    target.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return target.isEmpty() ? null : target;
    }

    /**
     * Merge two lists, the merge is performed based on the contains method so
     * elements presents both in source and target are not added twice to the
     * list.
     *
     * @param source The source list.
     * @param target The target list.
     * @return A list that represents the merged collections.
     */
    public static <T> List<T> merge(List<T> source, List<T> target) {
        List<T> merged = target == null ? new ArrayList<>() : target;

        if (source == null) {
            return merged;
        }

        for (T t : source) {
            if (!merged.contains(t)) {
                merged.add(t);
            }
        }

        return merged;
    }

    /**
     * Returns a new list containing the second list appended to the
     * first list.  The {@link List#addAll(Collection)} operation is
     * used to append the two given lists into a new list.
     *
     * @param list1 the first list
     * @param list2 the second list
     * @return a new list containing the union of those lists
     * @throws NullPointerException if either list is null
     */
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        List<T> result = new ArrayList<>(list1);
        result.addAll(list2);
        return result;
    }
}
