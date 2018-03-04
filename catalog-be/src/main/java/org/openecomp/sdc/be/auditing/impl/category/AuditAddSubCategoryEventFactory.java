package org.openecomp.sdc.be.auditing.impl.category;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditAddSubCategoryEventFactory extends AuditCategoryEventFactory {

    public AuditAddSubCategoryEventFactory(CommonAuditData commonFields, User modifier,
                                           String categoryName, String subCategoryName, String groupingName, String resourceType) {
        super(AuditingActionEnum.ADD_SUB_CATEGORY, commonFields, modifier, categoryName, subCategoryName, groupingName, resourceType);
    }
}
