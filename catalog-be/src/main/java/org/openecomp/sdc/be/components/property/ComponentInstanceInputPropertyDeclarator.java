package org.openecomp.sdc.be.components.property;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openecomp.sdc.be.model.utils.ComponentUtilities.getInputAnnotations;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class ComponentInstanceInputPropertyDeclarator extends DefaultPropertyDeclarator<ComponentInstance, ComponentInstanceInput> {

    private static final Logger log = Logger.getLogger(ComponentInstanceInputPropertyDeclarator.class);
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final ExceptionUtils exceptionUtils;

    public ComponentInstanceInputPropertyDeclarator(ComponentsUtils componentsUtils, PropertyOperation propertyOperation, ToscaOperationFacade toscaOperationFacade, ComponentInstanceBusinessLogic componentInstanceBusinessLogic, ExceptionUtils exceptionUtils) {
        super(componentsUtils, propertyOperation);
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.exceptionUtils = exceptionUtils;
    }

    @Override
    public ComponentInstanceInput createDeclaredProperty(PropertyDataDefinition prop) {
        return new ComponentInstanceInput(prop);
    }

    @Override
    public Either<?, StorageOperationStatus> updatePropertiesValues(Component component, String cmptInstanceId, List<ComponentInstanceInput> properties) {
        log.debug("#updatePropertiesValues - updating component instance inputs for instance {} on component {}", cmptInstanceId, component.getUniqueId());
        Map<String, List<ComponentInstanceInput>> instProperties = Collections.singletonMap(cmptInstanceId, properties);
        return toscaOperationFacade.addComponentInstanceInputsToComponent(component, instProperties);
    }

    @Override
    public Optional<ComponentInstance> resolvePropertiesOwner(Component component, String propertiesOwnerId) {
        log.debug("#resolvePropertiesOwner - fetching component instance {} of component {}", propertiesOwnerId, component.getUniqueId());
        return component.getComponentInstanceById(propertiesOwnerId);
    }

    @Override
    public void addPropertiesListToInput(ComponentInstanceInput declaredProp, InputDefinition input) {
        List<ComponentInstanceInput> inputsValueList = input.getInputs();
        if(inputsValueList == null) {
            inputsValueList = new ArrayList<>(); // adding the property with the new value for UI
        }
        inputsValueList.add(declaredProp);
        input.setInputs(inputsValueList);
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition input) {
        List<ComponentInstanceInput> componentInstanceInputsByInputId = componentInstanceBusinessLogic.getComponentInstanceInputsByInputId(component, input.getUniqueId());
        if (isEmpty(componentInstanceInputsByInputId)) {
            return StorageOperationStatus.OK;
        }
        componentInstanceInputsByInputId.forEach(cmptInstanceInput -> prepareValueBeforeDelete(input, cmptInstanceInput, cmptInstanceInput.getPath()));
        return toscaOperationFacade.updateComponentInstanceInputs(component, componentInstanceInputsByInputId.get(0).getComponentInstanceId(), componentInstanceInputsByInputId);
    }

    @Override
    InputDefinition createInputFromProperty(String componentId, ComponentInstance propertiesOwner, String inputName, ComponentInstancePropInput propInput, PropertyDataDefinition prop) {
        InputDefinition inputFromProperty = super.createInputFromProperty(componentId, propertiesOwner, inputName, propInput, prop);
        Component propertiesOwnerNodeType = getInstanceOriginType(propertiesOwner);
        enrichInputWithAnnotations(prop, inputFromProperty, propertiesOwnerNodeType);
        return inputFromProperty;
    }

    private void enrichInputWithAnnotations(PropertyDataDefinition prop, InputDefinition inputFromProperty, Component propertiesOwnerNodeType) {
        List<Annotation> inputAnnotations = getInputAnnotations(propertiesOwnerNodeType, prop.getName());
        if(!isEmpty(inputAnnotations)){
            inputFromProperty.setAnnotations(inputAnnotations);
        }
    }

    private Component getInstanceOriginType(ComponentInstance propertiesOwner) {
        return toscaOperationFacade.getToscaElement(propertiesOwner.getActualComponentUid(), getFilterComponentInputs())
                    .left()
                    .on(err -> exceptionUtils.rollBackAndThrow(err, propertiesOwner.getActualComponentUid()));
    }

    private ComponentParametersView getFilterComponentInputs() {
        ComponentParametersView filterInputs = new ComponentParametersView(true);
        filterInputs.setIgnoreInputs(false);
        return filterInputs;
    }
}
