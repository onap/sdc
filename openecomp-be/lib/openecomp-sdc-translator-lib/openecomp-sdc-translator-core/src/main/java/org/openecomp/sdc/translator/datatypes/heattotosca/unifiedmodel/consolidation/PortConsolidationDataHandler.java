package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

public class PortConsolidationDataHandler implements ConsolidationDataHandler {

    private final PortConsolidationData portConsolidationData;

    public PortConsolidationDataHandler(PortConsolidationData portConsolidationData) {
        this.portConsolidationData = portConsolidationData;
    }

    @Override
    public void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId, String requirementId,
            RequirementAssignment requirementAssignment) {

        EntityConsolidationData entityConsolidationData =
                getPortTemplateConsolidationData(translateTo, translateTo.getResourceId(),
                        translateTo.getResource().getType(), translateTo.getTranslatedId());

        entityConsolidationData.addNodesConnectedOut(nodeTemplateId, requirementId, requirementAssignment);

    }

    @Override
    public void addNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId,
            String dependentNodeTemplateId, String targetResourceId, String requirementId,
            RequirementAssignment requirementAssignment) {

        EntityConsolidationData entityConsolidationData =
                getPortTemplateConsolidationData(translateTo, translateTo.getResourceId(),
                        translateTo.getResource().getType(), dependentNodeTemplateId);

        entityConsolidationData.addNodesConnectedIn(sourceNodeTemplateId, requirementId, requirementAssignment);

    }

    @Override
    public void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
            HeatOrchestrationTemplate heatOrchestrationTemplate, String paramName, String contrailSharedResourceId,
            String sharedTranslatedResourceId) {

        Resource resource = heatOrchestrationTemplate.getResources().get(contrailSharedResourceId);
        EntityConsolidationData entityConsolidationData = getPortTemplateConsolidationData(serviceTemplate,
                contrailSharedResourceId, resource.getType(), sharedTranslatedResourceId);
        entityConsolidationData.removeParamNameFromAttrFuncList(paramName);

    }

    private PortTemplateConsolidationData getPortTemplateConsolidationData(TranslateTo translateTo,
            String portResourceId, String portResourceType, String portNodeTemplateId) {
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        return getPortTemplateConsolidationData(serviceTemplate, portResourceId, portResourceType, portNodeTemplateId);
    }

    private PortTemplateConsolidationData getPortTemplateConsolidationData(ServiceTemplate serviceTemplate,
            String portResourceId, String portResourceType, String portNodeTemplateId) {

        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        FilePortConsolidationData filePortConsolidationData = portConsolidationData
                .getFilePortConsolidationData(serviceTemplateFileName);

        if (filePortConsolidationData == null) {
            filePortConsolidationData = new FilePortConsolidationData();
            portConsolidationData.setFilePortConsolidationData(serviceTemplateFileName,
                    filePortConsolidationData);
        }

        PortTemplateConsolidationData portTemplateConsolidationData =
                filePortConsolidationData.getPortTemplateConsolidationData(portNodeTemplateId);
        if (portTemplateConsolidationData == null) {
            portTemplateConsolidationData = getInitPortTemplateConsolidationData(portNodeTemplateId,
                    portResourceId, portResourceType);
            filePortConsolidationData.setPortTemplateConsolidationData(portNodeTemplateId,
                    portTemplateConsolidationData);
        }

        return portTemplateConsolidationData;
    }

    private static PortTemplateConsolidationData getInitPortTemplateConsolidationData(String portNodeTemplateId,
                                                                                      String portResourceId,
                                                                                      String portResourceType) {
        PortTemplateConsolidationData portTemplateConsolidationData = new PortTemplateConsolidationData();
        portTemplateConsolidationData.setNodeTemplateId(portNodeTemplateId);
        Optional<String> portNetworkRole = HeatToToscaUtil.evaluateNetworkRoleFromResourceId(portResourceId,
                portResourceType);
        portNetworkRole.ifPresent(portTemplateConsolidationData::setNetworkRole);
        return portTemplateConsolidationData;
    }
}
