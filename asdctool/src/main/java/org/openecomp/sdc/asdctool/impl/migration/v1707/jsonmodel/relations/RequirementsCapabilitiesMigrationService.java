package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations;

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementDefinition;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RequirementsCapabilitiesMigrationService<T extends Component> {

    @Resource(name = "fulfilled-capabilities-mig-service")
    FulfilledCapabilitiesMigrationService fulfilledCapabilityUpdate;

    @Resource(name = "fulfilled-requirements-mig-service")
    FulfilledRequirementsMigrationService fulfilledRequirementUpdate;

    public boolean associateFulfilledCapabilities(T component, NodeTypeEnum nodeType) {
        return fulfilledCapabilityUpdate.associateToscaDefinitions(component, nodeType);
    }

    public boolean associateFulfilledRequirements(T component, NodeTypeEnum nodeType) {
        return fulfilledRequirementUpdate.associateToscaDefinitions(component, nodeType);
    }

    public void overrideInstanceCapabilitiesRequirements(T element) {
        if (element.getComponentInstances() != null) {
            clearInstancesCapabilitiesRequirements(element);
            setInstancesRequirementsFromComponent(element);
            setInstancesCapabilitiesFromComponent(element);
        }
    }

    private void clearInstancesCapabilitiesRequirements(T element) {
        element.getComponentInstances().forEach(componentInstance -> {
            if (componentInstance.getCapabilities() != null) {
                componentInstance.getCapabilities().clear();
            }
            if (componentInstance.getRequirements() != null) {
                componentInstance.getRequirements().clear();
            }
        });
    }

    private void setInstancesCapabilitiesFromComponent(T element) {
        if (element.getCapabilities() != null) {
            Map<String, ComponentInstance> instancesById = groupInstancesById(element);
            element.getCapabilities().forEach((type, definitions) -> { setCapabilitiesOnInstance(instancesById, type, definitions);});
        }
    }

    private void setInstancesRequirementsFromComponent(T element) {
        if (element.getRequirements() != null) {
            Map<String, ComponentInstance> instancesById = groupInstancesById(element);
            element.getRequirements().forEach((type, requirements) -> { setRequirementsOnInstance(instancesById, type, requirements);});
        }
    }

    private void setCapabilitiesOnInstance(Map<String, ComponentInstance> instances, String capabilityType, List<CapabilityDefinition> definitions) {
        Map<String, List<CapabilityDefinition>> capByInstance = definitions.stream().collect(Collectors.groupingBy(CapabilityDefinition::getOwnerId));
        capByInstance.forEach((instanceId, capabilityDefinitions) -> { setCapabilitiesOnInstanceByType(instances.get(instanceId), capabilityType, capabilityDefinitions); });
    }

    private void setRequirementsOnInstance(Map<String, ComponentInstance> instances, String requirementType, List<RequirementDefinition> requirements) {
        Map<String, List<RequirementDefinition>> reqByInstance = requirements.stream().collect(Collectors.groupingBy(RequirementDefinition::getOwnerId));
        reqByInstance.forEach((instanceId, reqDefinitions) -> { setRequirementsOnInstanceByType(instances.get(instanceId), requirementType, reqDefinitions);});
    }

    private void setCapabilitiesOnInstanceByType(ComponentInstance instance, String capabilityType, List<CapabilityDefinition> capabilityDefinitions) {
        instance.getCapabilities().putIfAbsent(capabilityType, new ArrayList<>());
        instance.getCapabilities().get(capabilityType).addAll(capabilityDefinitions);
    }

    private void setRequirementsOnInstanceByType(ComponentInstance instance, String requirementType, List<RequirementDefinition> reqDefinitions) {
        instance.getRequirements().putIfAbsent(requirementType, new ArrayList<>());
        instance.getRequirements().get(requirementType).addAll(reqDefinitions);
    }

    private Map<String, ComponentInstance> groupInstancesById(T element) {
        return element.getComponentInstances()
                .stream()
                .collect(Collectors.toMap(ComponentInstance::getUniqueId, Function.identity()));
    }

}
