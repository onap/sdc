package org.openecomp.sdc.be.components.validation.component;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;


public interface ComponentFieldValidator {

    void validateAndCorrectField(User user, org.openecomp.sdc.be.model.Component component, AuditingActionEnum actionEnum);
}
