package org.openecomp.sdc.be.components.merge.input;

import fj.data.Either;
import org.openecomp.sdc.be.components.merge.input.InputsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
public class ComponentInputsMergeBL {

    @javax.annotation.Resource
    private InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic;

    @javax.annotation.Resource
    private ToscaOperationFacade toscaOperationFacade;

    @javax.annotation.Resource
    private ComponentsUtils componentsUtils;

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
