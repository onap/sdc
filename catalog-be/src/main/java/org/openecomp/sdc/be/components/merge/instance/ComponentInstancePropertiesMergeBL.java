package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
public class ComponentInstancePropertiesMergeBL implements ComponentsMergeCommand {

    @javax.annotation.Resource
    private ToscaOperationFacade toscaOperationFacade;

    @javax.annotation.Resource(name = "componentUtils")
    private ComponentsUtils componentsUtils;

    @javax.annotation.Resource
    private DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic;

    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        Map<String, List<ComponentInstanceProperty>> newInstProps = currentComponent.getComponentInstancesProperties();
        if (newInstProps == null) {
            return ActionStatus.OK;
        }
        newInstProps.forEach((instanceId, newProps) -> mergeOldInstancePropertiesValues(prevComponent, currentComponent, instanceId, newProps) );
        return updateComponentInstancesProperties(currentComponent, newInstProps);
    }

    @Override
    public String description() {
        return "merge component instance properties";
    }


    public ActionStatus mergeComponentInstanceProperties(List<ComponentInstanceProperty> oldInstProps, List<InputDefinition> oldInputs, Component newComponent, String instanceId) {
        List<ComponentInstanceProperty> newInstProps = newComponent.safeGetComponentInstanceProperties(instanceId);
        if (newInstProps == null) {
            return ActionStatus.OK;
        }
        propertyValuesMergingBusinessLogic.mergeInstanceDataDefinitions(oldInstProps, oldInputs, newInstProps, newComponent.getInputs());
        return updateComponentInstanceProperties(newComponent, instanceId, newInstProps);
    }

    private void mergeOldInstancePropertiesValues(Component oldComponent, Component newComponent, String instanceId, List<ComponentInstanceProperty> newProps) {
        List<ComponentInstanceProperty> oldInstProperties = oldComponent == null ? Collections.emptyList() : oldComponent.safeGetComponentInstanceProperties(instanceId);
        List<InputDefinition> oldInputs = oldComponent == null ? Collections.emptyList() : oldComponent.getInputs();
        propertyValuesMergingBusinessLogic.mergeInstanceDataDefinitions(oldInstProperties, oldInputs, newProps, newComponent.getInputs());
    }

    private ActionStatus updateComponentInstancesProperties(Component newComponent, Map<String, List<ComponentInstanceProperty>> newInstProps) {
        Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> mapStorageOperationStatusEither = toscaOperationFacade.updateComponentInstancePropsToComponent(newInstProps, newComponent.getUniqueId());
        if (mapStorageOperationStatusEither.isRight()) {
            return componentsUtils.convertFromStorageResponse(mapStorageOperationStatusEither.right().value());
        }
        return ActionStatus.OK;
    }

    private ActionStatus updateComponentInstanceProperties(Component component, String instanceId, List<ComponentInstanceProperty> newInstProps) {
        StorageOperationStatus storageOperationStatus = toscaOperationFacade.updateComponentInstanceProperties(component, instanceId, newInstProps);
        if (storageOperationStatus != StorageOperationStatus.OK) {
            return componentsUtils.convertFromStorageResponse(storageOperationStatus);
        }
        return ActionStatus.OK;
    }

}
