package org.openecomp.sdc.be.components.merge;

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.exception.SdcActionException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component
public class TopologyComparator {

    public static final Logger LOGGER = LoggerFactory.getLogger(TopologyComparator.class);

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
        Map<String, ComponentInstance> oldInstancesByName = MapUtil.toMap(oldInstances, ComponentInstance::getName);
        Map<String, ComponentInstance> newInstancesByName = MapUtil.toMap(newInstances, ComponentInstance::getName);
        return isTopologyInstancesChanged(oldResource, newResource, oldInstancesByName, newInstancesByName);
    }

    private Either<Boolean, ActionStatus> isTopologyInstancesChanged(Resource oldResource, Resource newResource, Map<String, ComponentInstance> oldInstancesByName, Map<String, ComponentInstance> newInstancesByName) {
        try {
            boolean isTopologyChanged = isInstanceNamesChanged(oldInstancesByName, newInstancesByName) ||
                                        isInstanceTypesChanged(oldInstancesByName, newInstancesByName) ||
                                        relationsComparator.isRelationsChanged(oldResource, newResource);
            return Either.left(isTopologyChanged);
        } catch (SdcActionException e) {
            LOGGER.error("failed to merge entities of previous resource %s to current resource %s. reason: %s", oldResource.getUniqueId(), newResource.getUniqueId(), e.getActionStatus(), e);
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
        LOGGER.error("failed to fetch origin node type %s for instance %s", cmptInstance.getUniqueId(), cmptInstance.getComponentUid());
        throw new SdcActionException(componentsUtils.convertFromStorageResponse(storageOperationStatus));
    }


}
