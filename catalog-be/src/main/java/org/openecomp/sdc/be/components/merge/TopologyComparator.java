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

package org.openecomp.sdc.be.components.merge;

import fj.data.Either;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.exception.SdcActionException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
public class TopologyComparator {

    public static final Logger log = Logger.getLogger(TopologyComparator.class);

    @javax.annotation.Resource
    private RelationsComparator relationsComparator;

    @javax.annotation.Resource
    private ToscaOperationFacade toscaOperationFacade;

    @javax.annotation.Resource
    private ComponentsUtils componentsUtils;

    /**
     *
     * @param oldResource the old version of the resource of which to check for topology change
     * @param newResource the new version of the resource of which to check for topology change
     * @return true if there was a topology change between the old resource and new resource or false otherwise
     * in case the action to find topology change failed, an appropriate {@link ActionStatus} will be returned
     */
    public Either<Boolean, ActionStatus> isTopologyChanged(Resource oldResource, Resource newResource) {
        List<ComponentInstance> oldInstances = oldResource.getComponentInstances();
        List<ComponentInstance> newInstances = newResource.getComponentInstances();
        if (oldInstances != null && newInstances == null || oldInstances == null && newInstances != null) {
            return Either.left(true);
        }
        if (oldInstances == null && newInstances == null) {
            return Either.left(false);
        }
        Map<String, ComponentInstance> oldInstancesByName = MapUtil.toMap(oldInstances, ComponentInstance::getInvariantName);
        Map<String, ComponentInstance> newInstancesByName = MapUtil.toMap(newInstances, ComponentInstance::getInvariantName);
        return isTopologyInstancesChanged(oldResource, newResource, oldInstancesByName, newInstancesByName);
    }

    private Either<Boolean, ActionStatus> isTopologyInstancesChanged(Resource oldResource, Resource newResource, Map<String, ComponentInstance> oldInstancesByName, Map<String, ComponentInstance> newInstancesByName) {
        try {
            boolean isTopologyChanged = isInstanceNamesChanged(oldInstancesByName, newInstancesByName) ||
                                        isInstanceTypesChanged(oldInstancesByName, newInstancesByName) ||
                                        relationsComparator.isRelationsChanged(oldResource, newResource);
            return Either.left(isTopologyChanged);
        } catch (SdcActionException e) {
            log.error("failed to merge entities of previous resource %s to current resource %s. reason: %s", oldResource.getUniqueId(), newResource.getUniqueId(), e.getActionStatus(), e);
            return Either.right(e.getActionStatus());
        }
    }

    private boolean isInstanceTypesChanged(Map<String, ComponentInstance> oldInstancesByName, Map<String, ComponentInstance> newInstancesByName) {
        for (Map.Entry<String, ComponentInstance> instanceByName : newInstancesByName.entrySet()) {
            ComponentInstance oldInstance = oldInstancesByName.get(instanceByName.getKey());
            if (!isSameToscaTypeOrOriginComponent(oldInstance, instanceByName.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean isInstanceNamesChanged(Map<String, ComponentInstance> oldInstanceByName, Map<String, ComponentInstance> newInstancesByName) {
        return !oldInstanceByName.keySet().equals(newInstancesByName.keySet());
    }

    private boolean isSameToscaTypeOrOriginComponent(ComponentInstance oldInstance, ComponentInstance newInstance) {
        return isSameToscaType(oldInstance, newInstance) ||
               isSameOriginComponent(oldInstance, newInstance);
    }

    private boolean isSameToscaType(ComponentInstance oldInstance, ComponentInstance newInstance) {
        return oldInstance.getToscaComponentName().equals(newInstance.getToscaComponentName());
    }

    private boolean isSameOriginComponent(ComponentInstance oldInstance, ComponentInstance newInstance) {
        if (oldInstance.getComponentUid().equals(newInstance.getComponentUid())) {
            return true;
        }
        Component oldOriginCmpt = toscaOperationFacade.getToscaElement(oldInstance.getComponentUid()).left().on(storageStatus -> throwSdcActionException(storageStatus, oldInstance));
        Component newOriginCmpt = toscaOperationFacade.getToscaElement(newInstance.getComponentUid()).left().on(storageStatus -> throwSdcActionException(storageStatus, newInstance));
        return oldOriginCmpt.getInvariantUUID().equals(newOriginCmpt.getInvariantUUID());
    }

    private Component throwSdcActionException(StorageOperationStatus storageOperationStatus, ComponentInstance cmptInstance) {
        log.error("failed to fetch origin node type %s for instance %s", cmptInstance.getUniqueId(), cmptInstance.getComponentUid());
        throw new SdcActionException(componentsUtils.convertFromStorageResponse(storageOperationStatus));
    }


}
