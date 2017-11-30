package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
/**
 * Created by chaya on 9/12/2017.
 */
@org.springframework.stereotype.Component("componentInstanceMergeDataBusinessLogic")
public class ComponentInstanceMergeDataBusinessLogic {

    private static Logger log = LoggerFactory.getLogger(ComponentInstanceMergeDataBusinessLogic.class.getName());

    @Autowired
    private List<ComponentInstanceMergeInterface> componentInstancesMergeBLs;

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;

    @Autowired
    private ComponentsUtils componentsUtils;

    //for testing only
    protected void setComponentInstancesMergeBLs(List<ComponentInstanceMergeInterface> componentInstancesMergeBLs) {
        this.componentInstancesMergeBLs = componentInstancesMergeBLs;
    }

    /**
     * Saves all containerComponents data before deleting, in order to merge once creating a new instance
     * @param containerComponent
     * @param currentResourceInstance
     */
    public DataForMergeHolder saveAllDataBeforeDeleting(org.openecomp.sdc.be.model.Component containerComponent, ComponentInstance currentResourceInstance, Component originComponent) {
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        for (ComponentInstanceMergeInterface compInstMergeBL : componentInstancesMergeBLs) {
            compInstMergeBL.saveDataBeforeMerge(dataHolder, containerComponent, currentResourceInstance, originComponent);
        }
        return dataHolder;
    }

    /**
     * Merges inputs and instance inputs/props of the new Container component with the old container component data (before deleting)
     * @param containerComponent
     * @param newContainerComponentId
     * @param newInstanceId
     * @return
     */
    public Either<Component, ResponseFormat> mergeComponentUserOrigData(User user, DataForMergeHolder dataHolder, org.openecomp.sdc.be.model.Component containerComponent, String newContainerComponentId, String newInstanceId) {

        Either<Component, StorageOperationStatus> componentWithInstancesInputsAndProperties = getComponentWithInstancesInputsAndProperties(newContainerComponentId);
        if (componentWithInstancesInputsAndProperties.isRight()) {
            log.error("Component with id {} was not found", newContainerComponentId);
            StorageOperationStatus storageOperationStatus = componentWithInstancesInputsAndProperties.right().value();
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(storageOperationStatus, containerComponent.getComponentType());
            return Either.right(componentsUtils.getResponseFormat(actionStatus));
        }
        Component updatedContainerComponent = componentWithInstancesInputsAndProperties.left().value();

        for (ComponentInstanceMergeInterface compInstMergeBL: componentInstancesMergeBLs) {
            Either<Component, ResponseFormat> compInstanceMergeEither = compInstMergeBL.mergeDataAfterCreate(user, dataHolder, updatedContainerComponent, newInstanceId);
            if (compInstanceMergeEither.isRight()) {
                return Either.right(compInstanceMergeEither.right().value());
            }
        }

        return Either.left(updatedContainerComponent);
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
