package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.components.merge.input.ComponentInputsMergeBL;
import org.openecomp.sdc.be.components.merge.property.ComponentInstanceInputsMergeBL;
import org.openecomp.sdc.be.components.merge.property.ComponentInstancePropertiesMergeBL;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chaya on 9/20/2017.
 */
@org.springframework.stereotype.Component("ComponentInstancePropsAndInputsMerge")
public class ComponentInstancePropsAndInputsMerge implements ComponentInstanceMergeInterface {

    private static Logger log = LoggerFactory.getLogger(ComponentInstancePropsAndInputsMerge.class.getName());

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;

    @Autowired
    private ComponentsUtils componentsUtils;

    @Autowired
    private ComponentInstancePropertiesMergeBL componentInstancePropertiesMergeBL;

    @Autowired
    private ComponentInstanceInputsMergeBL resourceInstanceInputsMergeBL;

    @Autowired
    private ComponentInputsMergeBL resourceInputsMergeBL;

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent, ComponentInstance currentResourceInstance, Component originComponent) {
        dataHolder.setOrigComponentInstanceInputs(containerComponent.safeGetComponentInstanceInputsByName(currentResourceInstance.getName()));
        dataHolder.setOrigComponentInstanceProperties(containerComponent.safeGetComponentInstanceProperties(currentResourceInstance.getUniqueId()));
        dataHolder.setOrigComponentInputs(containerComponent.getInputs());
    }

    @Override
    public Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Either<List<ComponentInstanceInput>, ActionStatus> instanceInputsEither = mergeComponentInstanceInputsIntoContainer(dataHolder, updatedContainerComponent, newInstanceId);
        if (instanceInputsEither.isRight()) {
            ActionStatus actionStatus = instanceInputsEither.right().value();
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        Either<List<ComponentInstanceProperty>, ActionStatus> instancePropsEither = mergeComponentInstancePropsIntoContainer(dataHolder, updatedContainerComponent, newInstanceId);
        if (instancePropsEither.isRight()) {
            ActionStatus actionStatus = instancePropsEither.right().value();
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        Either<List<InputDefinition>, ActionStatus> inputsEither = mergeComponentInputsIntoContainer(dataHolder, updatedContainerComponent.getUniqueId(), newInstanceId);
        if (inputsEither.isRight()) {
            ActionStatus actionStatus = inputsEither.right().value();
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        return Either.left(updatedContainerComponent);
    }

    private Either<List<ComponentInstanceProperty>, ActionStatus> mergeComponentInstancePropsIntoContainer(DataForMergeHolder dataHolder, Component updatedComponent, String instanceId) {
        List<ComponentInstanceProperty> originComponentInstanceProps = dataHolder.getOrigComponentInstanceProperties();
        List<InputDefinition> originComponentsInputs = dataHolder.getOrigComponentInputs();
        List<ComponentInstanceProperty> newComponentInstancesProps = updatedComponent.safeGetComponentInstanceProperties(instanceId);
        ActionStatus actionStatus = componentInstancePropertiesMergeBL.mergeComponentInstanceProperties(originComponentInstanceProps, originComponentsInputs, updatedComponent, instanceId);

        if (actionStatus != ActionStatus.OK) {
            log.error("Failed to update component {} with merged instance properties", updatedComponent.getUniqueId(), newComponentInstancesProps);
            return Either.right(actionStatus);
        }
        return Either.left(newComponentInstancesProps);
    }

    private Either<List<ComponentInstanceInput>, ActionStatus> mergeComponentInstanceInputsIntoContainer(DataForMergeHolder dataHolder, Component updatedComponent, String instanceId) {
        List<ComponentInstanceInput> originComponentInstanceInputs 	= dataHolder.getOrigComponentInstanceInputs();
        List<InputDefinition> originComponentsInputs = dataHolder.getOrigComponentInputs();
        List<ComponentInstanceInput> newComponentInstancesInputs = updatedComponent.safeGetComponentInstanceInput(instanceId);
        ActionStatus actionStatus = resourceInstanceInputsMergeBL.mergeComponentInstanceInputs(originComponentInstanceInputs, originComponentsInputs, updatedComponent, instanceId);
        if (actionStatus != ActionStatus.OK) {
            log.error("Failed to update component {} with merged instance properties", updatedComponent.getUniqueId(), newComponentInstancesInputs);
            return Either.right(actionStatus);
        }
        return Either.left(newComponentInstancesInputs);
    }

    private Either<List<InputDefinition>, ActionStatus> mergeComponentInputsIntoContainer(DataForMergeHolder dataHolder, String newContainerComponentId, String newInstanceId) {
        List<InputDefinition> origComponentInputs = dataHolder.getOrigComponentInputs();
        List<InputDefinition> inputsToAddToContainer = new ArrayList<>();
        if (origComponentInputs != null && !origComponentInputs.isEmpty()) {
            // get  instance inputs and properties after merge
            Either<Component, StorageOperationStatus> componentWithInstancesInputsAndProperties = getComponentWithInstancesInputsAndProperties(newContainerComponentId);
            if (componentWithInstancesInputsAndProperties.isRight()) {
                log.error("Component %s was not found", newContainerComponentId);
                return Either.right(componentsUtils.convertFromStorageResponse(componentWithInstancesInputsAndProperties.right().value()));
            }
            Component updatedContainerComponent = componentWithInstancesInputsAndProperties.left().value();

            ActionStatus redeclareStatus = resourceInputsMergeBL.redeclareComponentInputsForInstance(origComponentInputs, updatedContainerComponent, newInstanceId);
            if (redeclareStatus != ActionStatus.OK) {
                log.error("Failed to update component {} with merged inputs {}", newContainerComponentId, inputsToAddToContainer);
                Either.right(redeclareStatus);
            }
        }
        return Either.left(inputsToAddToContainer);
    }

    private Either<Component, StorageOperationStatus> getComponentWithInstancesInputsAndProperties(String containerComponentId) {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreComponentInstances(false);
        filter.setIgnoreComponentInstancesInputs(false);
        filter.setIgnoreComponentInstancesProperties(false);
        filter.setIgnoreArtifacts(false);
        return toscaOperationFacade.getToscaElement(containerComponentId, filter);
    }
}
