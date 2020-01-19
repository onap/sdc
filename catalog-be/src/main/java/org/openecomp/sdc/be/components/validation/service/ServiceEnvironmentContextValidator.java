/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.validation.service;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
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
