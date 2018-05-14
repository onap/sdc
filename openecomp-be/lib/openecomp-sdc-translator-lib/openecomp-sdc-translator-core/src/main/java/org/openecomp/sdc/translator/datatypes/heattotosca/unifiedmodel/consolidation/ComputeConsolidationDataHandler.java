package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.NameExtractor;

import java.util.*;

public class ComputeConsolidationDataHandler extends BaseConsolidationDataHandler {

    private ComputeConsolidationData computeConsolidationData = null;

    ComputeConsolidationDataHandler(ComputeConsolidationData computeConsolidationData){
        this.computeConsolidationData = computeConsolidationData;
    }

    @Override
    public void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId, String requirementId,
                                     RequirementAssignment requirementAssignment){

        String translatedSourceNodeId = translateTo.getTranslatedId();
        ServiceTemplate serviceTemplate = getServiceTemplate(translateTo);
        NodeTemplate computeNodeTemplate = getNodeTemplate(serviceTemplate, translatedSourceNodeId);
        String nodeType = getNodeType(computeNodeTemplate);

        EntityConsolidationData entityConsolidationData =
                getComputeTemplateConsolidationData(translateTo, nodeType, translatedSourceNodeId);

        addNodesConnectedOut(entityConsolidationData, nodeTemplateId, requirementId, requirementAssignment);

    }

    @Override
    public void addNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId,
                                    String dependentNodeTemplateId, String targetResourceId, String requirementId,
                                    RequirementAssignment requirementAssignment){

        ServiceTemplate serviceTemplate = getServiceTemplate(translateTo);
        NodeTemplate nodeTemplate = getNodeTemplate(serviceTemplate, dependentNodeTemplateId);
        String nodeType = getNodeType(nodeTemplate, translateTo, targetResourceId, dependentNodeTemplateId );
        EntityConsolidationData entityConsolidationData =
                getComputeTemplateConsolidationData(translateTo,
                        nodeType,
                        dependentNodeTemplateId);
        addNodesConnectedIn(entityConsolidationData, sourceNodeTemplateId, requirementId, requirementAssignment);

    }

    @Override
    public void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                     TranslationContext context, String paramName, String contrailSharedResourceId,
                                                     String sharedTranslatedResourceId) {

        NodeTemplate nodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
                sharedTranslatedResourceId);
        EntityConsolidationData entityConsolidationData =
                getComputeTemplateConsolidationData(serviceTemplate, getNodeType(nodeTemplate),
                        sharedTranslatedResourceId);
        removeParamNameFromAttrFuncList(entityConsolidationData, paramName);

    }

    private ComputeTemplateConsolidationData getComputeTemplateConsolidationData(
            TranslateTo translateTo, String computeNodeType, String computeNodeTemplateId) {

        ServiceTemplate serviceTemplate = getServiceTemplate(translateTo);
        return getComputeTemplateConsolidationData(serviceTemplate, computeNodeType, computeNodeTemplateId);
    }


    private ComputeTemplateConsolidationData getComputeTemplateConsolidationData(
            ServiceTemplate serviceTemplate,
            String computeNodeType,
            String computeNodeTemplateId) {

        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        FileComputeConsolidationData fileComputeConsolidationData = computeConsolidationData
                .getFileComputeConsolidationData(serviceTemplateFileName);

        if (fileComputeConsolidationData == null) {
            fileComputeConsolidationData = new FileComputeConsolidationData();
            computeConsolidationData.setFileComputeConsolidationData(serviceTemplateFileName,
                    fileComputeConsolidationData);
        }

        TypeComputeConsolidationData typeComputeConsolidationData = fileComputeConsolidationData
                .getTypeComputeConsolidationData(computeNodeType);
        if (typeComputeConsolidationData == null) {
            typeComputeConsolidationData = new TypeComputeConsolidationData();
            fileComputeConsolidationData.setTypeComputeConsolidationData(computeNodeType,
                    typeComputeConsolidationData);
        }

        ComputeTemplateConsolidationData computeTemplateConsolidationData =
                typeComputeConsolidationData.getComputeTemplateConsolidationData(computeNodeTemplateId);
        if (computeTemplateConsolidationData == null) {
            computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
            computeTemplateConsolidationData.setNodeTemplateId(computeNodeTemplateId);
            typeComputeConsolidationData.setComputeTemplateConsolidationData(computeNodeTemplateId,
                    computeTemplateConsolidationData);
        }

        return computeTemplateConsolidationData;
    }

    private NodeTemplate getNodeTemplate (ServiceTemplate serviceTemplate,  String nodeTemplateId){
        return DataModelUtil.getNodeTemplate(serviceTemplate, nodeTemplateId);
    }

    private String getNodeType(NodeTemplate computeNodeTemplate, TranslateTo translateTo, String targetResourceId, String nodeTemplateId ){
        String nodeType;
        if (Objects.isNull(computeNodeTemplate)) {
            Resource targetResource =
                    translateTo.getHeatOrchestrationTemplate().getResources().get(targetResourceId);
            NameExtractor nodeTypeNameExtractor =
                    TranslationContext.getNameExtractorImpl(targetResource.getType());
            nodeType =
                    nodeTypeNameExtractor.extractNodeTypeName(translateTo.getHeatOrchestrationTemplate()
                                    .getResources().get(nodeTemplateId),
                            nodeTemplateId, nodeTemplateId);
        } else {
            nodeType = getNodeType(computeNodeTemplate);
        }

        return nodeType;
    }

    private String getNodeType(NodeTemplate nodeTemplate) {
        return nodeTemplate.getType();
    }

    }
