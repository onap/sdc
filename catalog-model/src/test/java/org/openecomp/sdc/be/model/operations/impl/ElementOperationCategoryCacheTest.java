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
package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fj.data.Either;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.category.CategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.category.SubCategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;

class ElementOperationCategoryCacheTest extends ModelTestBase {

    private JanusGraphGenericDao janusGraphGenericDao;
    private ElementOperation elementOperation;
    private Integer configuredTtl;

    @BeforeAll
    static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @BeforeEach
    void setUp() {
        configuredTtl = configurationManager.getConfiguration().getCategoriesCacheTtlInSeconds();
        janusGraphGenericDao = mock(JanusGraphGenericDao.class);
        elementOperation = new ElementOperation(janusGraphGenericDao, mock(HealingJanusGraphDao.class));

        final CategoryDataDefinition category = new CategoryDataDefinition();
        category.setName("Network L2-3");
        category.setUniqueId("resourceNewCategory.network l2-3");
        doReturn(Either.left(List.of(new CategoryData(NodeTypeEnum.ResourceNewCategory, category))))
            .when(janusGraphGenericDao).getAll(NodeTypeEnum.ResourceNewCategory, CategoryData.class);
        final SubCategoryDataDefinition subCategory = new SubCategoryDataDefinition();
        subCategory.setName("Router");
        doReturn(Either.left(List.of(ImmutablePair.of(new SubCategoryData(NodeTypeEnum.ResourceSubcategory, subCategory), new GraphEdge()))))
            .when(janusGraphGenericDao).getChildrenNodes(anyString(), eq(category.getUniqueId()), eq(GraphEdgeLabels.SUB_CATEGORY),
                eq(NodeTypeEnum.ResourceSubcategory), eq(SubCategoryData.class));

        final JanusGraphOperationStatus failure = JanusGraphOperationStatus.GENERAL_ERROR;
        doReturn(Either.right(failure)).when(janusGraphGenericDao).createNode(any(), any());
        doReturn(Either.right(failure)).when(janusGraphGenericDao).updateNode(any(), any());
        doReturn(Either.right(failure)).when(janusGraphGenericDao).getNode(anyString(), any(), any());
    }

    @AfterEach
    void restoreTtl() {
        configurationManager.getConfiguration().setCategoriesCacheTtlInSeconds(configuredTtl);
    }

    @Test
    void secondReadIsServedFromCacheAndStillCommits() {
        final Either<List<CategoryDefinition>, ActionStatus> first = elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, false);
        final Either<List<CategoryDefinition>, ActionStatus> second = elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, false);

        assertEquals(first.left().value(), second.left().value());
        assertEquals("Router", second.left().value().get(0).getSubcategories().get(0).getName());
        verify(janusGraphGenericDao, times(1)).getAll(NodeTypeEnum.ResourceNewCategory, CategoryData.class);
        verify(janusGraphGenericDao, times(2)).commit();
    }

    @Test
    void legacyGetterSharesTheCache() {
        elementOperation.getAllResourceCategories();
        elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, false);

        verify(janusGraphGenericDao, times(1)).getAll(NodeTypeEnum.ResourceNewCategory, CategoryData.class);
    }

    @Test
    void mutatingAResultDoesNotAffectTheNextRead() {
        final CategoryDefinition first = elementOperation.getAllResourceCategories().left().value().get(0);
        first.setSubcategories(List.of());
        first.setName("changed");

        final CategoryDefinition second = elementOperation.getAllResourceCategories().left().value().get(0);

        assertEquals("Network L2-3", second.getName());
        assertEquals(1, second.getSubcategories().size());
    }

    @Test
    void zeroTtlReadsTheGraphEveryTime() {
        configurationManager.getConfiguration().setCategoriesCacheTtlInSeconds(0);

        elementOperation.getAllResourceCategories();
        elementOperation.getAllResourceCategories();

        verify(janusGraphGenericDao, times(2)).getAll(NodeTypeEnum.ResourceNewCategory, CategoryData.class);
    }

    @Test
    void failedReadIsNotCached() {
        doReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR))
            .when(janusGraphGenericDao).getAll(NodeTypeEnum.ServiceNewCategory, CategoryData.class);

        assertEquals(ActionStatus.GENERAL_ERROR, elementOperation.getAllServiceCategories().right().value());
        assertEquals(ActionStatus.GENERAL_ERROR, elementOperation.getAllServiceCategories().right().value());

        verify(janusGraphGenericDao, times(2)).getAll(NodeTypeEnum.ServiceNewCategory, CategoryData.class);
    }

    @Test
    void readInsideCallerTransactionIsServedButNotStored() {
        elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, true);
        elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, true);
        verify(janusGraphGenericDao, times(2)).getAll(NodeTypeEnum.ResourceNewCategory, CategoryData.class);
        verify(janusGraphGenericDao, never()).commit();

        elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, false);
        final Either<List<CategoryDefinition>, ActionStatus> cached = elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, true);

        assertTrue(cached.isLeft());
        verify(janusGraphGenericDao, times(3)).getAll(NodeTypeEnum.ResourceNewCategory, CategoryData.class);
    }

    static Stream<Arguments> writes() {
        return Stream.of(
            write("createCategory", op -> op.createCategory(category(), NodeTypeEnum.ResourceNewCategory)),
            write("createCategory in transaction", op -> op.createCategory(category(), NodeTypeEnum.ResourceNewCategory, true)),
            write("updateCategory", op -> op.updateCategory(category(), NodeTypeEnum.ResourceNewCategory)),
            write("updateCategory in transaction", op -> op.updateCategory(category(), NodeTypeEnum.ResourceNewCategory, true)),
            write("deleteCategory", op -> op.deleteCategory(NodeTypeEnum.ResourceNewCategory, "id")),
            write("createSubCategory", op -> op.createSubCategory("id", subCategory(), NodeTypeEnum.ResourceSubcategory)),
            write("createSubCategory in transaction", op -> op.createSubCategory("id", subCategory(), NodeTypeEnum.ResourceSubcategory, true)),
            write("updateSubCategory", op -> op.updateSubCategory("id", subCategory(), NodeTypeEnum.ResourceSubcategory)),
            write("updateSubCategory in transaction", op -> op.updateSubCategory("id", subCategory(), NodeTypeEnum.ResourceSubcategory, true)),
            write("deleteSubCategory", op -> op.deleteSubCategory(NodeTypeEnum.ResourceSubcategory, "id")),
            write("createGrouping", op -> op.createGrouping("id", new GroupingDefinition(), NodeTypeEnum.ProductGrouping)),
            write("deleteGrouping", op -> op.deleteGrouping(NodeTypeEnum.ProductGrouping, "id"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writes")
    void everyWriteInvalidatesTheCache(final String name, final Consumer<ElementOperation> write) {
        elementOperation.getAllResourceCategories();
        write.accept(elementOperation);
        elementOperation.getAllResourceCategories();

        verify(janusGraphGenericDao, times(2)).getAll(NodeTypeEnum.ResourceNewCategory, CategoryData.class);
    }

    private static Arguments write(final String name, final Consumer<ElementOperation> write) {
        return Arguments.of(name, write);
    }

    private static CategoryDefinition category() {
        final CategoryDefinition category = new CategoryDefinition();
        category.setName("New");
        category.setNormalizedName("new");
        return category;
    }

    private static SubCategoryDefinition subCategory() {
        final SubCategoryDefinition subCategory = new SubCategoryDefinition();
        subCategory.setName("New");
        subCategory.setNormalizedName("new");
        return subCategory;
    }
}
