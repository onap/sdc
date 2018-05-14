package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.errors.DuplicateResourceIdsInDifferentFilesErrorBuilder;

import java.util.Objects;

public class NestedConsolidationDataHandler extends BaseConsolidationDataHandler {

    private NestedConsolidationData nestedConsolidationData = null;

    public NestedConsolidationDataHandler(NestedConsolidationData nestedConsolidationData)
    {
        this.nestedConsolidationData = nestedConsolidationData;
    }

    @Override
    public void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId, String requirementId, RequirementAssignment requirementAssignment){
        EntityConsolidationData entityConsolidationData =
                getNestedTemplateConsolidationData(translateTo,
                        translateTo.getHeatFileName(),
                        translateTo.getTranslatedId());
        addNodesConnectedOut(entityConsolidationData, nodeTemplateId, requirementId, requirementAssignment);

    }

    @Override
    public void addNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId, String dependentNodeTemplateId, String targetResourceId, String requirementId, RequirementAssignment requirementAssignment){

        EntityConsolidationData entityConsolidationData =
                getNestedTemplateConsolidationData(translateTo,
                        translateTo.getHeatFileName(),
                        dependentNodeTemplateId);
        addNodesConnectedIn(entityConsolidationData, sourceNodeTemplateId, requirementId, requirementAssignment);

    }

    @Override
    public void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                TranslationContext context, String paramName, String contrailSharedResourceId,
                                                String sharedTranslatedResourceId) {

        throw new UnsupportedOperationException("API removeParamNameFromAttrFuncList not supported for NestedConsolidationDataHandler");

    }

    private NestedTemplateConsolidationData getNestedTemplateConsolidationData(
            TranslateTo translateTo, String nestedHeatFileName, String nestedNodeTemplateId) {

        ServiceTemplate serviceTemplate = getServiceTemplate(translateTo);
        TranslationContext context = getTranslationContext(translateTo);
        return getNestedTemplateConsolidationData(serviceTemplate, context, nestedHeatFileName, nestedNodeTemplateId);
    }

    private NestedTemplateConsolidationData getNestedTemplateConsolidationData(
            ServiceTemplate serviceTemplate,  TranslationContext context, String nestedHeatFileName, String nestedNodeTemplateId) {

        if (isNestedResourceIdOccuresInDifferentNestedFiles(context, nestedHeatFileName,
                nestedNodeTemplateId)) {
            throw new CoreException(
                    new DuplicateResourceIdsInDifferentFilesErrorBuilder(nestedNodeTemplateId).build());
        }

        if (isNodeTemplatePointsToServiceTemplateWithoutNodeTemplates(
                nestedNodeTemplateId, nestedHeatFileName, context)) {
            return null;
        }

        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        FileNestedConsolidationData fileNestedConsolidationData = nestedConsolidationData
                .getFileNestedConsolidationData(serviceTemplateFileName);

        if (fileNestedConsolidationData == null) {
            fileNestedConsolidationData = new FileNestedConsolidationData();
            nestedConsolidationData.setFileNestedConsolidationData(serviceTemplateFileName,
                    fileNestedConsolidationData);
        }

        NestedTemplateConsolidationData nestedTemplateConsolidationData =
                fileNestedConsolidationData.getNestedTemplateConsolidationData(nestedNodeTemplateId);
        if (nestedTemplateConsolidationData == null) {
            nestedTemplateConsolidationData = new NestedTemplateConsolidationData();
            nestedTemplateConsolidationData.setNodeTemplateId(nestedNodeTemplateId);
            fileNestedConsolidationData.setNestedTemplateConsolidationData(nestedNodeTemplateId,
                    nestedTemplateConsolidationData);
        }

        return nestedTemplateConsolidationData;
    }

    private boolean isNodeTemplatePointsToServiceTemplateWithoutNodeTemplates(String
                                                                                            nestedNodeTemplateId,
                                                                                    String nestedHeatFileName,
                                                                                    TranslationContext context) {

        return context.isServiceTemplateWithoutNodeTemplatesSection(
                FileUtils.getFileWithoutExtention(nestedHeatFileName))
                || context.isNodeTemplateIdPointsToStWithoutNodeTemplates(nestedNodeTemplateId);
    }

    private boolean isNestedResourceIdOccuresInDifferentNestedFiles(TranslationContext context,
                                                                           String nestedHeatFileName,
                                                                           String nestedNodeTemplateId) {
        return Objects.nonNull(nestedHeatFileName)
                && context.getAllTranslatedResourceIdsFromDiffNestedFiles(nestedHeatFileName)
                .contains(nestedNodeTemplateId);
    }
}
