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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.category.MetadataKeyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;

class CategoryHierarchyCacheTest {

    private final AtomicLong now = new AtomicLong();
    private final AtomicInteger ttlInSeconds = new AtomicInteger(60);
    private final AtomicInteger loads = new AtomicInteger();
    private CategoryHierarchyCache cache;

    @BeforeEach
    void setUp() {
        cache = new CategoryHierarchyCache(ttlInSeconds::get, now::get);
    }

    @Test
    void secondGetIsServedFromCache() {
        get(NodeTypeEnum.ResourceNewCategory, true);
        final Either<List<CategoryDefinition>, ActionStatus> second = get(NodeTypeEnum.ResourceNewCategory, true);

        assertEquals(1, loads.get());
        assertEquals("cat", second.left().value().get(0).getName());
    }

    @Test
    void nodeTypesAreCachedSeparately() {
        get(NodeTypeEnum.ResourceNewCategory, true);
        get(NodeTypeEnum.ServiceNewCategory, true);
        get(NodeTypeEnum.ResourceNewCategory, true);
        get(NodeTypeEnum.ServiceNewCategory, true);

        assertEquals(2, loads.get());
    }

    @Test
    void entryExpiresAfterTtl() {
        get(NodeTypeEnum.ResourceNewCategory, true);
        now.addAndGet(TimeUnit.SECONDS.toNanos(59));
        get(NodeTypeEnum.ResourceNewCategory, true);
        assertEquals(1, loads.get());

        now.addAndGet(TimeUnit.SECONDS.toNanos(1));
        get(NodeTypeEnum.ResourceNewCategory, true);
        assertEquals(2, loads.get());
    }

    @Test
    void zeroTtlDisablesCaching() {
        ttlInSeconds.set(0);
        get(NodeTypeEnum.ResourceNewCategory, true);
        get(NodeTypeEnum.ResourceNewCategory, true);

        assertEquals(2, loads.get());
    }

    @Test
    void negativeTtlDisablesCaching() {
        ttlInSeconds.set(-1);
        get(NodeTypeEnum.ResourceNewCategory, true);
        get(NodeTypeEnum.ResourceNewCategory, true);

        assertEquals(2, loads.get());
    }

    @Test
    void invalidateForcesReload() {
        get(NodeTypeEnum.ResourceNewCategory, true);
        cache.invalidate();
        get(NodeTypeEnum.ResourceNewCategory, true);

        assertEquals(2, loads.get());
    }

    @Test
    void failureIsNotCached() {
        final Either<List<CategoryDefinition>, ActionStatus> failed = cache.get(NodeTypeEnum.ResourceNewCategory, true, () -> {
            loads.incrementAndGet();
            return Either.right(ActionStatus.GENERAL_ERROR);
        });
        assertEquals(ActionStatus.GENERAL_ERROR, failed.right().value());

        assertTrue(get(NodeTypeEnum.ResourceNewCategory, true).isLeft());
        assertEquals(2, loads.get());
    }

    @Test
    void loadIsNotStoredWhenNotRequested() {
        get(NodeTypeEnum.ResourceNewCategory, false);
        get(NodeTypeEnum.ResourceNewCategory, false);
        assertEquals(2, loads.get());

        get(NodeTypeEnum.ResourceNewCategory, true);
        get(NodeTypeEnum.ResourceNewCategory, false);
        assertEquals(3, loads.get());
    }

    @Test
    void loadRacingAnInvalidationIsNotStored() {
        cache.get(NodeTypeEnum.ResourceNewCategory, true, () -> {
            loads.incrementAndGet();
            final Either<List<CategoryDefinition>, ActionStatus> staleRead = Either.left(hierarchy());
            cache.invalidate();
            return staleRead;
        });
        get(NodeTypeEnum.ResourceNewCategory, true);

        assertEquals(2, loads.get());
    }

    @Test
    void mutatingTheLoadedResultDoesNotReachTheCache() {
        final List<CategoryDefinition> loaded = get(NodeTypeEnum.ResourceNewCategory, true).left().value();
        mutate(loaded);

        assertUntouched(get(NodeTypeEnum.ResourceNewCategory, true).left().value());
    }

    @Test
    void mutatingACachedResultDoesNotReachTheCache() {
        get(NodeTypeEnum.ResourceNewCategory, true);
        final List<CategoryDefinition> cached = get(NodeTypeEnum.ResourceNewCategory, true).left().value();
        mutate(cached);

        assertUntouched(get(NodeTypeEnum.ResourceNewCategory, true).left().value());
        assertEquals(1, loads.get());
    }

    @Test
    void copyKeepsNullListsNull() {
        final CategoryDefinition category = new CategoryDefinition();
        category.setName("bare");

        final CategoryDefinition copy = CategoryHierarchyCache.copy(List.of(category)).get(0);

        assertEquals("bare", copy.getName());
        assertNull(copy.getSubcategories());
        assertNull(copy.getModels());
        assertNull(copy.getMetadataKeys());
        assertNull(CategoryHierarchyCache.copy(null));
    }

    @Test
    void copyIsDeep() {
        final List<CategoryDefinition> original = hierarchy();
        final CategoryDefinition copy = CategoryHierarchyCache.copy(original).get(0);
        final CategoryDefinition source = original.get(0);

        assertEquals(source, copy);
        assertNotSame(source, copy);
        assertNotSame(source.getModels(), copy.getModels());
        assertNotSame(source.getMetadataKeys().get(0), copy.getMetadataKeys().get(0));
        assertNotSame(source.getSubcategories().get(0), copy.getSubcategories().get(0));
        assertNotSame(source.getSubcategories().get(0).getGroupings().get(0), copy.getSubcategories().get(0).getGroupings().get(0));
    }

    private Either<List<CategoryDefinition>, ActionStatus> get(final NodeTypeEnum nodeType, final boolean storeLoaded) {
        final Supplier<Either<List<CategoryDefinition>, ActionStatus>> loader = () -> {
            loads.incrementAndGet();
            return Either.left(hierarchy());
        };
        return cache.get(nodeType, storeLoaded, loader);
    }

    private static List<CategoryDefinition> hierarchy() {
        final GroupingDefinition grouping = new GroupingDefinition();
        grouping.setName("grp");
        final MetadataKeyDataDefinition metadataKey = new MetadataKeyDataDefinition();
        metadataKey.setName("key");
        metadataKey.setValidValues(new ArrayList<>(List.of("a", "b")));
        final SubCategoryDefinition subCategory = new SubCategoryDefinition();
        subCategory.setName("sub");
        subCategory.setIcons(new ArrayList<>(List.of("icon")));
        subCategory.addGrouping(grouping);
        final CategoryDefinition category = new CategoryDefinition();
        category.setName("cat");
        category.setModels(new ArrayList<>(List.of("model")));
        category.setMetadataKeys(new ArrayList<>(List.of(metadataKey)));
        category.addSubCategory(subCategory);
        final List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        return categories;
    }

    private static void mutate(final List<CategoryDefinition> categories) {
        final CategoryDefinition category = categories.get(0);
        category.setName("changed");
        category.getModels().clear();
        category.getMetadataKeys().get(0).getValidValues().clear();
        final SubCategoryDefinition subCategory = category.getSubcategories().get(0);
        subCategory.setName("changed");
        subCategory.getIcons().clear();
        subCategory.getGroupings().get(0).setName("changed");
        category.getSubcategories().clear();
        categories.add(new CategoryDefinition());
    }

    private static void assertUntouched(final List<CategoryDefinition> categories) {
        assertEquals(hierarchy(), categories);
    }
}
