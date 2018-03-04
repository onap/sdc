package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.merge.capability.CapabilityResolver;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
public class ComponentCapabilitiesPropertiesMergeBL implements ComponentsMergeCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentCapabilitiesPropertiesMergeBL.class);

    private DataDefinitionsValuesMergingBusinessLogic dataDefinitionsValuesMergingBusinessLogic;
    private ToscaOperationFacade toscaOperationFacade;
    private ComponentsUtils componentsUtils;
    private CapabilityResolver capabilityResolver;

    public ComponentCapabilitiesPropertiesMergeBL(DataDefinitionsValuesMergingBusinessLogic dataDefinitionsValuesMergingBusinessLogic, ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils, CapabilityResolver capabilityResolver) {
        this.dataDefinitionsValuesMergingBusinessLogic = dataDefinitionsValuesMergingBusinessLogic;
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.capabilityResolver = capabilityResolver;
    }

    @Override
    public String description() {
        return "merge component instances capabilities properties";
    }

    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        StorageOperationStatus mergeStatus = getCmptWithCapabilitiesProps(currentComponent.getUniqueId())
                .either(currCmptWithCap -> mergeCmptCalculatedCapabilitiesProperties(prevComponent, currCmptWithCap),
                        err -> err);
        return componentsUtils.convertFromStorageResponse(mergeStatus);
    }

    public ActionStatus mergeComponentInstanceCapabilities(Component currentComponent, Component origInstanceCmpt, String instanceId, List<CapabilityDefinition> prevInstanceCapabilities) {
        if (CollectionUtils.isEmpty(prevInstanceCapabilities)) {
            return ActionStatus.OK;
        }
        Map<CapabilityDefinition, CapabilityDefinition> oldToNewCap = capabilityResolver.resolvePrevCapToNewCapability(currentComponent, origInstanceCmpt, instanceId, prevInstanceCapabilities);
        oldToNewCap.forEach(this::mergeCapabilityProperties);
        StorageOperationStatus updateStatus = updateInstanceCapabilitiesProperties(currentComponent, instanceId);
        return componentsUtils.convertFromStorageResponse(updateStatus);
    }

    private StorageOperationStatus mergeCmptCalculatedCapabilitiesProperties(Component prevComponent, Component currentComponent) {
        List<ComponentInstance> prevInstances = prevComponent.getComponentInstances();
        if (prevInstances == null) {
            return StorageOperationStatus.OK;
        }
        prevInstances.forEach(prevInstance -> mergeInstanceCapabilities(prevInstance, currentComponent));
        return updateComponentCapabilitiesProperties(currentComponent);
    }

    private void mergeInstanceCapabilities(ComponentInstance prevInstance, Component currComponent) {
        ComponentInstance currInstance = MapUtil.toMap(currComponent.getComponentInstances(), ComponentInstance::getName).get(prevInstance.getName());
        Map<CapabilityDefinition, CapabilityDefinition> oldToNewCapabilities = capabilityResolver.resolvePrevCapIdToNewCapability(prevInstance, currInstance);
        oldToNewCapabilities.forEach(this::mergeCapabilityProperties);
    }

    private void mergeCapabilityProperties(CapabilityDefinition prevCapability, CapabilityDefinition currCapability) {
        dataDefinitionsValuesMergingBusinessLogic.mergeInstanceDataDefinitions(prevCapability.getProperties(), currCapability.getProperties());
    }

    private StorageOperationStatus updateComponentCapabilitiesProperties(Component currComponent) {
        return toscaOperationFacade.updateComponentCalculatedCapabilitiesProperties(currComponent);
    }

    private StorageOperationStatus updateInstanceCapabilitiesProperties(Component currComponent, String instanceId) {
        return toscaOperationFacade.updateComponentInstanceCapabilityProperties(currComponent, instanceId);
    }

    private Either<Component, StorageOperationStatus> getCmptWithCapabilitiesProps(String cmptId) {
        ComponentParametersView propertiesCapabilitiesFilter = new ComponentParametersView(true);
        propertiesCapabilitiesFilter.setIgnoreCapabiltyProperties(false);
        propertiesCapabilitiesFilter.setIgnoreComponentInstances(false);
        propertiesCapabilitiesFilter.setIgnoreCapabilities(false);
        return toscaOperationFacade.getToscaElement(cmptId, propertiesCapabilitiesFilter)
                .right()
                .map(err -> {
                   LOGGER.debug("failed to fetch cmpt {} with properties capabilities. status: {}", cmptId, err);
                   return err;
                });

    }

}
