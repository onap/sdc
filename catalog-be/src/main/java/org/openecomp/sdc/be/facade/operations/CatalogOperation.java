package org.openecomp.sdc.be.facade.operations;

import java.util.Arrays;
import java.util.List;

import org.openecomp.sdc.be.catalog.api.IComponentMessage;
import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.catalog.impl.ComponentMessage;
import org.openecomp.sdc.be.catalog.impl.DmaapProducer;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.CatalogUpdateTimestamp;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;


@org.springframework.stereotype.Component
public class CatalogOperation {
    
    private static final Logger log = Logger.getLogger(CatalogOperation.class); 
    
    private static final List<ResourceTypeEnum> EXCLUDE_TYPES = Arrays.asList(ResourceTypeEnum.VFCMT, ResourceTypeEnum.Configuration);
    
    private final DmaapProducer msProducer;
    
    public CatalogOperation(DmaapProducer msProducer){
        this.msProducer = msProducer;
    }
    
    public ActionStatus updateCatalog(ChangeTypeEnum changeTypeEnum, Component component){
        ActionStatus result = ActionStatus.OK;
       try{
            if(isNeedToUpdateCatalog(component)){
                IComponentMessage message = new ComponentMessage(component, changeTypeEnum, CatalogUpdateTimestamp.buildDummyCatalogUpdateTimestamp());
                IStatus status = msProducer.pushMessage(message);
                result = FacadeOperationUtils.convertStatusToActionStatus(status);
            }
           
       }catch(Exception e){
           log.debug("updateCatalog - failed to updateCatalog and send notification {}", e.getMessage());
           return ActionStatus.OK;
       }
        return result;
    }
    
    private boolean isNeedToUpdateCatalog(Component component) {
          boolean isUpdateCatalog = true;
             if(component.getComponentType() == ComponentTypeEnum.RESOURCE){
                 return ((Resource)component).isAbstract() || EXCLUDE_TYPES.contains(((Resource)component).getResourceType())? false : true;
               
              }
            return isUpdateCatalog;
     }


    public DmaapProducer getMsProducer() {
        return msProducer;
    }
    
}
