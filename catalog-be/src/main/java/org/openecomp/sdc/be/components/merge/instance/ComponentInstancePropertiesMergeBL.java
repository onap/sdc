package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.components.merge.VspComponentsMergeCommand;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.core.annotation.Order;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic.ANY_ORDER_COMMAND;

@org.springframework.stereotype.Component
@Order(ANY_ORDER_COMMAND)
public class ComponentInstancePropertiesMergeBL implements VspComponentsMergeCommand {

    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic;

    public ComponentInstancePropertiesMergeBL(ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils, DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.propertyValuesMergingBusinessLogic = propertyValuesMergingBusinessLogic;
    }

    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        Map<String, List<ComponentInstanceProperty>> newInstProps = currentComponent.getComponentInstancesProperties();
        if (newInstProps == null) {
            return ActionStatus.OK;
        }
        Map<String, String> currComponentNames = getComponentNameByUniqueId(currentComponent);
        Map<String, String> prevComponentUniqueIds = getComponentUniqueIdByName(prevComponent);
        
        newInstProps.forEach((instanceId, newProps) -> {
            String instanceName = currComponentNames.get(instanceId);
            String oldInstanceId = prevComponentUniqueIds.get(instanceName);
            
            mergeOldInstancePropertiesValues(prevComponent, currentComponent, oldInstanceId, newProps);
        });
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
    
    private static Map<String, String> getComponentNameByUniqueId(Component component) {
        return asMap(component, ComponentInstance::getUniqueId, ComponentInstance::getName);
    }
    
    private static Map<String, String> getComponentUniqueIdByName(Component component) {
        return asMap(component, ComponentInstance::getName, ComponentInstance::getUniqueId);
    }
    
    private static Map<String, String> asMap(Component component, Function<? super ComponentInstance, ? extends String> keyMapper, Function<? super ComponentInstance, ? extends String> valueMapper) {
        return component.safeGetComponentInstances().stream().
            collect(Collectors.toMap(keyMapper, valueMapper));
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
