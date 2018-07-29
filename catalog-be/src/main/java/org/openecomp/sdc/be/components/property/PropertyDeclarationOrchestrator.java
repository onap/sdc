package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

@org.springframework.stereotype.Component
public class PropertyDeclarationOrchestrator {

    private static final Logger log = Logger.getLogger(PropertyDeclarationOrchestrator.class);
    private ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDeclarator;
    private ComponentInstancePropertyDeclarator componentInstancePropertyDeclarator;
    private PolicyPropertyDeclarator policyPropertyDeclarator;
    private GroupPropertyDeclarator groupPropertyDeclarator;
    private List<PropertyDeclarator> propertyDeclarators;

    public PropertyDeclarationOrchestrator(ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDeclarator, ComponentInstancePropertyDeclarator componentInstancePropertyDeclarator, PolicyPropertyDeclarator policyPropertyDeclarator, GroupPropertyDeclarator groupPropertyDeclarator) {
        this.componentInstanceInputPropertyDeclarator = componentInstanceInputPropertyDeclarator;
        this.componentInstancePropertyDeclarator = componentInstancePropertyDeclarator;
        this.policyPropertyDeclarator = policyPropertyDeclarator;
        this.groupPropertyDeclarator = groupPropertyDeclarator;
        propertyDeclarators = Arrays.asList(componentInstanceInputPropertyDeclarator, componentInstancePropertyDeclarator, policyPropertyDeclarator, groupPropertyDeclarator);
    }

    public Either<List<InputDefinition>, StorageOperationStatus> declarePropertiesToInputs(Component component, ComponentInstInputsMap componentInstInputsMap) {
        PropertyDeclarator propertyDeclarator = getPropertyDeclarator(componentInstInputsMap);
        Pair<String, List<ComponentInstancePropInput>> propsToDeclare = componentInstInputsMap.resolvePropertiesToDeclare();
        return propertyDeclarator.declarePropertiesAsInputs(component, propsToDeclare.getLeft(), propsToDeclare.getRight());
    }

    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition inputToDelete) {
        log.debug("#unDeclarePropertiesAsInputs - removing input declaration for input {} on component {}", inputToDelete.getName(), component.getUniqueId());
        for (PropertyDeclarator propertyDeclarator : propertyDeclarators) {
            StorageOperationStatus storageOperationStatus = propertyDeclarator.unDeclarePropertiesAsInputs(component, inputToDelete);
            if (StorageOperationStatus.OK != storageOperationStatus) {
                log.debug("#unDeclarePropertiesAsInputs - failed to remove input declaration for input {} on component {}. reason {}", inputToDelete.getName(), component.getUniqueId(), storageOperationStatus);
                return storageOperationStatus;
            }
        }
        return StorageOperationStatus.OK;

    }

    private PropertyDeclarator getPropertyDeclarator(ComponentInstInputsMap componentInstInputsMap) {
        if (isNotEmpty(componentInstInputsMap.getComponentInstanceInputsMap())) {
            return componentInstanceInputPropertyDeclarator;
        }
        if (isNotEmpty(componentInstInputsMap.getComponentInstanceProperties())) {
            return componentInstancePropertyDeclarator;
        }
        if (isNotEmpty(componentInstInputsMap.getPolicyProperties())) {
            return policyPropertyDeclarator;
        }
        if (isNotEmpty(componentInstInputsMap.getGroupProperties())) {
            return groupPropertyDeclarator;
        }
        throw new IllegalStateException("there are no properties selected for declaration");

    }

}
