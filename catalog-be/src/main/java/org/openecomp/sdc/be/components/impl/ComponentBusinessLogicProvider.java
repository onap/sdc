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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class ComponentBusinessLogicProvider {

    private final ResourceBusinessLogic resourceBusinessLogic;
    private final ServiceBusinessLogic serviceBusinessLogic;
    private final ProductBusinessLogic productBusinessLogic;

    public ComponentBusinessLogicProvider(ResourceBusinessLogic resourceBusinessLogic, ServiceBusinessLogic serviceBusinessLogic, ProductBusinessLogic productBusinessLogic) {
        this.resourceBusinessLogic = resourceBusinessLogic;
        this.serviceBusinessLogic = serviceBusinessLogic;
        this.productBusinessLogic = productBusinessLogic;
    }

    public ComponentBusinessLogic getInstance(ComponentTypeEnum componentTypeEnum) {
        switch (componentTypeEnum) {
            case SERVICE:
               return serviceBusinessLogic;
            case PRODUCT:
               return productBusinessLogic;
            case RESOURCE:
            case RESOURCE_INSTANCE:
               return resourceBusinessLogic;
            default:
                BeEcompErrorManager.getInstance().logBeSystemError("getComponentBL");
                throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT_PARAM, componentTypeEnum.getValue());
        }
    }

}
