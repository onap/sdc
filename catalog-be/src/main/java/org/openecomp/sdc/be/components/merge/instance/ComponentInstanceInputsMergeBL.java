package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.components.merge.VspComponentsMergeCommand;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.core.annotation.Order;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic.ANY_ORDER_COMMAND;

@org.springframework.stereotype.Component
@Order(ANY_ORDER_COMMAND)
public class ComponentInstanceInputsMergeBL implements VspComponentsMergeCommand {

    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic;

    public ComponentInstanceInputsMergeBL(ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils, DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.propertyValuesMergingBusinessLogic = propertyValuesMergingBusinessLogic;
    }

    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        Map<String, List<ComponentInstanceInput>> componentInstancesInputs = currentComponent.getComponentInstancesInputs();
        if (componentInstancesInputs == null) {
            return ActionStatus.OK;
        }
        componentInstancesInputs.forEach((instanceId, instInputs) -> mergeOldInstanceInputsValues(prevComponent, currentComponent, instanceId, instInputs));
        return updateComponentInstancesInputs(currentComponent, componentInstancesInputs);
    }

    @Override
    public String description() {
        return "merge component instance inputs";
    }

    public ActionStatus mergeComponentInstanceInputs(List<ComponentInstanceInput> oldInstProps, List<InputDefinition> oldInputs, Component newComponent, String instanceId) {
        List<ComponentInstanceInput> newInstInputs = newComponent.safeGetComponentInstanceInput(instanceId);
        if (newInstInputs == null) {
            return ActionStatus.OK;
        }
        
        List<ComponentInstanceInput> oldRedeclaredInputs = findComponentInputs(oldInstProps);
        oldRedeclaredInputs.forEach(oldInput -> newInstInputs.stream()
                                                              .filter(newInstInput -> oldInput.getName().equals(newInstInput.getName()))
                                                              .forEach(this::switchValues));
        
        propertyValuesMergingBusinessLogic.mergeInstanceDataDefinitions(oldInstProps, oldInputs, newInstInputs, newComponent.getInputs());
        return updateComponentInstanceInputs(newComponent, instanceId, newInstInputs);
    }
    
    private void switchValues(ComponentInstanceInput input) {
        String tempDefaultValue = input.getDefaultValue();
        input.setDefaultValue(input.getValue());
        input.setValue(tempDefaultValue);
    }
    
    private List<ComponentInstanceInput> findComponentInputs(List<ComponentInstanceInput> oldInstProps) {
        return oldInstProps.stream()
                           .filter(ComponentInstanceInput::isGetInputProperty)
                           .collect(Collectors.toList());
    }

    private ActionStatus updateComponentInstanceInputs(Component newComponent, String instanceId, List<ComponentInstanceInput> newInstInput) {
        StorageOperationStatus storageOperationStatus = toscaOperationFacade.updateComponentInstanceInputs(newComponent, instanceId, newInstInput);
        if (storageOperationStatus != StorageOperationStatus.OK) {
            return componentsUtils.convertFromStorageResponse(storageOperationStatus);
        }
        return ActionStatus.OK;
    }

    private ActionStatus updateComponentInstancesInputs(Component component, Map<String, List<ComponentInstanceInput>> componentInstancesInputs) {
        Either<Map<String, List<ComponentInstanceInput>>, StorageOperationStatus> mapStorageOperationStatusEither = toscaOperationFacade.updateComponentInstanceInputsToComponent(componentInstancesInputs, component.getUniqueId());
        if (mapStorageOperationStatusEither.isRight()) {
            return componentsUtils.convertFromStorageResponse(mapStorageOperationStatusEither.right().value());
        }
        return ActionStatus.OK;
    }

    private void mergeOldInstanceInputsValues(Component oldComponent, Component newComponent, String instanceId, List<ComponentInstanceInput> instInputs) {
        ComponentInstance currentCompInstance = newComponent.getComponentInstanceById(instanceId).get();
        List<ComponentInstanceInput> oldInstInputs = oldComponent == null ? Collections.emptyList() : oldComponent.safeGetComponentInstanceInputsByName(currentCompInstance.getName());
        List<InputDefinition> oldInputs = oldComponent == null ? Collections.emptyList() : oldComponent.getInputs();
        propertyValuesMergingBusinessLogic.mergeInstanceDataDefinitions(oldInstInputs, oldInputs, instInputs, newComponent.getInputs());
    }

}
