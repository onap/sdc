package org.openecomp.sdc.be.components.property;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

import fj.data.Either;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.components.property.propertytopolicydeclarators.ComponentInstancePropertyToPolicyDeclarator;
import org.openecomp.sdc.be.components.property.propertytopolicydeclarators.ComponentPropertyToPolicyDeclarator;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class PropertyDeclarationOrchestrator {

    private static final Logger log = Logger.getLogger(PropertyDeclarationOrchestrator.class);
    private ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDeclarator;
    private ComponentInstancePropertyDeclarator componentInstancePropertyDeclarator;
    private PolicyPropertyDeclarator policyPropertyDeclarator;
    private GroupPropertyDeclarator groupPropertyDeclarator;
    private ComponentPropertyDeclarator servicePropertyDeclarator;
    private List<PropertyDeclarator> propertyDeclaratorsToInput;
    private List<PropertyDeclarator> propertyDeclaratorsToPolicy;
    private ComponentPropertyToPolicyDeclarator componentPropertyToPolicyDeclarator;
    private ComponentInstancePropertyToPolicyDeclarator componentInstancePropertyToPolicyDeclarator;

    public PropertyDeclarationOrchestrator(ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDeclarator,
            ComponentInstancePropertyDeclarator componentInstancePropertyDeclarator, PolicyPropertyDeclarator policyPropertyDeclarator,
            GroupPropertyDeclarator groupPropertyDeclarator, ComponentPropertyDeclarator servicePropertyDeclarator,
            ComponentPropertyToPolicyDeclarator componentPropertyToPolicyDeclarator,
            ComponentInstancePropertyToPolicyDeclarator componentInstancePropertyToPolicyDeclarator) {
        this.componentInstanceInputPropertyDeclarator = componentInstanceInputPropertyDeclarator;
        this.componentInstancePropertyDeclarator = componentInstancePropertyDeclarator;
        this.policyPropertyDeclarator = policyPropertyDeclarator;
        this.groupPropertyDeclarator = groupPropertyDeclarator;
        this.servicePropertyDeclarator = servicePropertyDeclarator;
        this.componentPropertyToPolicyDeclarator = componentPropertyToPolicyDeclarator;
        this.componentInstancePropertyToPolicyDeclarator = componentInstancePropertyToPolicyDeclarator;
        propertyDeclaratorsToInput = Arrays.asList(componentInstanceInputPropertyDeclarator, componentInstancePropertyDeclarator, policyPropertyDeclarator, groupPropertyDeclarator, servicePropertyDeclarator);
        propertyDeclaratorsToPolicy = Arrays.asList(componentPropertyToPolicyDeclarator, componentInstancePropertyToPolicyDeclarator);
    }

    public Either<List<InputDefinition>, StorageOperationStatus> declarePropertiesToInputs(Component component, ComponentInstInputsMap componentInstInputsMap) {
        PropertyDeclarator propertyDeclarator = getPropertyDeclarator(componentInstInputsMap);
        Pair<String, List<ComponentInstancePropInput>> propsToDeclare = componentInstInputsMap.resolvePropertiesToDeclare();
        return propertyDeclarator.declarePropertiesAsInputs(component, propsToDeclare.getLeft(), propsToDeclare.getRight());
    }

    public Either<List<PolicyDefinition>, StorageOperationStatus> declarePropertiesToPolicies(Component component, ComponentInstInputsMap componentInstInputsMap) {
        PropertyDeclarator propertyDeclarator = getPropertyDeclarator(componentInstInputsMap);
        Pair<String, List<ComponentInstancePropInput>> propsToDeclare = componentInstInputsMap.resolvePropertiesToDeclare();
        return propertyDeclarator.declarePropertiesAsPolicies(component, propsToDeclare.getLeft(), propsToDeclare.getRight());
    }

    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition inputToDelete) {
        log.debug("#unDeclarePropertiesAsInputs - removing input declaration for input {} on component {}", inputToDelete.getName(), component.getUniqueId());
        for (PropertyDeclarator propertyDeclarator : propertyDeclaratorsToInput) {
            StorageOperationStatus storageOperationStatus = propertyDeclarator.unDeclarePropertiesAsInputs(component, inputToDelete);
            if (StorageOperationStatus.OK != storageOperationStatus) {
                log.debug("#unDeclarePropertiesAsInputs - failed to remove input declaration for input {} on component {}. reason {}", inputToDelete.getName(), component.getUniqueId(), storageOperationStatus);
                return storageOperationStatus;
            }
        }
        return StorageOperationStatus.OK;

    }

    public StorageOperationStatus unDeclarePropertiesAsPolicies(Component component, PolicyDefinition policyToDelete) {
        log.debug("#unDeclarePropertiesAsInputs - removing policy declaration for input {} on component {}", policyToDelete
                                                                                                                     .getName(), component.getUniqueId());
        for(PropertyDeclarator propertyDeclarator : propertyDeclaratorsToPolicy) {
            StorageOperationStatus storageOperationStatus =
                    propertyDeclarator.unDeclarePropertiesAsPolicies(component, policyToDelete);
            if (StorageOperationStatus.OK != storageOperationStatus) {
                log.debug("#unDeclarePropertiesAsInputs - failed to remove policy declaration for policy {} on component {}. reason {}", policyToDelete
                                                                                                                                                 .getName(), component.getUniqueId(), storageOperationStatus);
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
        if(isNotEmpty(componentInstInputsMap.getServiceProperties())) {
            return servicePropertyDeclarator;
        }
        if(isNotEmpty(componentInstInputsMap.getComponentPropertiesToPolicies())) {
            return componentPropertyToPolicyDeclarator;
        }
        if(isNotEmpty(componentInstInputsMap.getComponentInstancePropertiesToPolicies())) {
            return componentInstancePropertyToPolicyDeclarator;
        }
        throw new IllegalStateException("there are no properties selected for declaration");

    }

}
