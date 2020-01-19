package org.openecomp.sdc.be.components.validation.service;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;

import java.util.List;

@org.springframework.stereotype.Component
public class ServiceEnvironmentContextValidator implements ServiceFieldValidator {
    @Override
    public void validateAndCorrectField(User user, Service service, AuditingActionEnum actionEnum) {
        String environmentContext = service.getEnvironmentContext();
        if (environmentContext == null) {
            setDefaultEnvironmentContextFromConfiguration(service);
            return;
        }
        List<String> environmentContextValidValues =
                ConfigurationManager.getConfigurationManager().getConfiguration().getEnvironmentContext().getValidValues();
        if (!environmentContextValidValues.contains(environmentContext))
            throw new ByActionStatusComponentException(ActionStatus.INVALID_ENVIRONMENT_CONTEXT, environmentContext);
    }

    private void setDefaultEnvironmentContextFromConfiguration(Service service) {
        String defaultEnvironmentContext =
                ConfigurationManager.getConfigurationManager().getConfiguration().getEnvironmentContext().getDefaultValue();
        service.setEnvironmentContext(defaultEnvironmentContext);
    }
}
