package org.openecomp.sdc.be.components.merge.property;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import fj.data.Either;

@org.springframework.stereotype.Component
public class ComponentInstancePropertiesMergeBL {

    @javax.annotation.Resource
    private ToscaOperationFacade toscaOperationFacade;

    @javax.annotation.Resource(name = "componentUtils")
    private ComponentsUtils componentsUtils;

    @javax.annotation.Resource
    private DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic;

    public ActionStatus mergeComponentInstancesProperties(Component oldComponent, Resource newResource) {
        Map<String, List<ComponentInstanceProperty>> newInstProps = newResource.getComponentInstancesProperties();
        if (newInstProps == null) {
            return ActionStatus.OK;
        }
        newInstProps.forEach((instanceId, newProps) -> mergeOldInstancePropertiesValues(oldComponent, newResource, instanceId, newProps) );
        return updateComponentInstancesProperties(newResource, newInstProps);
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
