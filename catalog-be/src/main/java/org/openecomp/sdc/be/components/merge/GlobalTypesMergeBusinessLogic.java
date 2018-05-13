package org.openecomp.sdc.be.components.merge;

import java.util.List;

import org.openecomp.sdc.be.components.merge.input.ComponentInputsMergeBL;
import org.openecomp.sdc.be.components.merge.resource.MergeResourceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component
public class GlobalTypesMergeBusinessLogic implements MergeResourceBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTypesMergeBusinessLogic.class);

    @javax.annotation.Resource
    private GlobalInputsFilteringBusinessLogic globalInputsFilteringBusinessLogic;

    @javax.annotation.Resource
    private ComponentInputsMergeBL inputsValuesMergeBL;

    @Override
    public ActionStatus mergeResourceEntities(Resource oldResource, Resource newResource) {
        if (oldResource == null) {
            return ActionStatus.OK;
        }
        Either<List<InputDefinition>, ActionStatus> globalInputsEither = globalInputsFilteringBusinessLogic.filterGlobalInputs(newResource);
        if (globalInputsEither.isRight()) {
            LOGGER.error("failed to get global inputs of resource {} status is {}", newResource.getUniqueId(), globalInputsEither.right().value());
            return globalInputsEither.right().value();
        }
        List<InputDefinition> globalInputs = globalInputsEither.left().value();
        return inputsValuesMergeBL.mergeComponentInputs(oldResource, newResource, globalInputs);
    }

}
