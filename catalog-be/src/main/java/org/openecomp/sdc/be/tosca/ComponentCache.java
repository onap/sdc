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

public final class ComponentCache {

    // TODO: Make this final whenever possible. The current code using the class
    // does not allow this.
    private Map<String, CacheEntry> entries = HashMap.empty();
    private final BinaryOperator<CacheEntry> merge;

    private ComponentCache(BinaryOperator<CacheEntry> merge) {
        this.merge = merge;
    }

    public static ComponentCache overwritable(BinaryOperator<CacheEntry> merge) {
        return new ComponentCache(merge);
    }

    public static CacheEntry entry(String id, String fileName, Component component) {
        return new CacheEntry(id, fileName, component);
    }

    public ComponentCache onMerge(BiConsumer<CacheEntry, CacheEntry> bc) {
        return new ComponentCache((oldValue, newValue) -> {
            CacheEntry value = merge.apply(oldValue, newValue);
            if(value.equals(newValue)) {
                bc.accept(oldValue, newValue);
            }
            return value;
        });
    }

    public interface MergeStrategy {
        static BinaryOperator<CacheEntry> overwriteIfSameVersions() {
            return (oldValue, newValue) ->
                compareAsdcComponentVersions(newValue.getComponentVersion(), oldValue.getComponentVersion()) ?
                    newValue : oldValue;
        }
    }

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

    // TODO: Encapsulate the cache and expose functions to interact with it
    // For now we'll keep this as is, to prevent the refactoring to be too big
    public Iterable<ImmutableTriple<String, String, Component>> iterable() {
        return all().map(e ->
            new ImmutableTriple<>(e.id, e.fileName, e.component)
        );
    }

    public Stream<CacheEntry> all() {
        return entries.values().toStream();
    }

    public boolean notCached(String id) {
        return !entries.get(id).isDefined();
    }

    public ComponentCache put(
        String id,
        String fileName,
        Component component
    ) {
        String uuid = component.getInvariantUUID();
        CacheEntry entry = new CacheEntry(id, fileName, component);
        // TODO: Make the entries final whenever possible. The current code using the class does not allow this
        entries = entries.put(uuid, entry, merge);

        return this;
    }
}
