package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

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
    public Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Component origInstanceNode = dataHolder.getOrigInstanceNode();
        List<CapabilityDefinition> origInstanceCapabilities = dataHolder.getOrigInstanceCapabilities();
        ActionStatus mergeStatus = capabilitiesPropertiesMergeBL.mergeComponentInstanceCapabilities(updatedContainerComponent, origInstanceNode, newInstanceId, origInstanceCapabilities);
        return Either.iif(!ActionStatus.OK.equals(mergeStatus), () -> componentsUtils.getResponseFormat(mergeStatus), () -> updatedContainerComponent);
    }

    private List<CapabilityDefinition> getAllInstanceCapabilities(ComponentInstance currentResourceInstance) {
        return isNotEmpty( currentResourceInstance.getCapabilities() )  ? currentResourceInstance.getCapabilities().values().stream().flatMap(Collection::stream).collect(Collectors.toList()) :  new ArrayList<>() ;
    }
}
