package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.utils.PropertiesUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class ComponentInstancePropertyDeclarator extends DefaultPropertyDeclarator<ComponentInstance, ComponentInstanceProperty> {

    private static final Logger log = Logger.getLogger(ComponentInstancePropertyDeclarator.class);
    private ToscaOperationFacade toscaOperationFacade;
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    public ComponentInstancePropertyDeclarator(ComponentsUtils componentsUtils, PropertyOperation propertyOperation, ToscaOperationFacade toscaOperationFacade, ComponentInstanceBusinessLogic componentInstanceBusinessLogic) {
        super(componentsUtils, propertyOperation);
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
    }

    @Override
    public ComponentInstanceProperty createDeclaredProperty(PropertyDataDefinition prop) {
        return new ComponentInstanceProperty(prop);
    }

    @Override
    public Either<?, StorageOperationStatus> updatePropertiesValues(Component component, String cmptInstanceId, List<ComponentInstanceProperty> properties) {
        log.debug("#updatePropertiesValues - updating component instance properties for instance {} on component {}", cmptInstanceId, component.getUniqueId());
        Map<String, List<ComponentInstanceProperty>> instProperties = Collections.singletonMap(cmptInstanceId, properties);
        return toscaOperationFacade.addComponentInstancePropertiesToComponent(component, instProperties);
    }

    @Override
    public Optional<ComponentInstance> resolvePropertiesOwner(Component component, String propertiesOwnerId) {
        log.debug("#resolvePropertiesOwner - fetching component instance {} of component {}", propertiesOwnerId, component.getUniqueId());
        return component.getComponentInstanceById(propertiesOwnerId);
    }

    @Override
    public void addPropertiesListToInput(ComponentInstanceProperty declaredProp, InputDefinition input) {
        List<ComponentInstanceProperty> propertiesList = input.getProperties();
        if(propertiesList == null) {
            propertiesList = new ArrayList<>(); // adding the property with the new value for UI
        }
        propertiesList.add(declaredProp);
        input.setProperties(propertiesList);
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition input) {

        Optional<ComponentInstanceProperty> propertyByInputId = PropertiesUtils.getPropertyByInputId(component,
                input.getUniqueId());
        if(propertyByInputId.isPresent()) {
            List<ComponentInstanceProperty> capabilityPropertyDeclaredAsInput =
                   PropertiesUtils.getCapabilityProperty(propertyByInputId.get(), input.getUniqueId());
            capabilityPropertyDeclaredAsInput.forEach(cmptInstanceProperty -> prepareValueBeforeDeleteOfCapProp(input,
                    cmptInstanceProperty));

            Optional<CapabilityDefinition> propertyCapabilityOptional = PropertiesUtils.getPropertyCapabilityOfChildInstance(
                    capabilityPropertyDeclaredAsInput.get(0).getParentUniqueId(), component.getCapabilities());
            if(!propertyCapabilityOptional.isPresent()) {
                return StorageOperationStatus.OK;
            }

            return toscaOperationFacade.updateInstanceCapabilityProperty(component, input.getInstanceUniqueId(),
                    capabilityPropertyDeclaredAsInput.get(0), propertyCapabilityOptional.get() );
        } else {
            List<ComponentInstanceProperty> componentInstancePropertiesDeclaredAsInput = componentInstanceBusinessLogic
                    .getComponentInstancePropertiesByInputId(component, input.getUniqueId());
            if (CollectionUtils.isEmpty(componentInstancePropertiesDeclaredAsInput)) {
                return StorageOperationStatus.OK;
            }
            componentInstancePropertiesDeclaredAsInput.forEach(cmptInstanceProperty -> prepareValueBeforeDelete(input,
                    cmptInstanceProperty, cmptInstanceProperty.getPath()));
            return toscaOperationFacade.updateComponentInstanceProperties(component,
                    componentInstancePropertiesDeclaredAsInput.get(0).getComponentInstanceId(),
                    componentInstancePropertiesDeclaredAsInput);
        }
    }

}
