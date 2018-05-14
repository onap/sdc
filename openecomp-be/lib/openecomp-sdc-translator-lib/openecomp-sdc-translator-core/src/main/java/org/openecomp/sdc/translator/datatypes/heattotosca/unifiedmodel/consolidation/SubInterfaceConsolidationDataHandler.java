package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

import java.util.Objects;
import java.util.Optional;

public class SubInterfaceConsolidationDataHandler implements ConsolidationDataHandler{

    private PortConsolidationData portConsolidationData = null;

    SubInterfaceConsolidationDataHandler(PortConsolidationData portConsolidationData) {
        this.portConsolidationData = portConsolidationData;
    }

    public void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId, String requirementId,
                                            RequirementAssignment requirementAssignment) {
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        if (Objects.nonNull(
                serviceTemplate.getTopology_template().getNode_templates().get(translateTo.getTranslatedId()))) {
            Optional<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationData =
                    getSubInterfaceTemplateConsolidationData(translateTo, translateTo.getTranslatedId());

            subInterfaceTemplateConsolidationData.ifPresent(
                    sInttemplateConsolidationData -> sInttemplateConsolidationData.addNodesConnectedOut(nodeTemplateId,
                            requirementId, requirementAssignment));

        }
    }

    public void addNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId,
                                           String dependentNodeTemplateId, String targetResourceId,
                                           String requirementId, RequirementAssignment requirementAssignment) {
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        TranslationContext translationContext = translateTo.getContext();
        Resource targetResource = translateTo.getHeatOrchestrationTemplate().getResources().get(targetResourceId);
        TranslateTo subInterfaceTo = new TranslateTo(translateTo.getHeatFileName(), serviceTemplate,
                                                            translateTo.getHeatOrchestrationTemplate(), targetResource,
                                                            targetResourceId, null, translationContext);
        Optional<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationData =
                getSubInterfaceTemplateConsolidationData(subInterfaceTo, targetResourceId);

        subInterfaceTemplateConsolidationData.ifPresent(
                sInttemplateConsolidationData -> sInttemplateConsolidationData.addNodesConnectedIn(sourceNodeTemplateId,
                        requirementId, requirementAssignment));

    }

    public void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
                                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                       String paramName,
                                                       String contrailSharedResourceId,
                                                       String sharedTranslatedResourceId) {


        throw new UnsupportedOperationException("API removeParamNameFromAttrFuncList not supported for SubInterfaceConsolidationDataHandler");

    }

    private Optional<SubInterfaceTemplateConsolidationData> getSubInterfaceTemplateConsolidationData(TranslateTo subInterfaceTo,
                                                                                                            String subInterfaceNodeTemplateId) {
        Optional<String> parentPortNodeTemplateId =
                HeatToToscaUtil.getSubInterfaceParentPortNodeTemplateId(subInterfaceTo);
        return parentPortNodeTemplateId.map(s -> getSubInterfaceTemplateConsolidationData(subInterfaceTo, s,
                subInterfaceNodeTemplateId));

    }

    private SubInterfaceTemplateConsolidationData getSubInterfaceTemplateConsolidationData(
                                                                                                  TranslateTo subInterfaceTo,
                                                                                                  String parentPortNodeTemplateId,
                                                                                                  String subInterfaceNodeTemplateId) {

        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(subInterfaceTo.getServiceTemplate());
        FilePortConsolidationData filePortConsolidationData = portConsolidationData
                                                                      .getFilePortConsolidationData(serviceTemplateFileName);

        if (filePortConsolidationData == null) {
            filePortConsolidationData = new FilePortConsolidationData();
            portConsolidationData.setFilePortConsolidationData(serviceTemplateFileName,
                    filePortConsolidationData);
        }

        PortTemplateConsolidationData portTemplateConsolidationData =
                filePortConsolidationData.getPortTemplateConsolidationData(parentPortNodeTemplateId);
        if (portTemplateConsolidationData == null) {
            portTemplateConsolidationData = new PortTemplateConsolidationData();
            portTemplateConsolidationData.setNodeTemplateId(parentPortNodeTemplateId);
            filePortConsolidationData.setPortTemplateConsolidationData(parentPortNodeTemplateId,
                    portTemplateConsolidationData);
        }

        return portTemplateConsolidationData.getSubInterfaceResourceTemplateConsolidationData(subInterfaceTo.getResource(),
                subInterfaceNodeTemplateId, parentPortNodeTemplateId);
    }

}
