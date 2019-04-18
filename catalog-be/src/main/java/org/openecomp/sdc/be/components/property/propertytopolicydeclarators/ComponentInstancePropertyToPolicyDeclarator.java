package org.openecomp.sdc.be.components.property.propertytopolicydeclarators;

import fj.data.Either;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.property.DefaultPropertyDeclarator;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;

@org.springframework.stereotype.Component
public class ComponentInstancePropertyToPolicyDeclarator extends
        DefaultPropertyDeclarator<ComponentInstance, ComponentInstanceProperty> {

    private ToscaOperationFacade toscaOperationFacade;
    PropertyBusinessLogic propertyBl;
    private ComponentInstanceBusinessLogic componentInstanceBl;

    public ComponentInstancePropertyToPolicyDeclarator(ComponentsUtils componentsUtils,
            PropertyOperation propertyOperation, ToscaOperationFacade toscaOperationFacade,
            PropertyBusinessLogic propertyBl, ComponentInstanceBusinessLogic componentInstanceBl) {
        super(componentsUtils, propertyOperation);
        this.toscaOperationFacade = toscaOperationFacade;
        this.propertyBl = propertyBl;
        this.componentInstanceBl = componentInstanceBl;
    }

    @Override
    protected ComponentInstanceProperty createDeclaredProperty(PropertyDataDefinition prop) {
        return new ComponentInstanceProperty(prop);
    }

    @Override
    protected Either<?, StorageOperationStatus> updatePropertiesValues(Component component, String componentInstanceId,
            List<ComponentInstanceProperty> properties) {
        Map<String, List<ComponentInstanceProperty>>
                instProperties = Collections.singletonMap(componentInstanceId, properties);
        return toscaOperationFacade.addComponentInstancePropertiesToComponent(component, instProperties);
    }

    @Override
    protected Optional<ComponentInstance> resolvePropertiesOwner(Component component, String componentInstanceId) {
        return component.getComponentInstanceById(componentInstanceId);
    }

    @Override
    protected void addPropertiesListToInput(ComponentInstanceProperty declaredProp, InputDefinition input) {
        return;
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition input) {
        return StorageOperationStatus.OK;
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsListInputs(Component component, InputDefinition input) {
        return StorageOperationStatus.OK;
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsPolicies(Component component, PolicyDefinition policy) {

        Optional<ComponentInstanceProperty> propertyCandidate =
                componentInstanceBl.getComponentInstancePropertyByPolicyId(component, policy);


        if(propertyCandidate.isPresent()) {
            return toscaOperationFacade
                           .updateComponentInstanceProperty(component, policy.getInstanceUniqueId(), propertyCandidate.get());
        }

        return StorageOperationStatus.OK;
    }
}
