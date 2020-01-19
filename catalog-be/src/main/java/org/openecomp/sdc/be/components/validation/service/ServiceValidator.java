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

import org.openecomp.sdc.be.components.validation.component.ComponentFieldValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentValidator;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceValidator extends ComponentValidator {

    private List<ServiceFieldValidator> serviceFieldValidators;

    public ServiceValidator(ComponentsUtils componentsUtils, List<ComponentFieldValidator> componentFieldValidators, List<ServiceFieldValidator> serviceFieldValidators) {
        super(componentsUtils, componentFieldValidators);
        this.serviceFieldValidators = serviceFieldValidators;
    }

    @Override
    public void validate(User user, org.openecomp.sdc.be.model.Component component, AuditingActionEnum actionEnum) {
        super.validate(user, component, actionEnum);
        serviceFieldValidators.forEach(validator ->
                validator.validateAndCorrectField(user,(Service)component,actionEnum));
    }
}
