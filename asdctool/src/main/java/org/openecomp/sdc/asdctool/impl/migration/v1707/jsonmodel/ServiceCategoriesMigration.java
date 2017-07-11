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

package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;

import javax.annotation.Resource;
import java.util.List;

import static org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils.handleError;

public class ServiceCategoriesMigration extends JsonModelMigration<CategoryDefinition> {

    @Resource(name = "element-operation")
    private IElementOperation elementOperation;

    @Resource(name = "element-operation-migration")
    private IElementOperation elementOperationMigration;


    @Override
    public String description() {
        return "migrate services categories";
    }

    @Override
    Either<List<CategoryDefinition>, ?> getElementsToMigrate() {
        return elementOperation.getAllCategories(NodeTypeEnum.ServiceNewCategory, false).left().map(CategoriesUtils::filterOldCategories);
    }

    @Override
    Either<CategoryDefinition, ?> getElementFromNewGraph(CategoryDefinition node) {
        String categoryUid = UniqueIdBuilder.buildCategoryUid(node.getNormalizedName(), NodeTypeEnum.ServiceNewCategory);//in malformed graph there are some categories with different id but same normalized name. so in new graph they same id
        return elementOperationMigration.getCategory(NodeTypeEnum.ServiceNewCategory, categoryUid);
    }

    @Override
    boolean save(CategoryDefinition graphNode) {
        return elementOperationMigration.createCategory(graphNode, NodeTypeEnum.ServiceNewCategory)
                .either(savedCategory -> true,
                        err -> handleError(String.format("failed to save category %s. error: %s", graphNode.getName(), err.name())));
    }

    @Override
    ActionStatus getNotFoundErrorStatus() {
        return ActionStatus.COMPONENT_CATEGORY_NOT_FOUND;
    }
}
