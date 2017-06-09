package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;

import java.util.List;
import java.util.stream.Collectors;

public class CategoriesUtils {

    public static final String OLD_CATEGORY_ID_SUB_STR = "layer";

    public static List<CategoryDefinition> filterOldCategories(List<CategoryDefinition> categoryDefinitions) {
        return categoryDefinitions.stream()
                .filter(categoryDefinition -> !categoryDefinition.getUniqueId().contains(OLD_CATEGORY_ID_SUB_STR))
                .collect(Collectors.toList());
    }

    public static List<SubCategoryDefinition> filterOldSubCategories(List<SubCategoryDefinition> categoryDefinitions) {
        return categoryDefinitions.stream()
                .filter(categoryDefinition -> !categoryDefinition.getUniqueId().contains(OLD_CATEGORY_ID_SUB_STR))
                .collect(Collectors.toList());
    }

}
