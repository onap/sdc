package org.openecomp.sdc.be.components.merge.resource;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.components.merge.input.ComponentInputsMergeBL;
import org.openecomp.sdc.be.components.merge.property.ComponentInstanceInputsMergeBL;
import org.openecomp.sdc.be.components.merge.property.ComponentInstancePropertiesMergeBL;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourceDataMergeBusinessLogic implements MergeResourceBusinessLogic {

    private final static Logger log = LoggerFactory.getLogger(ResourceDataMergeBusinessLogic.class.getName());

    @javax.annotation.Resource
    private ComponentInputsMergeBL inputsValuesMergeBL;

    @javax.annotation.Resource
    private ComponentInstancePropertiesMergeBL instancePropertiesValueMergeBL;

    @javax.annotation.Resource
    private ComponentInstanceInputsMergeBL instanceInputsValueMergeBL;

    @Override
    public ActionStatus mergeResourceEntities(Resource oldResource, Resource newResource) {
        if (oldResource == null) {
            return ActionStatus.OK;
        }

        ActionStatus mergeInstInputsStatus = instancePropertiesValueMergeBL.mergeComponentInstancesProperties(oldResource, newResource);
        if (mergeInstInputsStatus != ActionStatus.OK) {
            log.error("failed to merge instance properties of resource {} status is {}", newResource.getUniqueId(), mergeInstInputsStatus);
            return mergeInstInputsStatus;
        }

        ActionStatus mergeInstPropsStatus = instanceInputsValueMergeBL.mergeComponentInstancesInputs(oldResource, newResource);
        if (mergeInstPropsStatus != ActionStatus.OK) {
            log.error("failed to merge instance inputs of resource {} status is {}", newResource.getUniqueId(), mergeInstPropsStatus);
            return mergeInstPropsStatus;
        }

        ActionStatus mergeInputsStatus = mergeInputs(oldResource, newResource);
        if (mergeInputsStatus != ActionStatus.OK) {
            log.error("failed to merge inputs of resource {} status is {}", newResource.getUniqueId(), mergeInputsStatus);
            return mergeInputsStatus;
        }
        return ActionStatus.OK;
    }

    private ActionStatus mergeInputs(Resource oldResource, Resource newResource) {
        List<InputDefinition> inputsToMerge = newResource.getInputs() != null ? newResource.getInputs() : new ArrayList<>();
        return inputsValuesMergeBL.mergeAndRedeclareComponentInputs(oldResource, newResource, inputsToMerge);
    }


}
