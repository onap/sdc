/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.merge.instance;

import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

@org.springframework.stereotype.Component
public class ComponentInstanceCapabilitiesPropertiesMerge implements ComponentInstanceMergeInterface {

    private ComponentCapabilitiesPropertiesMergeBL capabilitiesPropertiesMergeBL;
    private ComponentsUtils componentsUtils;

    public ComponentInstanceCapabilitiesPropertiesMerge(ComponentCapabilitiesPropertiesMergeBL capabilitiesPropertiesMergeBL, ComponentsUtils componentsUtils) {
        this.capabilitiesPropertiesMergeBL = capabilitiesPropertiesMergeBL;
        this.componentsUtils = componentsUtils;
    }

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent, ComponentInstance currentResourceInstance, Component originComponent) {
        dataHolder.setOrigInstanceCapabilities(getAllInstanceCapabilities(currentResourceInstance));
        dataHolder.setOrigInstanceNode(originComponent);
    }

    @Override
    public Component mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Component origInstanceNode = dataHolder.getOrigInstanceNode();
        List<CapabilityDefinition> origInstanceCapabilities = dataHolder.getOrigInstanceCapabilities();
        ActionStatus mergeStatus = capabilitiesPropertiesMergeBL.mergeComponentInstanceCapabilities(updatedContainerComponent, origInstanceNode, newInstanceId, origInstanceCapabilities);
        if(!ActionStatus.OK.equals(mergeStatus)){
            throw new ByActionStatusComponentException(mergeStatus);
        }
        return  updatedContainerComponent;
    }

    private List<CapabilityDefinition> getAllInstanceCapabilities(ComponentInstance currentResourceInstance) {
        return isNotEmpty( currentResourceInstance.getCapabilities() )  ? currentResourceInstance.getCapabilities().values().stream().flatMap(Collection::stream).collect(Collectors.toList()) :  new ArrayList<>() ;
    }
}
