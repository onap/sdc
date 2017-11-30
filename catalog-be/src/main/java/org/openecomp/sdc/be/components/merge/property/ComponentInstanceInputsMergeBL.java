package org.openecomp.sdc.be.components.merge.property;

import fj.data.Either;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
public class ComponentInstanceInputsMergeBL {

    @javax.annotation.Resource
    private ToscaOperationFacade toscaOperationFacade;

    @javax.annotation.Resource
    private ComponentsUtils componentsUtils;

    @javax.annotation.Resource
    private DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic;

    public ActionStatus mergeComponentInstancesInputs(Component oldComponent, Component newComponent) {
        Map<String, List<ComponentInstanceInput>> componentInstancesInputs = newComponent.getComponentInstancesInputs();
        if (componentInstancesInputs == null) {
            return ActionStatus.OK;
        }
        componentInstancesInputs.forEach((instanceId, instInputs) -> mergeOldInstanceInputsValues(oldComponent, newComponent, instanceId, instInputs));
        return updateComponentInstancesInputs(newComponent, componentInstancesInputs);
    }

    public ActionStatus mergeComponentInstanceInputs(List<ComponentInstanceInput> oldInstProps, List<InputDefinition> oldInputs, Component newComponent, String instanceId) {
        List<ComponentInstanceInput> newInstInput = newComponent.safeGetComponentInstanceInput(instanceId);
        if (newInstInput == null) {
            return ActionStatus.OK;
        }
        propertyValuesMergingBusinessLogic.mergeInstanceDataDefinitions(oldInstProps, oldInputs, newInstInput, newComponent.getInputs());
        return updateComponentInstanceInputs(newComponent, instanceId, newInstInput);
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
