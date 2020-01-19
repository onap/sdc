package org.openecomp.sdc.be.components.validation.service;

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;


public interface ServiceFieldValidator {
    void validateAndCorrectField(User user, Service service, AuditingActionEnum actionEnum);
}
