package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;

import java.util.Objects;

public abstract class BaseConsolidationDataHandler implements IConsolidationDataHandler {

    protected void addNodesConnectedOut(EntityConsolidationData entityConsolidationData, String nodeTemplateId, String requirementId, RequirementAssignment requirementAssignment) {
        if (Objects.nonNull(entityConsolidationData)) {
            entityConsolidationData.addNodesConnectedOut(nodeTemplateId, requirementId, requirementAssignment);
        }
    }

    protected void addNodesConnectedIn(EntityConsolidationData entityConsolidationData, String nodeTemplateId, String requirementId, RequirementAssignment requirementAssignment) {
        if (Objects.nonNull(entityConsolidationData)) {
            entityConsolidationData.addNodesConnectedIn(nodeTemplateId, requirementId, requirementAssignment);
        }
    }

    protected void removeParamNameFromAttrFuncList(EntityConsolidationData entityConsolidationData, String paramName) {
        if (Objects.nonNull(entityConsolidationData)){
            entityConsolidationData.removeParamNameFromAttrFuncList(paramName);
        }
    }

    protected TranslationContext getTranslationContext(TranslateTo translateTo) {
        return translateTo.getContext();
    }

    protected ServiceTemplate getServiceTemplate(TranslateTo translateTo) {
        return translateTo.getServiceTemplate();
    }

}
