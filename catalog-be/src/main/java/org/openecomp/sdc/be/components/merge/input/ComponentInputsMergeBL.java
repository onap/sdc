package org.openecomp.sdc.be.components.merge.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.components.merge.instance.ComponentsMergeCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import fj.data.Either;

@org.springframework.stereotype.Component
public class ComponentInputsMergeBL implements ComponentsMergeCommand {

    @javax.annotation.Resource
    private InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic;

    @javax.annotation.Resource
    private ToscaOperationFacade toscaOperationFacade;

    @javax.annotation.Resource
    private ComponentsUtils componentsUtils;

    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        List<InputDefinition> inputsToMerge = currentComponent.getInputs() != null ? currentComponent.getInputs() : new ArrayList<>();
        return this.mergeAndRedeclareComponentInputs(prevComponent, currentComponent, inputsToMerge);
    }

    @Override
    public String description() {
        return "merge component inputs";
    }

    public ActionStatus mergeAndRedeclareComponentInputs(Component prevComponent, Component newComponent, List<InputDefinition> inputsToMerge) {
        mergeInputs(prevComponent, inputsToMerge);
        List<InputDefinition> previouslyDeclaredInputs = inputsValuesMergingBusinessLogic.getPreviouslyDeclaredInputsToMerge(prevComponent, newComponent);
        inputsToMerge.addAll(previouslyDeclaredInputs);
        return updateInputs(newComponent.getUniqueId(), inputsToMerge);
    }

    public ActionStatus mergeComponentInputs(Component prevComponent, Component newComponent, List<InputDefinition> inputsToMerge) {
        mergeInputs(prevComponent, inputsToMerge);
        return updateInputs(newComponent.getUniqueId(), inputsToMerge);
    }

    public ActionStatus redeclareComponentInputsForInstance(List<InputDefinition> oldInputs, Component newComponent, String instanceId) {
        List<InputDefinition> previouslyDeclaredInputs = inputsValuesMergingBusinessLogic.getPreviouslyDeclaredInputsToMerge(oldInputs, newComponent, instanceId);
        return updateInputs(newComponent.getUniqueId(), previouslyDeclaredInputs);
    }

    private void mergeInputs(Component prevComponent, List<InputDefinition> inputsToMerge) {
        Map<String, InputDefinition> oldInputsByName = prevComponent.getInputs() == null ? Collections.emptyMap() : MapUtil.toMap(prevComponent.getInputs(), InputDefinition::getName);
        Map<String, InputDefinition> inputsToMergeByName = MapUtil.toMap(inputsToMerge, InputDefinition::getName);
        inputsValuesMergingBusinessLogic.mergeComponentInputs(oldInputsByName, inputsToMergeByName);
    }

    private ActionStatus updateInputs(String containerId, List<InputDefinition> inputsToUpdate) {
        Either<List<InputDefinition>, StorageOperationStatus> updateInputsEither = toscaOperationFacade.updateInputsToComponent(inputsToUpdate, containerId);
        if (updateInputsEither.isRight()) {
            return componentsUtils.convertFromStorageResponse(updateInputsEither.right().value());
        }
        return ActionStatus.OK;
    }

}
