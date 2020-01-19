package org.openecomp.sdc.be.facade.operations;

import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.impl.DmaapProducer;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.user.UserMessage;
import org.openecomp.sdc.be.user.UserOperationEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserOperation {
    private static final Logger log = Logger.getLogger(UserOperation.class);
    private final DmaapProducer msProducer; 
    
    @Autowired
    public UserOperation(DmaapProducer msProducer){
        this.msProducer = msProducer;
    }
    
    public ActionStatus updateUserCache(UserOperationEnum operation, String userId, String role){
       ActionStatus result = ActionStatus.OK;
       try{
            UserMessage message = new UserMessage(operation, userId,role);
            IStatus status = msProducer.pushMessage(message);
            result = FacadeOperationUtils.convertStatusToActionStatus(status);
           
       }catch(Exception e){
           log.debug("update user cache - failed to send notification to update user cache {}", e.getMessage());
           return ActionStatus.OK;
       }
        return result;
    }
    

    public DmaapProducer getMsProducer() {
        return msProducer;
    }

}
