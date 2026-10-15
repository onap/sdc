/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.openecomp.sdc.be.model.cache;

import fj.data.Either;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.category.MetadataKeyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;

/**
 * Caches the category -> sub-category -> grouping hierarchy per category node type.
 * <p>
 * Entries expire after {@code categoriesCacheTtlInSeconds} (0 disables the cache), because a write handled by another sdc-be replica does not
 * reach {@link #invalidate()} here. Callers always get their own deep copy, so they may mutate the result freely.
 */
public class CategoryHierarchyCache {

    static final int DEFAULT_TTL_IN_SECONDS = 3600;

    private final IntSupplier ttlInSeconds;
    private final LongSupplier nanoClock;
    private final Map<NodeTypeEnum, Entry> entries = new EnumMap<>(NodeTypeEnum.class);
    private long generation;

    public CategoryHierarchyCache() {
        this(CategoryHierarchyCache::configuredTtlInSeconds, System::nanoTime);
    }

    CategoryHierarchyCache(final IntSupplier ttlInSeconds, final LongSupplier nanoClock) {
        this.ttlInSeconds = ttlInSeconds;
        this.nanoClock = nanoClock;
    }

    /**
     * Returns the cached hierarchy for {@code nodeType}, or runs {@code loader}. A successful load is stored only if {@code storeLoaded} is set
     * and no {@link #invalidate()} happened while it ran.
     */
    public Either<List<CategoryDefinition>, ActionStatus> get(final NodeTypeEnum nodeType, final boolean storeLoaded,
                                                              final Supplier<Either<List<CategoryDefinition>, ActionStatus>> loader) {
        final long ttlNanos = TimeUnit.SECONDS.toNanos(Math.max(0, ttlInSeconds.getAsInt()));
        if (ttlNanos == 0) {
            return loader.get();
        }
        final long loadGeneration;
        synchronized (this) {
            final Entry entry = entries.get(nodeType);
            if (entry != null && nanoClock.getAsLong() - entry.loadStartedAt < ttlNanos) {
                return Either.left(copy(entry.categories));
            }
            loadGeneration = generation;
        }
        final long loadStartedAt = nanoClock.getAsLong();
        final Either<List<CategoryDefinition>, ActionStatus> loaded = loader.get();
        if (storeLoaded && loaded.isLeft()) {
            final List<CategoryDefinition> snapshot = copy(loaded.left().value());
            synchronized (this) {
                if (generation == loadGeneration) {
                    entries.put(nodeType, new Entry(snapshot, loadStartedAt));
                }
            }
        }
        return loaded;
    }

    public synchronized void invalidate() {
        generation++;
        entries.clear();
    }

    private static int configuredTtlInSeconds() {
        final ConfigurationManager configurationManager = ConfigurationManager.getConfigurationManager();
        final Configuration configuration = configurationManager == null ? null : configurationManager.getConfiguration();
        final Integer ttl = configuration == null ? null : configuration.getCategoriesCacheTtlInSeconds();
        return ttl == null ? DEFAULT_TTL_IN_SECONDS : ttl;
    }

    static List<CategoryDefinition> copy(final List<CategoryDefinition> categories) {
        if (categories == null) {
            return null;
        }
        final List<CategoryDefinition> copies = new ArrayList<>(categories.size());
        for (final CategoryDefinition category : categories) {
            final CategoryDefinition copy = new CategoryDefinition(category);
            copy.setModels(copyList(category.getModels()));
            copy.setIcons(copyList(category.getIcons()));
            copy.setMetadataKeys(copyMetadataKeys(category.getMetadataKeys()));
            copy.setNotApplicableMetadataKeys(copyList(category.getNotApplicableMetadataKeys()));
            copy.setSubcategories(copySubCategories(category.getSubcategories()));
            copies.add(copy);
        }
        return copies;
    }

    private static List<SubCategoryDefinition> copySubCategories(final List<SubCategoryDefinition> subCategories) {
        if (subCategories == null) {
            return null;
        }
        final List<SubCategoryDefinition> copies = new ArrayList<>(subCategories.size());
        for (final SubCategoryDefinition subCategory : subCategories) {
            final SubCategoryDefinition copy = new SubCategoryDefinition(subCategory);
            copy.setIcons(copyList(subCategory.getIcons()));
            copy.setMetadataKeys(copyMetadataKeys(subCategory.getMetadataKeys()));
            copy.setGroupings(copyGroupings(subCategory.getGroupings()));
            copies.add(copy);
        }
        return copies;
    }

    private static List<GroupingDefinition> copyGroupings(final List<GroupingDefinition> groupings) {
        if (groupings == null) {
            return null;
        }
        final List<GroupingDefinition> copies = new ArrayList<>(groupings.size());
        groupings.forEach(grouping -> copies.add(new GroupingDefinition(grouping)));
        return copies;
    }

    private static List<MetadataKeyDataDefinition> copyMetadataKeys(final List<MetadataKeyDataDefinition> metadataKeys) {
        if (metadataKeys == null) {
            return null;
        }
        final List<MetadataKeyDataDefinition> copies = new ArrayList<>(metadataKeys.size());
        for (final MetadataKeyDataDefinition metadataKey : metadataKeys) {
            final MetadataKeyDataDefinition copy = new MetadataKeyDataDefinition();
            copy.setName(metadataKey.getName());
            copy.setDisplayName(metadataKey.getDisplayName());
            copy.setValidValues(copyList(metadataKey.getValidValues()));
            copy.setMandatory(metadataKey.isMandatory());
            copy.setDefaultValue(metadataKey.getDefaultValue());
            copies.add(copy);
        }
        return copies;
    }

    private static <T> List<T> copyList(final List<T> list) {
        return list == null ? null : new ArrayList<>(list);
    }

    private static final class Entry {

        private final List<CategoryDefinition> categories;
        private final long loadStartedAt;

        private Entry(final List<CategoryDefinition> categories, final long loadStartedAt) {
            this.categories = categories;
            this.loadStartedAt = loadStartedAt;
        }
    }
}
