/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.tosca;

import static org.openecomp.sdc.be.utils.CommonBeUtils.compareAsdcComponentVersions;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.openecomp.sdc.be.model.Component;

/**
 * Provides caching abilities for components
 */
public final class ComponentCache {
    // TODO: Make this final whenever possible. The current code using the class

    private final BinaryOperator<CacheEntry> merge;
    // does not allow this.
    private Map<String, CacheEntry> entries = HashMap.empty();

    private ComponentCache(BinaryOperator<CacheEntry> merge) {
        this.merge = merge;
    }

    /**
     * Creates an overwritable cache based on a merging strategy
     *
     * @param merge The strategy used to merge two values which keys are the same
     */
    public static ComponentCache overwritable(BinaryOperator<CacheEntry> merge) {
        return new ComponentCache(merge);
    }

    /**
     * Creates a cached entry
     *
     * @param id        The id of the entry
     * @param fileName  the filename of the entry
     * @param component the cached component
     */
    public static CacheEntry entry(String id, String fileName, Component component) {
        return new CacheEntry(id, fileName, component);
    }

    /**
     * Decorate the cache with a listener called whenever a value is merged
     *
     * @param bc the consumer called when a value is merged
     */
    public ComponentCache onMerge(BiConsumer<CacheEntry, CacheEntry> bc) {
        return new ComponentCache((oldValue, newValue) -> {
            CacheEntry value = merge.apply(oldValue, newValue);
            if (value.equals(newValue)) {
                bc.accept(oldValue, newValue);
            }
            return value;
        });
    }

    // For now we'll keep this as is, to prevent the refactoring to be too big
    public Iterable<ImmutableTriple<String, String, Component>> iterable() {
        return all().map(e -> new ImmutableTriple<>(e.id, e.fileName, e.component));
    }

    /**
     * Streams all the entries stored in the cache
     */
    public Stream<CacheEntry> all() {
        return entries.values().toStream();
    }
    // TODO: Encapsulate the cache and expose functions to interact with it

    /**
     * Tells if an entry has been cached for a specific key
     *
     * @param key The key used to index the entry
     */
    public boolean notCached(String key) {
        return !entries.get(key).isDefined();
    }

    /**
     * Store an entry in the cache. Keep in mind that currently this mutates the cache and does not work in a referentially transparent way (This
     * should be fixed whenever possible).
     *
     * @param id        The id of the entry
     * @param fileName  the filename of the entry
     * @param component the cached component
     */
    public ComponentCache put(String id, String fileName, Component component) {
        String uuid = component.getInvariantUUID();
        CacheEntry entry = new CacheEntry(id, fileName, component);
        // TODO: Make the entries final whenever possible. The current code using the class does not allow this
        entries = entries.put(uuid, entry, merge);
        return this;
    }

    public interface MergeStrategy {

        /**
         * A strategy designed to favour the latest component version when merging two cached entries
         */
        static BinaryOperator<CacheEntry> overwriteIfSameVersions() {
            return (oldValue, newValue) -> compareAsdcComponentVersions(newValue.getComponentVersion(), oldValue.getComponentVersion()) ? newValue
                : oldValue;
        }
    }

    /**
     * Entry stored by the cache
     */
    @EqualsAndHashCode
    public static final class CacheEntry {

        final String id;
        final String fileName;
        final Component component;

        CacheEntry(String id, String fileName, Component component) {
            this.id = id;
            this.fileName = fileName;
            this.component = component;
        }

        public String getComponentVersion() {
            return component.getVersion();
        }
    }
}
