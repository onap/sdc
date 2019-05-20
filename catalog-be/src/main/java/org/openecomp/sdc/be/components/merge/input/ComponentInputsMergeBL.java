package org.openecomp.sdc.be.components.merge.input;

import org.openecomp.sdc.be.components.merge.VspComponentsMergeCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic.LAST_COMMAND;
import static org.openecomp.sdc.be.utils.PropertyDefinitionUtils.convertListOfProperties;

@org.springframework.stereotype.Component
@Order(LAST_COMMAND)//must run after all properties values were merged
public class ComponentInputsMergeBL extends InputsMergeCommand implements VspComponentsMergeCommand {

    public ComponentInputsMergeBL(InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic, DeclaredInputsResolver declaredInputsResolver, ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils) {
        super(inputsValuesMergingBusinessLogic, declaredInputsResolver, toscaOperationFacade, componentsUtils);
    }

    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        return super.redeclareAndMergeInputsValues(prevComponent, currentComponent);
    }

    @Override
    public String description() {
        return "merge component inputs";
    }

    @Override
    List<InputDefinition> getInputsToMerge(Component component) {
        return component.safeGetInputs();
    }

    @Override
    Map<String, List<PropertyDataDefinition>> getProperties(Component component) {
        return Stream.of(component.safeGetComponentInstancesProperties(),
                         component.safeGetComponentInstancesInputs(),
                         component.safeGetGroupsProperties(),
                         component.safeGetPolicyProperties())
                .flatMap(map -> map.entrySet().stream())
                .collect(toMap(Entry::getKey, entry -> convertListOfProperties(entry.getValue())));
    }

}
