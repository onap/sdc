package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import fj.data.Either;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;

import javax.annotation.Resource;
import java.util.List;

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
        return elementOperationMigration.getCategory(NodeTypeEnum.ServiceNewCategory, node.getUniqueId());
    }

    @Override
    Either<CategoryDefinition, ActionStatus> save(CategoryDefinition graphNode) {
        return elementOperationMigration.createCategory(graphNode, NodeTypeEnum.ServiceNewCategory);
    }

    @Override
    ActionStatus getNotFoundErrorStatus() {
        return ActionStatus.COMPONENT_CATEGORY_NOT_FOUND;
    }
}
