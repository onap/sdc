package org.openecomp.sdc.be.auditing.impl.category;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditAddCategoryEventFactory extends AuditCategoryEventFactory {

    public AuditAddCategoryEventFactory(CommonAuditData commonFields, User modifier,
                                        String categoryName, String subCategoryName, String groupingName, String resourceType) {
        super(AuditingActionEnum.ADD_CATEGORY, commonFields, modifier, categoryName, subCategoryName, groupingName, resourceType);
    }
}
