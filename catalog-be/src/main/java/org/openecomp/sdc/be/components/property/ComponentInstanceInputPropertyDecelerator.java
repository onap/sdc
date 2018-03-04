package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Component
public class ComponentInstanceInputPropertyDecelerator extends DefaultPropertyDecelerator<ComponentInstance, ComponentInstanceInput> {

    private static final Logger log = LoggerFactory.getLogger(ComponentInstanceInputPropertyDecelerator.class);
    private ToscaOperationFacade toscaOperationFacade;
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    public ComponentInstanceInputPropertyDecelerator(ComponentsUtils componentsUtils, PropertyOperation propertyOperation, ToscaOperationFacade toscaOperationFacade, ComponentInstanceBusinessLogic componentInstanceBusinessLogic) {
        super(componentsUtils, propertyOperation);
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
    }

    @Override
    ComponentInstanceInput createDeclaredProperty(PropertyDataDefinition prop) {
        return new ComponentInstanceInput(prop);
    }

    @Override
    Either<?, StorageOperationStatus> updatePropertiesValues(Component component, String cmptInstanceId, List<ComponentInstanceInput> properties) {
        log.debug("#updatePropertiesValues - updating component instance inputs for instance {} on component {}", cmptInstanceId, component.getUniqueId());
        Map<String, List<ComponentInstanceInput>> instProperties = Collections.singletonMap(cmptInstanceId, properties);
        return toscaOperationFacade.addComponentInstanceInputsToComponent(component, instProperties);
    }

    @Override
    Optional<ComponentInstance> resolvePropertiesOwner(Component component, String propertiesOwnerId) {
        log.debug("#resolvePropertiesOwner - fetching component instance {} of component {}", propertiesOwnerId, component.getUniqueId());
        return component.getComponentInstanceById(propertiesOwnerId);
    }

    @Override
    void addPropertiesListToInput(ComponentInstanceInput declaredProp, PropertyDataDefinition originalProp, InputDefinition input) {
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
        if (CollectionUtils.isEmpty(componentInstanceInputsByInputId)) {
            return StorageOperationStatus.OK;
        }
        componentInstanceInputsByInputId.forEach(cmptInstanceInput -> prepareValueBeforeDelete(input, cmptInstanceInput, cmptInstanceInput.getPath()));
        return toscaOperationFacade.updateComponentInstanceInputs(component, componentInstanceInputsByInputId.get(0).getComponentInstanceId(), componentInstanceInputsByInputId);
    }

}
