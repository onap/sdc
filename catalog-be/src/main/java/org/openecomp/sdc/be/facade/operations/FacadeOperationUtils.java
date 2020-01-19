package org.openecomp.sdc.be.facade.operations;

import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.enums.ResultStatusEnum;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class FacadeOperationUtils {
    private static final Logger log = Logger.getLogger(CatalogOperation.class); 
    
    private FacadeOperationUtils() {
    }

    public static ActionStatus convertStatusToActionStatus(IStatus status) {
        ActionStatus result = ActionStatus.OK;
        if (status.getResultStatus() != ResultStatusEnum.SUCCESS){
            log.debug("updateCatalog - failed to  send notification {}", status);
        }
        return result;
    }
}
