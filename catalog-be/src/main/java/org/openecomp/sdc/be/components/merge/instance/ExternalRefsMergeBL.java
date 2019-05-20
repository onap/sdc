package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ExternalReferencesOperation;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.collections.MapUtils.isEmpty;

@org.springframework.stereotype.Component
public class ExternalRefsMergeBL implements ComponentInstanceMergeInterface {

    private final ExternalReferencesOperation externalReferencesOperation;

    ExternalRefsMergeBL(ExternalReferencesOperation externalReferencesOperation) {
        this.externalReferencesOperation = externalReferencesOperation;
    }

    @Override
    public void saveDataBeforeMerge(DataForMergeHolder dataHolder, Component containerComponent,
                                    ComponentInstance currentResourceInstance, Component originComponent) {
        Map<String, List<String>> externalRefs = externalReferencesOperation.getAllExternalReferences(containerComponent.getUniqueId(),
                currentResourceInstance.getUniqueId());
        dataHolder.setOrigComponentInstanceExternalRefs(externalRefs);
    }

    @Override
    public Either<Component, ResponseFormat> mergeDataAfterCreate(User user, DataForMergeHolder dataHolder, Component updatedContainerComponent, String newInstanceId) {
        Optional<ComponentInstance> componentInstance = updatedContainerComponent.getComponentInstanceById(newInstanceId);
        if (!componentInstance.isPresent()) {
            throw new ComponentException(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND,
                    newInstanceId);
        }
        Map<String, List<String>>  savedExternalRefs = dataHolder.getOrigCompInstExternalRefs();
        if (!isEmpty(savedExternalRefs)) {
            externalReferencesOperation.addAllExternalReferences(updatedContainerComponent.getUniqueId(),
                    componentInstance.get().getUniqueId(), savedExternalRefs);
        }
        return Either.left(updatedContainerComponent);
    }
}
