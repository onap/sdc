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

package org.openecomp.sdc.be.facade.operations;

import org.openecomp.sdc.be.catalog.api.IComponentMessage;
import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.catalog.impl.ComponentMessage;
import org.openecomp.sdc.be.catalog.impl.DmaapProducer;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CatalogUpdateTimestamp;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.Arrays;
import java.util.List;


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
