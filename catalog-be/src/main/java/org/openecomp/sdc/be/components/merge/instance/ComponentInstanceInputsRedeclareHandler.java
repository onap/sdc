package org.openecomp.sdc.be.components.merge.instance;

import org.openecomp.sdc.be.components.merge.input.DeclaredInputsResolver;
import org.openecomp.sdc.be.components.merge.input.InputsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.utils.ComponentUtilities;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openecomp.sdc.be.dao.utils.MapUtil.toMap;
import static org.openecomp.sdc.be.utils.PropertyDefinitionUtils.resolveGetInputProperties;

@org.springframework.stereotype.Component
public class ComponentInstanceInputsRedeclareHandler {

    private static final Logger log = Logger.getLogger(ComponentInstanceInputsRedeclareHandler.class);
    private final DeclaredInputsResolver declaredInputsResolver;
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic;
    
    public ComponentInstanceInputsRedeclareHandler(DeclaredInputsResolver declaredInputsResolver, ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils, InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic) {
        this.declaredInputsResolver = declaredInputsResolver;
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.inputsValuesMergingBusinessLogic = inputsValuesMergingBusinessLogic;
    }

    ActionStatus redeclareComponentInputsForInstance(Component container, String newInstanceId, Component newInstanceOriginType, List<InputDefinition> oldInputs) {
        log.debug("#redeclareComponentInputsForInstance - getting inputs that were previously declared from instance {} and setting on current component {}", newInstanceId, container.getUniqueId());
        Map<String, List<PropertyDataDefinition>> allPropertiesForInstance = getAllGetPropertiesForInstance(container, newInstanceId);
        List<InputDefinition> previouslyDeclaredInputs = declaredInputsResolver.getPreviouslyDeclaredInputsToMerge(oldInputs, container, allPropertiesForInstance);
        inputsValuesMergingBusinessLogic.mergeComponentInputs(oldInputs, previouslyDeclaredInputs);
        Map<String, List<PropertyDataDefinition>> getInputProperties = resolveGetInputProperties(allPropertiesForInstance);
        updateInputsAnnotations(getInputProperties.get(newInstanceId), newInstanceOriginType, previouslyDeclaredInputs);

        return updateInputs(container.getUniqueId(), previouslyDeclaredInputs);
    }

    private void updateInputsAnnotations(List<PropertyDataDefinition> instanceProps, Component newInstanceOriginType, List<InputDefinition> previouslyDeclaredInputs) {
        Map<String, PropertyDataDefinition> instancePropsById = toMap(instanceProps, PropertyDataDefinition::getUniqueId);
        for (InputDefinition input : previouslyDeclaredInputs) {
            List<Annotation> originPropInputAnnotations = getAnnotationsFromOriginType(newInstanceOriginType, input.getPropertyId(), instancePropsById);
            if(!isEmpty(originPropInputAnnotations)){
                input.setAnnotations(originPropInputAnnotations);
            }
        }
    }

    private List<Annotation> getAnnotationsFromOriginType(Component originType, String propertyId, Map<String, PropertyDataDefinition> instancePropsById) {
        PropertyDataDefinition instanceProp = instancePropsById.get(propertyId);
        String originPropInputName = instanceProp.getName();
        return ComponentUtilities.getInputAnnotations(originType, originPropInputName);
    }

    private Map<String, List<PropertyDataDefinition>> getAllGetPropertiesForInstance(Component newComponent, String instanceId) {
        List<PropertyDataDefinition> allInstanceProps = Stream.of(newComponent.safeGetComponentInstanceProperties(instanceId),
                newComponent.safeGetComponentInstanceInput(instanceId))
                .flatMap(Collection::stream)
                .map(PropertyDataDefinition::new)
                .collect(toList());
        return singletonMap(instanceId, allInstanceProps);
    }

    private ActionStatus updateInputs(String containerId, List<InputDefinition> inputsToUpdate) {
        log.debug("#updateInputs - updating inputs for container {}", containerId);
        return toscaOperationFacade.updateInputsToComponent(inputsToUpdate, containerId)
                .either(updatedInputs -> ActionStatus.OK,
                        componentsUtils::convertFromStorageResponse);
    }

}
