package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@org.springframework.stereotype.Component
public class PropertyDecelerationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PropertyDecelerationOrchestrator.class);
    private ComponentInstanceInputPropertyDecelerator componentInstanceInputPropertyDecelerator;
    private ComponentInstancePropertyDecelerator componentInstancePropertyDecelerator;
    private PolicyPropertyDecelerator policyPropertyDecelerator;
    private List<PropertyDecelerator> propertyDecelerators;

    public PropertyDecelerationOrchestrator(ComponentInstanceInputPropertyDecelerator componentInstanceInputPropertyDecelerator, ComponentInstancePropertyDecelerator componentInstancePropertyDecelerator, PolicyPropertyDecelerator policyPropertyDecelerator) {
        this.componentInstanceInputPropertyDecelerator = componentInstanceInputPropertyDecelerator;
        this.componentInstancePropertyDecelerator = componentInstancePropertyDecelerator;
        this.policyPropertyDecelerator = policyPropertyDecelerator;
        propertyDecelerators = Arrays.asList(componentInstanceInputPropertyDecelerator, componentInstancePropertyDecelerator, policyPropertyDecelerator);
    }

    public Either<List<InputDefinition>, StorageOperationStatus> declarePropertiesToInputs(Component component, ComponentInstInputsMap componentInstInputsMap) {
        PropertyDecelerator propertyDecelerator = getPropertyDecelerator(componentInstInputsMap);
        Pair<String, List<ComponentInstancePropInput>> propsToDeclare = componentInstInputsMap.resolvePropertiesToDeclare();
        return propertyDecelerator.declarePropertiesAsInputs(component, propsToDeclare.getLeft(), propsToDeclare.getRight());
    }

    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition inputToDelete) {
        log.debug("#unDeclarePropertiesAsInputs - removing input declaration for input {} on component {}", inputToDelete.getName(), component.getUniqueId());
        for (PropertyDecelerator propertyDecelerator : propertyDecelerators) {
            StorageOperationStatus storageOperationStatus = propertyDecelerator.unDeclarePropertiesAsInputs(component, inputToDelete);
            if (StorageOperationStatus.OK != storageOperationStatus) {
                log.debug("#unDeclarePropertiesAsInputs - failed to remove input declaration for input {} on component {}. reason {}", inputToDelete.getName(), component.getUniqueId(), storageOperationStatus);
                return storageOperationStatus;
            }
        }
        return StorageOperationStatus.OK;

    }

    private PropertyDecelerator getPropertyDecelerator(ComponentInstInputsMap componentInstInputsMap) {
        if (!MapUtils.isEmpty(componentInstInputsMap.getComponentInstanceInputsMap())) {
            return componentInstanceInputPropertyDecelerator;
        }
        if (!MapUtils.isEmpty(componentInstInputsMap.getComponentInstanceProperties())) {
            return componentInstancePropertyDecelerator;
        }
        if (!MapUtils.isEmpty(componentInstInputsMap.getPolicyProperties())) {
            return policyPropertyDecelerator;
        }
        throw new IllegalStateException("there are no properties selected for deceleration");

    }

}
