package org.openecomp.sdc.be.components.merge.input;

import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.components.merge.ComponentsGlobalMergeCommand;
import org.openecomp.sdc.be.components.merge.GlobalInputsFilteringBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic.LAST_COMMAND;
import static org.openecomp.sdc.be.utils.PropertyDefinitionUtils.convertListOfProperties;

@org.springframework.stereotype.Component
@Order(LAST_COMMAND)
public class GlobalInputsMergeCommand extends InputsMergeCommand implements ComponentsGlobalMergeCommand {

    private GlobalInputsFilteringBusinessLogic globalInputsFilteringBusinessLogic;
    private ExceptionUtils exceptionUtils;

    public GlobalInputsMergeCommand(InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic, DeclaredInputsResolver declaredInputsResolver, ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils, GlobalInputsFilteringBusinessLogic globalInputsFilteringBusinessLogic, ExceptionUtils exceptionUtils) {
        super(inputsValuesMergingBusinessLogic, declaredInputsResolver, toscaOperationFacade, componentsUtils);
        this.globalInputsFilteringBusinessLogic = globalInputsFilteringBusinessLogic;
        this.exceptionUtils = exceptionUtils;
    }

    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        return super.redeclareAndMergeInputsValues(prevComponent, currentComponent);
    }

    @Override
    public String description() {
        return "merge global (non vsp) inputs";
    }

    @Override
    List<InputDefinition> getInputsToMerge(Component component) {
        return globalInputsFilteringBusinessLogic.filterGlobalInputs(component).left().on(err -> exceptionUtils.rollBackAndThrow(err));
    }

    @Override
    Map<String, List<PropertyDataDefinition>> getProperties(Component component) {
        return Stream.of(component.safeGetGroupsProperties(), component.safeGetPolicyProperties())
                .flatMap(map -> map.entrySet().stream())
                .collect(toMap(Map.Entry::getKey, entry -> convertListOfProperties(entry.getValue())));
    }

}
