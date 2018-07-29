package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
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
                throw new ComponentException(ActionStatus.INVALID_CONTENT_PARAM, componentTypeEnum.getValue());
        }
    }

}
