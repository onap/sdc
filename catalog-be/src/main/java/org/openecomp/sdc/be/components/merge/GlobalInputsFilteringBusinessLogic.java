package org.openecomp.sdc.be.components.merge;

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GlobalInputsFilteringBusinessLogic extends BaseBusinessLogic {

    @javax.annotation.Resource
    private GenericTypeBusinessLogic genericTypeBusinessLogic;

    Either<List<InputDefinition>, ActionStatus> filterGlobalInputs(Resource newResource) {
            Either<Resource, StorageOperationStatus> genericComp = toscaOperationFacade.getLatestCertifiedNodeTypeByToscaResourceName(newResource.fetchGenericTypeToscaNameFromConfig());
            return genericComp.bimap(genericResource -> findCommonInputs(genericResource, newResource),
                                     storageOperationStatus -> componentsUtils.convertFromStorageResponse(storageOperationStatus));
    }

    private List<InputDefinition> findCommonInputs(Resource genericResource, Resource resource) {
        List<InputDefinition> resourceInputs = resource.getInputs();
        List<InputDefinition> genericInputs = genericTypeBusinessLogic.generateInputsFromGenericTypeProperties(genericResource);
        Set<String> genericInputsNames = genericInputs.stream().map(InputDefinition::getName).collect(Collectors.toSet());
        return resourceInputs.stream().filter(input -> genericInputsNames.contains(input.getName())).collect(Collectors.toList());
    }

}
