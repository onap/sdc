package org.openecomp.sdc.be.catalog.api;

import org.openecomp.sdc.be.catalog.enums.ResultStatusEnum;

//import org.onap.sdc.catalogms.enums.ResultStatusEnum;

@FunctionalInterface
public interface IStatus {
    
    static IStatus getSuccessStatus() {
        
        return () -> ResultStatusEnum.SUCCESS;
    }

    static IStatus getFailStatus() {
        return () -> ResultStatusEnum.FAIL;
    }

    static IStatus getServiceDisabled() {
        return () -> ResultStatusEnum.SERVICE_DISABLED;
    }

    ResultStatusEnum getResultStatus();

}